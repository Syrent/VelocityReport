package ir.syrent.velocityreport.spigot

import com.google.gson.JsonObject
import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.bridge.BukkitBridge
import ir.syrent.velocityreport.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityreport.spigot.command.report.ReportCommand
import ir.syrent.velocityreport.spigot.command.reportadmin.ReportAdminCommand
import ir.syrent.velocityreport.spigot.listener.PlayerJoinListener
import ir.syrent.velocityreport.spigot.listener.PlayerQuitListener
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingEvent
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.Utils
import me.mohamad82.ruom.utils.MilliCounter
import org.bukkit.entity.Player
import java.util.*


class VelocityReportSpigot : RUoMPlugin() {

    var cooldowns = mutableMapOf<UUID, MilliCounter>()
    val networkPlayers = mutableSetOf<String>()
    val networkPlayersServer = mutableMapOf<UUID, String>()
    var reportsCount = 0
    var bridgeManager: BukkitBridgeManager? = null

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        initializeInstances()
        fetchData()
        registerCommands()
        registerListeners()

        if (Settings.velocitySupport) {
            initializePluginChannels()
        }
    }

    private fun initializeInstances() {
        AdventureApi.initialize()

        Settings
        Database
    }

    private fun fetchData() {
        var awaited = false
        Ruom.runSync({
            if (awaited) return@runSync
            awaited = true

            Database.getReportsCount(ReportStage.ACTIVE).whenComplete { count, _ ->
                reportsCount = count
                Ruom.getOnlinePlayers().let {
                    if (it.isNotEmpty()) {
                        if (Settings.velocitySupport) {
                            bridgeManager?.sendGetAllPlayersNameRequest(it.iterator().next())
                        }
                        it.map { player -> Utils.sendReportsActionbar(player) }
                    }
                }

                awaited = false
            }
        }, 0, 100)
    }

    private fun registerCommands() {
        ReportCommand(this)
        ReportAdminCommand()
    }

    private fun registerListeners() {
        PlayerJoinListener(this)
        PlayerQuitListener(this)
    }

    private fun initializePluginChannels() {
        val bridge = BukkitBridge()
        bridgeManager = BukkitBridgeManager(bridge, this)

        object : BukkitMessagingEvent(bridge) {
            override fun onPluginMessageReceived(player: Player, jsonObject: JsonObject) {
                bridgeManager!!.handleMessage(jsonObject)
            }
        }
    }

    override fun onDisable() {
        Database.shutdown()
        Ruom.shutdown()
    }

    companion object {
        lateinit var instance: VelocityReportSpigot
            private set
    }

}