package ir.syrent.velocityreport.spigot.listener

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.event.PreReportEvent
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.Utils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Instant

class PreReportListener : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPreReport(event: PreReportEvent) {
        if (event.isCancelled) return

        if (Settings.velocitySupport) {
            VelocityReportSpigot.instance.bridgeManager?.sendNewReportRequest(Ruom.getOnlinePlayers().iterator().next(), event.report)
        } else {
            Utils.sendNewReportMessage(event.report.reporterName, event.report.reportedName, event.report.server, event.report.reason)
        }

        val embed = WebhookEmbedBuilder()
            .setColor(Integer.parseInt(Settings.discordEmbedColor.replaceFirst("#", ""), 16))
            .setAuthor(WebhookEmbed.EmbedAuthor(Settings.discordEmbedAuthor.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName), Settings.discordEmbedAuthorIconURL, Settings.discordEmbedAuthorURL.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName)))
            .setTitle(WebhookEmbed.EmbedTitle(Settings.discordEmbedTitle.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName), Settings.discordEmbedTitleURL.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName)))
            .setDescription(
                Settings.discordEmbedDescription
                    .replace("\$reporter", event.report.reporterName)
                    .replace("\$reported", event.report.reportedName)
                    .replace("\$reason", event.report.reason)
                    .replace("\$server", Settings.getServerAlias(event.report.server))
                    .replace("\\n", "\n")
            )
            .setImageUrl(Settings.discordEmbedImageURL.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName))
            .setThumbnailUrl(Settings.discordEmbedThumbnailURL.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName))
            .setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
            .setFooter(WebhookEmbed.EmbedFooter(Settings.discordEmbedFooter.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName), Settings.discordEmbedFooterIconURL.replace("\$reported", event.report.reportedName).replace("\$reporter", event.report.reporterName)))
            .build()
        Settings.webhookClient?.send(embed)
    }
}