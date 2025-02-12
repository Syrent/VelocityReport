package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.entity.Player
import java.util.*

object Utils {
    val actionbarPlayers = mutableSetOf<UUID>()

    fun sendReportsActionbar(player: Player) {
        if (actionbarPlayers.contains(player.uniqueId)) return

        if (Settings.staffActionbarEnabled && player.hasPermission("velocityreport.admin.notify.actionbar")) {
            Ruom.runSync({
                val reportsCount = VelocityReportSpigot.instance.reportsCount
                if (!Settings.staffActionbarSendZero && reportsCount < 1) return@runSync
                player.sendActionbar(Message.REPORT_ACTIONBAR, TextReplacement("reports", reportsCount.toString()))
            }, 100, 100)
            actionbarPlayers.add(player.uniqueId)
        }
    }

    fun sendNewReportMessage(reporter: String, reported: String, server: String, reason: String) {
        for (player in Ruom.getOnlinePlayers()) {
            if (player.hasPermission("velocityreport.admin.notify.message")) {
                player.sendMessageOnly(
                    Message.REPORT_MESSAGE,
                    TextReplacement("reporter", reporter),
                    TextReplacement("reported", reported),
                    TextReplacement("server", Settings.getServerAlias(server)),
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