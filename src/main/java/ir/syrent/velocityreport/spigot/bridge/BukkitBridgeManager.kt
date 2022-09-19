package ir.syrent.velocityreport.spigot.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import ir.syrent.velocityreport.bridge.Bridge
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.utils.Utils
import me.mohamad82.ruom.utils.GsonUtils
import org.bukkit.entity.Player
import java.util.*

class BukkitBridgeManager(
    val bridge: Bridge,
    private val plugin: VelocityReportSpigot
) {

    fun sendGetAllPlayersNameRequest(sender: Player) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "PlayerList")

        sendPluginMessage(sender, messageJson)
    }

    fun sendReportsActionbar(sender: Player, count: Int) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "ReportsActionbar")
        messageJson.addProperty("count", count)

        sendPluginMessage(sender, messageJson)
    }

    private fun sendPluginMessage(sender: Player, messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(sender, byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        when (val type = messageJson["type"].asString) {
            "PlayerList" -> {
                val playerList = messageJson["players"].asString
                plugin.networkPlayers.clear()
                if (playerList.isNotEmpty()) {
                    val playerListSplit = playerList.split(",".toRegex()).toTypedArray()
                    for (playerName in playerListSplit) {
                        plugin.networkPlayers.add(playerName)
                    }
                }
            }
            "Server" -> {
                plugin.networkPlayersServer[UUID.fromString(messageJson["uuid"].asString)] = messageJson["server"].asString
            }
            "ReportsActionbar" -> {
                plugin.reportsCount = messageJson["count"].asInt
                for (player in Ruom.getOnlinePlayers()) {
                    Utils.sendReportsActionbar(player)
                }
            }
            else -> {
                Ruom.warn("Unsupported plugin message received from internal channel: $type")
            }
        }
    }

}