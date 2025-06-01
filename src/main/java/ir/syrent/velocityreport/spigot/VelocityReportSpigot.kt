package ir.syrent.velocityreport.spigot

import com.google.gson.JsonObject
import io.papermc.lib.PaperLib
import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.bridge.BukkitBridge
import ir.syrent.velocityreport.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityreport.spigot.command.report.ReportCommand
import ir.syrent.velocityreport.spigot.command.reportadmin.ReportAdminCommand
import ir.syrent.velocityreport.spigot.command.reports.ReportsCommand
import ir.syrent.velocityreport.spigot.hook.DependencyManager
import ir.syrent.velocityreport.spigot.listener.PlayerQuitListener
import ir.syrent.velocityreport.spigot.listener.PreReportListener
import ir.syrent.velocityreport.spigot.listener.PreReportUpdateListener
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingEvent
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Database.type
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.spigot.storage.Settings.bstats
import ir.syrent.velocityreport.spigot.storage.Settings.velocitySupport
import ir.syrent.velocityreport.utils.ServerVersion
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.Utils
import ir.syrent.velocityreport.utils.component
import ir.syrent.velocityreport.utils.sendActionbar
import org.bstats.bukkit.Metrics
import org.bukkit.entity.Player
import java.util.*

class VelocityReportSpigot : RUoMPlugin() {

    var cooldowns = mutableMapOf<UUID, Long>()
    val networkPlayers = mutableSetOf<String>()
    val networkPlayersServer = mutableMapOf<UUID, String>()
    var reportsCount = 0
    var bridgeManager: BukkitBridgeManager? = null

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        initializeInstances()
        sendWarningMessages()
        fetchData()
        registerCommands()
        registerListeners()
        if (velocitySupport) {
            initializePluginChannels()
        }

        if (bstats) {
            enableMetrics()
        }

        if (Settings.autoDoneEnabled) {
            autoDoneOldReportsRunnable()
        }

        initializeActionbarTask()
    }

    private fun sendWarningMessages() {
        if (velocitySupport && type === Database.DBType.SQLITE) {
            Ruom.log("You are using SQLite database, this is not recommended for Velocity servers.")
            Ruom.log("Please change database method to MySQL in `storage.yml` file.")
            Ruom.log("Otherwise, Data will not be sync between your servers.")
        }
        if (!ServerVersion.supports(16)) {
            Ruom.warn("Your running your server on a legacy minecraft version (< 16).")
            Ruom.warn("This plugin is not tested on legacy versions, so it may not work properly.")
            Ruom.warn("Please consider updating your server to 1.16.5 or higher.")
        }
        PaperLib.suggestPaper(this)
        DependencyManager
    }

    fun enableMetrics() {
        val pluginID = 16576
        Metrics(this, pluginID)
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
        }, Settings.autoDoneTime * 60 * 20, Settings.autoDoneTime * 60 * 20)
    }

    private fun registerCommands() {
        ReportCommand(this)
        ReportAdminCommand()
        ReportsCommand()
    }

    private fun registerListeners() {
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

    fun initializeActionbarTask() {
        Ruom.runSync({
            if (!Settings.staffActionbarEnabled) return@runSync
            if (!Settings.staffActionbarSendZero && reportsCount < 1) return@runSync
            for (player in Ruom.getOnlinePlayers()) {
                if (player.hasPermission("velocityreport.admin.notify.actionbar")) {
                    player.sendActionbar(Message.REPORT_ACTIONBAR, TextReplacement("reports", reportsCount.toString()))
                }
            }
        }, 1000, 1000)
    }

    override fun onDisable() {
        Database.shutdown()
        Ruom.shutdown()
    }

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: VelocityReportSpigot
            private set
    }

}