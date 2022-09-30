package ir.syrent.velocityreport.spigot.command.report

import ir.syrent.velocityreport.report.Report
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.adventure.ComponentUtils
import ir.syrent.velocityreport.spigot.command.library.PluginCommand
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.component
import ir.syrent.velocityreport.utils.openBook
import ir.syrent.velocityreport.utils.sendMessage
import me.mohamad82.ruom.utils.MilliCounter
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.roundToInt

class ReportCommand(
    private val plugin: VelocityReportSpigot
) : PluginCommand("report", "velocityreport.report", true) {

    init {
        this.register()
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            sender.sendMessage(Message.REPORT_USAGE)
            return
        }

        var target = plugin.server.getPlayerExact(args[0])?.name

        if (Settings.velocitySupport) {
            target = plugin.networkPlayers.findLast { it.lowercase() == args[0].lowercase() }
        }

        if (target == null) {
            sender.sendMessage(Message.NO_TARGET)
            return
        }

        if (target == sender.name && Settings.preventSelfReport) {
            sender.sendMessage(Message.REPORT_PREVENT_SELF)
            return
        }

        if (args.size == 1) {
            val title = ComponentUtils.parse("VelocityReport")
            var pageLines = 14

            val pages = mutableListOf<Component>()
            val header = Settings.bookHeader
            val footer = Settings.bookFooter
            var lineCount = 0

            pageLines -= header.size
            pageLines -= footer.size

            val page = StringBuilder()

            page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
            val enabledReasons = Settings.reasons.filter { it.enabled }
            for (reason in enabledReasons) {
                if (lineCount == pageLines) {
                    pages.add(page.toString().component())
                    page.clear()
                    page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                    lineCount = 0
                }

                page.append(Settings.formatMessage(
                    Message.REPORTADMIN_MYREPORTS_BOOK_FORMAT,
                    TextReplacement("player", target),
                    TextReplacement("id", reason.id),
                    TextReplacement("name", reason.displayName),
                    TextReplacement("description", reason.description)
                ).replace("\\n", "\n")).append("\n")

                if (lineCount == pageLines) {
                    page.append(Settings.formatMessage(header).joinToString("\n"))
                }

                lineCount++
            }
            page.append(Settings.formatMessage(header).joinToString("\n"))
            pages.add(page.toString().component())

            sender.openBook(Book.book(title, title, pages))
        } else {
            val reasons = Settings.reasons.filter { it.enabled }
            val reason = args.subList(1, args.size).joinToString(" ").lowercase()
            val formattedReason = reasons.findLast { it.id.lowercase() == reason.lowercase() }?.displayName ?: reason

            if (!Settings.customReason && !reasons.map { it.id.lowercase() }.contains(reason.lowercase())) {
                sender.sendMessage(Message.INVALID_REASON, TextReplacement("reason", formattedReason))
                return
            }

            if (plugin.cooldowns.containsKey(sender.uniqueId) && !sender.hasPermission("velocityreport.bypass.cooldown")) {
                val cooldownCounter = plugin.cooldowns[sender.uniqueId]!!
                cooldownCounter.stop()
                val elapsedCooldown = cooldownCounter.get() / 1000
                val allowedCooldown = Settings.cooldown

                if (elapsedCooldown < allowedCooldown) {
                    sender.sendMessage(Message.REPORT_COOLDOWN, TextReplacement("time", ((allowedCooldown - elapsedCooldown).roundToInt() + 1).toString()))
                    return
                }
            }

            Report(
                plugin.networkPlayersServer[sender.uniqueId] ?: "Unknown",
                sender.uniqueId,
                sender.name,
                target,
                System.currentTimeMillis(),
                MiniMessage.miniMessage().stripTags(formattedReason)
            ).update().whenComplete {_, _ ->
                val newCooldownCounter = MilliCounter()
                newCooldownCounter.start()
                plugin.cooldowns[sender.uniqueId] = newCooldownCounter
                sender.sendMessage(Message.REPORT_USE, TextReplacement("player", target), TextReplacement("reason", formattedReason))
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        when (args.size) {
            1 -> {
                return if (Settings.velocitySupport) plugin.networkPlayers.filter { it.startsWith(args[0], true) }
                else Ruom.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[0], true) }
            }
            2 -> {
                return Settings.reasons.filter { it.enabled && it.id.startsWith(args[1], true) }.map { it.id }
            }
        }

        return emptyList()
    }
}