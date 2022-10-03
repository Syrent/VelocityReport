package ir.syrent.velocityreport.bridge

import com.google.common.annotations.Beta
import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.velocitypowered.api.proxy.Player
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.utils.GsonUtils
import me.mohamad82.ruom.utils.MilliCounter
import java.util.*

class VelocityBridgeManager(
    val bridge: VelocityBridge,
    private val velocityAdapter: VelocityAdapter,
    private val cooldowns: MutableMap<UUID, MilliCounter>,
) {

    fun sendServer(player: Player) {
        val serverName = player.currentServer.let {
            if (it.isPresent) {
                it.get().serverInfo.name
            } else {
                "Unknown"
            }
        }
        val messageJson = JsonObject()
        messageJson.addProperty("type", "Server")
        messageJson.addProperty("uuid", player.uniqueId.toString())
        messageJson.addProperty("server", serverName)

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

    private fun sendNewReport(reporter: String, reported: String, server: String, reason: String) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "NewReport")
        messageJson.addProperty("reporter", reporter)
        messageJson.addProperty("reported", reported)
        messageJson.addProperty("server", server)
        messageJson.addProperty("reason", reason)

        sendPluginMessage(messageJson)
    }

    @Beta
    private fun sendPluginMessage(messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        when (messageJson["type"].asString) {
            "Server" -> {
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
            "NewReport" -> {
                sendNewReport(
                    messageJson["reporter"].asString,
                    messageJson["reported"].asString,
                    messageJson["server"].asString,
                    messageJson["reason"].asString
                )
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