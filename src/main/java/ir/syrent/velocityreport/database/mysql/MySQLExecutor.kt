package ir.syrent.velocityreport.database.mysql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ir.syrent.velocityreport.database.Database
import ir.syrent.velocityreport.database.Priority
import ir.syrent.velocityreport.database.Query
import ir.syrent.velocityreport.database.Query.StatusCode
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

abstract class MySQLExecutor(
    private val credentials: MySQLCredentials,
    @JvmField
    protected val poolingSize: Int,
    threadFactory: ThreadFactory?
) : Database() {
    @JvmField
    protected val threadPool: ExecutorService
    @JvmField
    protected var hikari: HikariDataSource? = null
    @JvmField
    protected var poolingUsed = 0

    init {
        threadPool = Executors.newFixedThreadPool(1.coerceAtLeast(poolingSize), threadFactory)
    }

    protected fun connect(driverClassName: String?) {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = credentials.url
        hikariConfig.driverClassName = driverClassName
        hikariConfig.username = credentials.username
        hikariConfig.password = credentials.password
        hikariConfig.maximumPoolSize = poolingSize
        hikari = HikariDataSource(hikariConfig)
    }

    protected fun tick() {
        val priorities: List<Priority> = ArrayList(listOf(*Priority.values()))
        for (priority in priorities) {
            val queries: MutableList<Query> = ArrayList(queue[priority] ?: emptyList())
            if (queries.isEmpty()) continue
            val removedQueries: MutableSet<Query> = HashSet()
            for (query in queries) {
                if (query.statusCode == StatusCode.FINISHED.code) removedQueries.add(query)
            }
            queries.removeAll(removedQueries)
            for (query in queries) {
                if (query.hasDoneRequirements() && query.statusCode != StatusCode.RUNNING.code) {
                    query.statusCode = StatusCode.RUNNING.code
                    executeQuery(query).whenComplete { statusCode: Int, _: Throwable? ->
                        query.statusCode = statusCode
                        poolingUsed--
                    }
                    poolingUsed++
                    if (poolingUsed >= poolingSize) break
                }
            }
            if (poolingUsed >= poolingSize) break
            if (queries.isNotEmpty()) break
        }
    }

    private fun executeQuery(query: Query): CompletableFuture<Int> {
        val completableFuture = CompletableFuture<Int>()
        val runnable = Runnable {
            val connection = createConnection()
            try {
                val preparedStatement = query.createPreparedStatement(connection)
                var resultSet: ResultSet? = null
                if (query.statement.startsWith("INSERT") ||
                    query.statement.startsWith("UPDATE") ||
                    query.statement.startsWith("DELETE") ||
                    query.statement.startsWith("CREATE")
                ) preparedStatement.executeUpdate() else resultSet = preparedStatement.executeQuery()
                query.completableFuture.complete(resultSet)
                closeConnection(connection)
                completableFuture.complete(StatusCode.FINISHED.code)
            } catch (e: SQLException) {
                onQueryFail(query)
                e.printStackTrace()
                query.increaseFailedAttempts()
                if (query.failedAttempts > failAttemptRemoval) {
                    closeConnection(connection)
                    completableFuture.complete(StatusCode.FINISHED.code)
                    onQueryRemoveDueToFail(query)
                }
                closeConnection(connection)
                completableFuture.complete(StatusCode.FAILED.code)
            }
        }
        threadPool.submit(runnable)
        return completableFuture
    }

    private fun createConnection(): Connection? {
        return try {
            hikari!!.connection
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    private fun closeConnection(connection: Connection?) {
        try {
            connection!!.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    protected abstract fun onQueryFail(query: Query?)
    protected abstract fun onQueryRemoveDueToFail(query: Query?)
}