package ir.syrent.velocityreport.report

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.event.PostReportEvent
import ir.syrent.velocityreport.spigot.event.PostReportUpdateEvent
import ir.syrent.velocityreport.spigot.event.PreReportEvent
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
    val callEvent: Boolean
) {
    private var prevReportData = this

    var reportID: UUID = UUID.randomUUID()
    var stage = ReportStage.ACTIVE
    var moderatorUUID: UUID? = null
    var moderatorName: String? = null

    init {
        if (callEvent) {
            var report = this
            val preReportEvent = PreReportEvent(report)
            VelocityReportSpigot.instance.server.pluginManager.callEvent(preReportEvent)
            report = preReportEvent.report

            if (!preReportEvent.isCancelled) {
                val postReportEvent = PostReportEvent(report)
                VelocityReportSpigot.instance.server.pluginManager.callEvent(postReportEvent)
            }
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

    fun decline() {
        stage = ReportStage.DECLINE
    }

    fun update(callEvent: Boolean): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        var report = this
        if (callEvent) {
            val preReportUpdateEvent = PreReportUpdateEvent(prevReportData, report)
            Ruom.runSync {
                VelocityReportSpigot.instance.server.pluginManager.callEvent(preReportUpdateEvent)
            }

            if (preReportUpdateEvent.isCancelled) {
                future.complete(false)
                return future
            }

            report = preReportUpdateEvent.newReport
        }

        Database.saveReport(report).whenComplete { _, _ ->
            val postReportUpdateEvent = PostReportUpdateEvent(prevReportData, report)
            Ruom.runSync {
                VelocityReportSpigot.instance.server.pluginManager.callEvent(postReportUpdateEvent)
            }
            prevReportData = report

            future.complete(true)
        }
        return future
    }

    enum class Mode {
        SIMPLE,
        CATEGORY
    }
}