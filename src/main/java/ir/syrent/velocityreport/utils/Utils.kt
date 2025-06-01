package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.entity.Player
import java.util.*

object Utils {

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