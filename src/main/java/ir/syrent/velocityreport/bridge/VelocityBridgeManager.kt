package ir.syrent.velocityreport.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import ir.syrent.velocityreport.report.Report
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.utils.GsonUtils
import me.mohamad82.ruom.utils.MilliCounter
import java.util.*

class VelocityBridgeManager(
    val bridge: VelocityBridge,
    private val velocityAdapter: VelocityAdapter,
    private val cooldowns: MutableMap<UUID, MilliCounter>,
) {

    private fun sendReport(report: Report) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "Report")
        messageJson.addProperty("report_id", report.reportID.toString())
        messageJson.addProperty("reporter_id", report.reporterID.toString())
        messageJson.addProperty("reporter_name", report.reporterName)
        messageJson.addProperty("reported_name", report.reportedName)
        messageJson.addProperty("date", report.date)
        messageJson.addProperty("reason", report.reason)
        messageJson.addProperty("stage", report.stage.name)
        messageJson.addProperty("moderator_id", report.moderatorUUID.toString())
        messageJson.addProperty("moderator_name", report.moderatorName)


        sendPluginMessage(messageJson)
    }

    fun sendAllPlayersName() {
        val players = velocityAdapter.getAllPlayersName()

        val stringBuilder = StringBuilder()
        var playersList = ""
        if (players.isNotEmpty()) {
            for (player in players) {
                stringBuilder.append(player).append(",")
            }
            playersList = stringBuilder.substring(0, stringBuilder.length - 1)
        }

        val messageJson = JsonObject()
        messageJson.addProperty("type", "PlayerList")
        messageJson.addProperty("players", playersList)

        sendPluginMessage(messageJson)
    }

    private fun sendPluginMessage(messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        when (messageJson["type"].asString) {
            "Report" -> {
                val allowedCooldown = messageJson["cooldown"].asInt
                val reporterUUID = UUID.fromString(messageJson["reporter_id"].asString)
                val cooldownCounter = cooldowns[reporterUUID]!!
                cooldownCounter.stop()
                val elapsedCooldown = cooldownCounter.get() / 1000

                if (elapsedCooldown < allowedCooldown) {
                    // TODO: On Cooldown
                } else {
                    resetCooldown(reporterUUID)
                }
            }
            "PlayerList" -> {
                sendAllPlayersName()
            }
            else -> {
                VRuom.warn("Unsupported message type: ${messageJson["type"].asString}")
            }
        }
    }

    private fun resetCooldown(playerUuid: UUID) {
        val newCooldownCounter = MilliCounter()
        newCooldownCounter.start()
        cooldowns[playerUuid] = newCooldownCounter
    }

}