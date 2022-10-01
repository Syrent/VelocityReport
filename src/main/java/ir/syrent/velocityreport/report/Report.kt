package ir.syrent.velocityreport.report

import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.event.PostReportEvent
import ir.syrent.velocityreport.spigot.event.PreReportEvent
import ir.syrent.velocityreport.spigot.event.PostReportUpdateEvent
import ir.syrent.velocityreport.spigot.event.PreReportUpdateEvent
import ir.syrent.velocityreport.spigot.storage.Database
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

data class Report(
    val server: String,
    val reporterID: UUID,
    val reporterName: String,
    val reportedName: String,
    val date: Long,
    val reason: String,
) {
    private var prevReportData = this

    var reportID: UUID = UUID.randomUUID()
    var stage = ReportStage.ACTIVE
    var moderatorUUID: UUID? = null
    var moderatorName: String? = null

    init {
        val preReportEvent = PreReportEvent(this)
        VelocityReportSpigot.instance.server.pluginManager.callEvent(preReportEvent)

        if (!preReportEvent.isCancelled) {
            val postReportEvent = PostReportEvent(this)
            VelocityReportSpigot.instance.server.pluginManager.callEvent(postReportEvent)
        }
    }

    fun setModerator(player: Player) {
        moderatorUUID = player.uniqueId
        moderatorName = player.name
    }

    fun active() {
        stage = ReportStage.ACTIVE
    }

    fun pending() {
        stage = ReportStage.PENDING
    }

    fun done() {
        stage = ReportStage.DONE
    }

    fun update(callEvent: Boolean): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        if (callEvent) {
            val preReportUpdateEvent = PreReportUpdateEvent(prevReportData, this)
            VelocityReportSpigot.instance.server.pluginManager.callEvent(preReportUpdateEvent)

            if (preReportUpdateEvent.isCancelled) {
                future.complete(false)
                return future
            }
        }

        Database.saveReport(this).whenComplete { _, _ ->
            val postReportUpdateEvent = PostReportUpdateEvent(prevReportData, this)
            VelocityReportSpigot.instance.server.pluginManager.callEvent(postReportUpdateEvent)
            prevReportData = this

            future.complete(true)
        }
        return future
    }
}