package ir.syrent.velocityreport.spigot

import com.google.gson.JsonObject
import com.jeff_media.updatechecker.UpdateCheckSource
import com.jeff_media.updatechecker.UpdateChecker
import io.papermc.lib.PaperLib
import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.bridge.BukkitBridge
import ir.syrent.velocityreport.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityreport.spigot.command.report.ReportCommand
import ir.syrent.velocityreport.spigot.command.reportadmin.ReportAdminCommand
import ir.syrent.velocityreport.spigot.command.reports.ReportsCommand
import ir.syrent.velocityreport.spigot.hook.DependencyManager
import ir.syrent.velocityreport.spigot.listener.PlayerJoinListener
import ir.syrent.velocityreport.spigot.listener.PlayerQuitListener
import ir.syrent.velocityreport.spigot.listener.PreReportListener
import ir.syrent.velocityreport.spigot.listener.PreReportUpdateListener
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingEvent
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Database.type
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.spigot.storage.Settings.bstats
import ir.syrent.velocityreport.spigot.storage.Settings.velocitySupport
import ir.syrent.velocityreport.utils.ServerVersion
import ir.syrent.velocityreport.utils.Utils
import ir.syrent.velocityreport.utils.component
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import org.bstats.bukkit.Metrics
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
        sendFiglet()
        sendWarningMessages()
        checkUpdate()
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
    }

    private fun sendFiglet() {
       sendConsoleMessage(" ")
       sendConsoleMessage("<gold>__     __   _            _ _         ____                       _   ")
       sendConsoleMessage("<gold>\\ \\   / /__| | ___   ___(_) |_ _   _|  _ \\ ___ _ __   ___  _ __| |_ ")
       sendConsoleMessage("<gold> \\ \\ / / _ \\ |/ _ \\ / __| | __| | | | |_) / _ \\ '_ \\ / _ \\| '__| __|")
       sendConsoleMessage("<gold>  \\ V /  __/ | (_) | (__| | |_| |_| |  _ <  __/ |_) | (_) | |  | |_ ")
       sendConsoleMessage("<gold>   \\_/ \\___|_|\\___/ \\___|_|\\__|\\__, |_| \\_\\___| .__/ \\___/|_|   \\__|")
       sendConsoleMessage("<gold>                               |___/          |_|                   v${Ruom.getServer().pluginManager.getPlugin("VelocityReport")?.description?.version ?: " Unknown"}")
       sendConsoleMessage(" ")
       sendConsoleMessage("<white>Wiki: <blue><u>https://github.com/Syrent/VelocityReport/wiki</u></blue>")
       sendConsoleMessage(" ")
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

    private fun checkUpdate() {
        Thread {
            try {
                UpdateChecker(this, UpdateCheckSource.SPIGOT, 105378.toString())
                    .setDownloadLink("https://www.spigotmc.org/resources/velocityreport-1-8-1-19-3-mysql-sqlite.105378/")
                    .checkEveryXHours(24.0)
                    .setChangelogLink(105378.toString())
                    .setNotifyOpsOnJoin(true)
                    .setNotifyByPermissionOnJoin("velocityreport.updatechecker")
                    .setTimeout(30 * 1000)
                    .setSupportLink("https://discord.gg/xZyYGU4EG4")
                    .checkNow()
            } catch (_: Exception) {
                Ruom.warn("Could not check for updates, check your connection.")
            }
        }.start()
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
        }, 0, Settings.autoDoneTime * 60 * 20)
    }

    private fun registerCommands() {
        ReportCommand(this)
        ReportAdminCommand()
        ReportsCommand()
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

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: VelocityReportSpigot
            private set
    }

}