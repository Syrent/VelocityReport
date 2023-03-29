package ir.syrent.velocityreport.spigot.command.report

import ir.syrent.velocityreport.report.Report
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.adventure.ComponentUtils
import ir.syrent.velocityreport.spigot.command.library.PluginCommand
import ir.syrent.velocityreport.spigot.hook.DependencyManager
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.component
import ir.syrent.velocityreport.utils.openBook
import ir.syrent.velocityreport.utils.sendMessage
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
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

        //           1          2        3
        // Report <player> <category> <reason>
        if (args.size <= 3) {
            val targetPlayer = plugin.server.getPlayerExact(args[0])
            var target = targetPlayer?.name

            if (Settings.velocitySupport && plugin.networkPlayers.isNotEmpty()) {
                target = plugin.networkPlayers.findLast { it.lowercase() == args[0].lowercase() }
            } else {
                /*
                * Support for SuperVanish/PremiumVanish/VelocityVanish and all plugins that save vanished meta on player
                * Note: only works on backend servers, so it should only work when `velocity_support` is off
                */
                if (targetPlayer != null) {
                    if (targetPlayer.getMetadata("vanished").map { it.asBoolean() }.contains(true)) {
                        sender.sendMessage(Message.NO_TARGET)
                        return
                    }
                }
            }

            if (target == null) {
                sender.sendMessage(Message.NO_TARGET)
                return
            }

            /*
            * Prevent players from reporting vanished players if VelocityVanish installed on server
            */
            if (DependencyManager.velocityVanishHook.exists) {
                if (VelocityVanishSpigot.instance.vanishedNames.map { it.lowercase() }.contains(target)) {
                    sender.sendMessage(Message.NO_TARGET)
                    return
                }
            }

            if (target == sender.name && Settings.preventSelfReport) {
                sender.sendMessage(Message.REPORT_PREVENT_SELF)
                return
            }

            val title = ComponentUtils.parse("VelocityReport")

            var pageLines = 14

            val header = Settings.bookHeader
            val footer = Settings.bookFooter

            pageLines -= header.size
            pageLines -= footer.size


            when (args.size) {
                1 -> {
                    val pages = mutableListOf<Component>()
                    var lineCount = 0

                    val page = StringBuilder()

                    page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                    val enabledCategories = Settings.categories.filter { it.enabled }
                    for (category in enabledCategories) {
                        if (lineCount == pageLines) {
                            pages.add(page.toString().component())
                            page.clear()
                            page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                            lineCount = 0
                        }

                        page.append(
                            Settings.formatMessage(
                                Message.BOOK_CATEGORY,
                                TextReplacement("player", target),
                                TextReplacement("category", category.id),
                                TextReplacement("name", category.displayName)
                            ).replace("\\n", "\n")
                        ).append("\n")

                        if (lineCount == pageLines) {
                            page.append(Settings.formatMessage(footer).joinToString("\n"))
                        }

                        lineCount++
                    }
                    page.append(Settings.formatMessage(footer).joinToString("\n"))
                    pages.add(page.toString().component())

                    sender.openBook(Book.book(title, title, pages))
                }
                2 -> {
                    val enabledReasons =
                        Settings.categories.findLast { it.id == args[1] }?.reasons?.filter { it.enabled } ?: run {
                            sender.sendMessage(Message.INVALID_CATEGORY, TextReplacement("category", args[1]))
                            return
                        }
                    val pages = mutableListOf<Component>()
                    var lineCount = 0

                    val page = StringBuilder()

                    page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                    for (reason in enabledReasons) {
                        if (lineCount == pageLines) {
                            pages.add(page.toString().component())
                            page.clear()
                            page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                            lineCount = 0
                        }

                        page.append(
                            Settings.formatMessage(
                                Message.BOOK_REASON,
                                TextReplacement("player", target),
                                TextReplacement("reason", reason.id),
                                TextReplacement("category", reason.category),
                                TextReplacement("name", reason.displayName),
                                TextReplacement("description", reason.description)
                            ).replace("\\n", "\n")
                        ).append("\n")

                        if (lineCount == pageLines) {
                            page.append(Settings.formatMessage(footer).joinToString("\n"))
                        }

                        lineCount++
                    }
                    page.append(Settings.formatMessage(footer).joinToString("\n"))
                    pages.add(page.toString().component())

                    sender.openBook(Book.book(title, title, pages))
                }

                3 -> {
                    val reasons = Settings.categories.findLast { it.id == args[1] }?.reasons?.filter { it.enabled } ?: run {
                        if (!Settings.customReason) {
                            sender.sendMessage(Message.INVALID_CATEGORY, TextReplacement("category", args[1]))
                        }
                    }.let { emptyList() }
                    val reason = args.subList(if (Settings.customReason) 1 else 2, args.size).joinToString(" ").lowercase()
                    val formattedReason =
                        reasons.findLast { it.id.lowercase() == reason.lowercase() }?.displayName ?: reason

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
                            sender.sendMessage(
                                Message.REPORT_COOLDOWN,
                                TextReplacement(
                                    "time",
                                    ((allowedCooldown - elapsedCooldown).roundToInt() + 1).toString()
                                )
                            )
                            return
                        }
                    }

                    Report(
                        plugin.networkPlayersServer[sender.uniqueId] ?: "Unknown",
                        sender.uniqueId,
                        sender.name,
                        target,
                        System.currentTimeMillis(),
                        MiniMessage.miniMessage().stripTags(formattedReason),
                        true
                    ).update(true).whenComplete { _, _ ->
                        val newCooldownCounter = MilliCounter()
                        newCooldownCounter.start()
                        plugin.cooldowns[sender.uniqueId] = newCooldownCounter
                        sender.sendMessage(
                            Message.REPORT_USE,
                            TextReplacement("player", target),
                            TextReplacement("reason", formattedReason)
                        )
                    }
                }

                else -> {
                    sender.sendMessage(Message.REPORT_USAGE)
                }
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        when (args.size) {
            1 -> {
                return if (Settings.velocitySupport && plugin.networkPlayers.isNotEmpty()) plugin.networkPlayers.filter { it.startsWith(args[0], true) }
                else Ruom.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[0], true) }
            }
            2 -> {
                return Settings.categories.filter { it.enabled && it.id.startsWith(args[1], true) }.map { it.id }
            }
            3 -> {
                return Settings.categories.findLast { it.id.startsWith(args[1], true) }?.reasons?.filter { it.enabled && it.id.startsWith(args[2], true) }?.map { it.id } ?: emptyList()
            }
        }

        return emptyList()
    }
}