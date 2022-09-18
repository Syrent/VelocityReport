package ir.syrent.velocityreport.report

import com.velocitypowered.api.proxy.Player
import java.util.UUID

data class Report(
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

    fun update() {
        // TODO: Update report in database
    }
}