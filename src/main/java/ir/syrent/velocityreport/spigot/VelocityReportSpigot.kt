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
import ir.syrent.velocityreport.spigot.listener.PreReportListener
import ir.syrent.velocityreport.spigot.listener.PreReportUpdateListener
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingEvent
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Database.type
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.spigot.storage.Settings.velocitySupport
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
        sendWarningMessage()
        fetchData()
        registerCommands()
        registerListeners()

        if (velocitySupport) {
            initializePluginChannels()
        }

        if (Settings.autoDoneEnabled) autoDoneOldReportsRunnable()
    }

    private fun sendWarningMessage() {
        if (velocitySupport && type === Database.DBType.SQLITE) {
            Ruom.warn("You are using SQLite database, this is not recommended for Velocity servers.")
            Ruom.warn("Please change database method to MySQL in `storage.yml` file.")
            Ruom.warn("Otherwise, Data will not be sync between your servers.")
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
                        if (velocitySupport) {
                            bridgeManager?.sendGetAllPlayersNameRequest(it.iterator().next())
                        }
                        it.map { player -> Utils.sendReportsActionbar(player) }
                    }
                }

                awaited = false
            }
        }, 0, 100)
    }

    private fun autoDoneOldReportsRunnable() {
        var awaited = false
        Ruom.runSync({
            if (awaited) return@runSync
            awaited = true

            Database.getReports(ReportStage.ACTIVE).whenComplete { reports, _ ->
                for (report in reports) {
                    if (System.currentTimeMillis() > (report.date + Settings.autoDoneTime * 20)) {
                        report.done()
                        report.update(Settings.autoDoneCallUpdateEvent)
                    }
                }
                awaited = false
            }
        }, 0, Settings.autoDoneTime * 20)
    }

    private fun registerCommands() {
        ReportCommand(this)
        ReportAdminCommand()
    }

    private fun registerListeners() {
        PlayerJoinListener(this)
        PlayerQuitListener(this)

        PreReportListener()
        PreReportUpdateListener()
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