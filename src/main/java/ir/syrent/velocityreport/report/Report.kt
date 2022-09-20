package ir.syrent.velocityreport.report

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Settings
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
    var reportID: UUID = UUID.randomUUID()
    var stage = ReportStage.ACTIVE
    var moderatorUUID: UUID? = null
    var moderatorName: String? = null

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

    fun update(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        Database.saveReport(this).whenComplete { _, _ ->
            Database.getReportsCount(ReportStage.ACTIVE).whenComplete { count, _ ->
                if (Settings.velocitySupport) {
                    if (Ruom.getOnlinePlayers().isNotEmpty()) {
                        VelocityReportSpigot.instance.bridgeManager!!.sendReportsActionbar(Ruom.getOnlinePlayers().iterator().next(), count)
                    }
                } else {
                    VelocityReportSpigot.instance.reportsCount = count
                }
                future.complete(true)
            }
        }
        return future
    }
}