package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import org.bukkit.entity.Player

object Utils {
    fun sendReportsActionbar(player: Player) {
        val reportsCount = VelocityReportSpigot.instance.reportsCount
        if (!Settings.staffActionbarSendZero && reportsCount < 1) return

        if (Settings.staffActionbarEnabled && player.hasPermission("velocityreport.admin.notify")) {
            Ruom.runSync({
                player.sendActionbar(Message.REPORT_ACTIONBAR, TextReplacement("reports", reportsCount.toString()))
            }, 0, 20)
        }
    }
}