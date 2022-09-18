package ir.syrent.velocityreport.report

import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Database
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID
import java.util.concurrent.CompletableFuture

data class Report(
    val server: String,
    val reporterID: UUID,
    val reporterName: String,
    val reportedName: String,
    val date: Long,
    val reason: String,
) {
    val reportID = UUID.randomUUID()
    var stage = ReportStage.ACTIVE
    var moderatorUUID: UUID? = null
    var moderatorName: String? = null

    fun setModerator(player: Player) {
        moderatorUUID = player.uniqueId
        moderatorName = player.username
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
        Database.saveReport(
            Report(
                VelocityReportSpigot.instance.networkPlayersServer[reporterID] ?: "Unknown",
                reporterID,
                reporterName,
                reportedName,
                System.currentTimeMillis(),
                MiniMessage.miniMessage().stripTags(reason)
            )
        ).whenComplete { _, _ -> future.complete(true) }
        return future
    }
}