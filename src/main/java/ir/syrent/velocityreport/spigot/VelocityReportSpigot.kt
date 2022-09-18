package ir.syrent.velocityreport.spigot

import com.google.gson.JsonObject
import ir.syrent.velocityreport.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.bridge.BukkitBridge
import ir.syrent.velocityreport.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingEvent
import ir.syrent.velocityreport.spigot.storage.Database
import me.mohamad82.ruom.utils.MilliCounter
import org.bukkit.entity.Player
import java.util.*


class VelocityReportSpigot : RUoMPlugin() {

    var cooldowns = mutableMapOf<UUID, MilliCounter>()
    val networkPlayers = mutableSetOf<String>()
    var bridgeManager: BukkitBridgeManager? = null

    override fun onEnable() {
        dataFolder.mkdir()

        AdventureApi.initialize()

        for (player in Ruom.getOnlinePlayers()) {
            val cooldownCounter = MilliCounter()
            cooldownCounter.start()
            cooldowns[player.uniqueId] = cooldownCounter
        }

        Database

        initializePluginChannels()
    }

    fun initializePluginChannels() {
        val bridge = BukkitBridge()
        bridgeManager = BukkitBridgeManager(bridge, this)
        object : BukkitMessagingEvent(bridge) {
            override fun onPluginMessageReceived(player: Player, jsonObject: JsonObject?) {
                bridgeManager!!.handleMessage(jsonObject!!)
            }
        }
    }

    override fun onDisable() {
        Database.shutdown()
        Ruom.shutdown()
    }
}