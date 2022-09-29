package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import org.bukkit.entity.Player

object Utils {
    val actionbarPlayers = mutableSetOf<Player>()

    fun sendReportsActionbar(player: Player) {
        if (actionbarPlayers.contains(player)) return

        if (Settings.staffActionbarEnabled && player.hasPermission("velocityreport.admin.notify")) {
            Ruom.runSync({
                val reportsCount = VelocityReportSpigot.instance.reportsCount
                if (!Settings.staffActionbarSendZero && reportsCount < 1) return@runSync

                player.sendActionbar(Message.REPORT_ACTIONBAR, TextReplacement("reports", reportsCount.toString()))
                actionbarPlayers.add(player)
            }, 0, 40)
        }
    }
}