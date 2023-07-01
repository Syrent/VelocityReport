package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.entity.Player

object Utils {
    val actionbarPlayers = mutableSetOf<Player>()

    fun sendReportsActionbar(player: Player) {
        if (actionbarPlayers.contains(player)) return

        if (Settings.staffActionbarEnabled && player.hasPermission("velocityreport.admin.notify.actionbar")) {
            val reportsCount = VelocityReportSpigot.instance.reportsCount
            if (!Settings.staffActionbarSendZero && reportsCount < 1) return
            Ruom.runSync({
                player.sendActionbar(Message.REPORT_ACTIONBAR, TextReplacement("reports", reportsCount.toString()))
            }, 100, 100)
            actionbarPlayers.add(player)
        }
    }

    fun sendNewReportMessage(reporter: String, reported: String, server: String, reason: String) {
        for (player in Ruom.getOnlinePlayers()) {
            if (player.hasPermission("velocityreport.admin.notify.message")) {
                player.sendMessageOnly(
                    Message.REPORT_MESSAGE,
                    TextReplacement("reporter", reporter),
                    TextReplacement("reported", reported),
                    TextReplacement("server", server),
                    TextReplacement("reason", reason)
                )


                Settings.newReportSound.let {
                    if (it != null) {
                        player.playSound(player.location, it, 1f, 1f)
                    }
                }
            }
        }
    }
}