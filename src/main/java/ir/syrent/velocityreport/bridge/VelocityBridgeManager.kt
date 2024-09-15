package ir.syrent.velocityreport.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityreport.utils.ruom.VRuom
import ir.syrent.velocityreport.utils.ruom.utils.GsonUtils
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
class VelocityBridgeManager(
    val bridge: VelocityBridge,
    private val velocityAdapter: VelocityAdapter,
    private val cooldowns: MutableMap<UUID, MilliCounter>,
) {

    val hasSayanVanish = VRuom.getServer().pluginManager.getPlugin("sayanvanish").getOrNull() != null

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

        val vanishedUsers = if (hasSayanVanish) SayanVanishVelocityAPI.getInstance().getVanishedUsers().map { it.username } else emptyList()

        val messageJson = JsonObject()
        messageJson.addProperty("type", "PlayerList")
        messageJson.addProperty("players", players.filter { !vanishedUsers.contains(it) }.joinToString(","))

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

    private fun sendPluginMessage(messageJson: JsonObject) {
        if (messageJson.isEmpty) return
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        if (messageJson.isEmpty) return
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