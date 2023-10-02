package ir.syrent.velocityreport.spigot.listener

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.event.PreReportUpdateEvent
import ir.syrent.velocityreport.spigot.storage.Settings
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PreReportUpdateListener : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPreReportUpdate(event: PreReportUpdateEvent) {
        if (event.isCancelled) return

        val moderator = event.newReport.moderatorName?.let { Bukkit.getPlayerExact(it) } ?: return
        when (event.newReport.stage) {
            ReportStage.ACTIVE -> {
                if (!Settings.acceptCommandsEnabled) return
                for (command in Settings.acceptCommands) {
                    Ruom.getServer().dispatchCommand(Ruom.getConsoleSender(), command.replace("\$moderator", moderator.name).replace("\$reporter", event.newReport.reporterName).replace("\$reported", event.newReport.reportedName))
                }
            }
            ReportStage.DONE -> {
                if (!Settings.doneCommandsEnabled) return
                for (command in Settings.doneCommands) {
                    Ruom.getServer().dispatchCommand(Ruom.getConsoleSender(), command.replace("\$moderator", moderator.name).replace("\$reporter", event.newReport.reporterName).replace("\$reported", event.newReport.reportedName))
                }
            }
            else -> {}
        }
    }
}