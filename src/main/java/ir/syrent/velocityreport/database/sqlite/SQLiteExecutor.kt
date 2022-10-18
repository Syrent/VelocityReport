package ir.syrent.velocityreport.database.sqlite

import ir.syrent.velocityreport.database.Database
import ir.syrent.velocityreport.database.Priority
import ir.syrent.velocityreport.database.Query
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.logging.Logger

abstract class SQLiteExecutor protected constructor(private val dbFile: File, private val logger: Logger?) :
    Database() {
    @JvmField
    protected var connection: Connection? = null

    init {
        try {
            if (!dbFile.exists()) dbFile.createNewFile()
        } catch (e: IOException) {
            logger?.severe("Failed to create the sqlite database file. Stacktrace:")
            e.printStackTrace()
        }
    }

    override fun connect() {
        try {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.path)
        } catch (e: SQLException) {
            logger?.severe(e.message)
            e.printStackTrace()
        }
    }

    protected fun tick() {
        val priorities: List<Priority> = ArrayList(listOf(*Priority.values()))
        for (priority in priorities) {
            val queries = queue[priority]
            if (queries!!.isEmpty()) continue
            val query = queries[0]
            try {
                val preparedStatement = query.createPreparedStatement(connection)
                var resultSet: ResultSet? = null
                if (query.statement.startsWith("INSERT") ||
                    query.statement.startsWith("UPDATE") ||
                    query.statement.startsWith("DELETE") ||
                    query.statement.startsWith("CREATE")
                ) preparedStatement.executeUpdate() else resultSet = preparedStatement.executeQuery()
                query.completableFuture.complete(resultSet)
                queries.removeAt(0)
            } catch (e: SQLException) {
                onQueryFail(query)
                e.printStackTrace()
                query.increaseFailedAttempts()
                if (query.failedAttempts > failAttemptRemoval) {
                    queries.removeAt(0)
                    onQueryRemoveDueToFail(query)
                }
            }
            break
        }
    }

    protected abstract fun onQueryFail(query: Query?)
    protected abstract fun onQueryRemoveDueToFail(query: Query?)
}