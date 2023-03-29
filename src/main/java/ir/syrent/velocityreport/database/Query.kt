package ir.syrent.velocityreport.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

open class Query protected constructor(statement: String) {
    private val statementValues: MutableMap<Int, Any> = HashMap()
    private val queryRequirements: MutableSet<Query> = HashSet()
    val completableFuture: CompletableFuture<ResultSet> = CompletableFuture()
    val statement: String
    var failedAttempts = 0
        protected set
    var statusCode = StatusCode.NOT_STARTED.code

    init {
        this.statement = statement
    }

    fun addRequirement(query: Query): Query {
        queryRequirements.add(query)
        return this
    }

    fun getRequirements(): Set<Query> {
        return queryRequirements
    }

    fun hasDoneRequirements(): Boolean {
        var hasDoneRequirements = true
        for (query in queryRequirements) {
            if (query.statusCode != StatusCode.FINISHED.code) {
                hasDoneRequirements = false
                break
            }
        }
        return hasDoneRequirements
    }

    fun increaseFailedAttempts() {
        failedAttempts += 1
    }

    fun setStatementValue(index: Int, value: Any): Query {
        statementValues[index] = value
        return this
    }

    @Throws(SQLException::class)
    fun createPreparedStatement(connection: Connection?): PreparedStatement {
        val preparedStatement = connection!!.prepareStatement(statement)
        for (index in statementValues.keys) {
            val value = statementValues[index]
            preparedStatement.setObject(index, value)
        }
        return preparedStatement
    }

    enum class StatusCode(val code: Int) {
        NOT_STARTED(-1), RUNNING(0), FAILED(1), FINISHED(2);
    }

    companion object {
        fun query(statement: String): Query {
            return Query(statement)
        }
    }
}