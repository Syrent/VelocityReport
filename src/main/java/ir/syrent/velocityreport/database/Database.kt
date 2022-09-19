package ir.syrent.velocityreport.database

import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

abstract class Database protected constructor() {
    @JvmField
    protected val queue: MutableMap<Priority, MutableList<Query>> = EnumMap(Priority::class.java)
    @JvmField
    protected var failAttemptRemoval = 2

    init {
        for (priority in Priority.values()) {
            queue[priority] = ArrayList()
        }
    }

    /**
     * Initializes the database connection.
     */
    abstract fun connect()

    /**
     * Shutdowns the database once queue becomes empty.
     * @return A completableFuture that will be completed once database shutdowns successfully.
     */
    abstract fun scheduleShutdown(): CompletableFuture<Void?>?

    /**
     * Force shutdowns the database and clears the queue.
     */
    abstract fun shutdown()

    /**
     * Queues a query.
     * @param query Statement that is going to run.
     * @param priority Priority of the query in queue. Higher priorities will be run sooner in the queue.
     * @return Query class that contains CompletableFuture with ResultSet callback. Useful when you need the results of a query.
     * @see Query
     */
    fun queueQuery(query: Query, priority: Priority): Query {
        queue[priority]!!.add(query)
        return query
    }

    /**
     * Queues a query with normal priority.
     * @param query Statement that is going to run.
     * @return Query class that contains CompletableFuture with ResultSet callback. Useful when you need the results of a query.
     * @see Query
     */
    fun queueQuery(query: Query): Query {
        queue[Priority.NORMAL]!!.add(query)
        return query
    }

    /**
     * Returns whether queue is empty or not.
     * @return true if queue is empty
     */
    val isQueueEmpty: Boolean
        get() {
            for (priority in queue.keys) {
                if (queue[priority]!!.isNotEmpty()) return false
            }
            return true
        }
}