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
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import ir.syrent.velocityreport.utils.sendMessage
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.roundToInt

// TODO: Rework command system
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

        if (Settings.mode == Report.Mode.SIMPLE && Settings.customReason && args.size > 1) {
            val reason = args.subList(1, args.size).joinToString(" ").lowercase()
            val formattedReason = Settings.simple.filter { it.enabled }.findLast { it.id.lowercase() == reason.lowercase() }?.displayName ?: reason

            if (!Settings.customReason && !Settings.simple.filter { it.enabled }.map { it.id.lowercase() }.contains(reason.lowercase())) {
                sender.sendMessage(Message.INVALID_REASON, TextReplacement("reason", formattedReason))
                return
            }

            if (plugin.cooldowns.containsKey(sender.uniqueId) && !sender.hasPermission("velocityreport.bypass.cooldown")) {
                val cooldown = plugin.cooldowns[sender.uniqueId]!!
                val allowedCooldown = Settings.cooldown

                if (System.currentTimeMillis() > cooldown + allowedCooldown) {
                    sender.sendMessage(
                        Message.REPORT_COOLDOWN,
                        TextReplacement(
                            "time",
                            ((System.currentTimeMillis() + allowedCooldown - cooldown) / 1000).toString()
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
                plugin.cooldowns[sender.uniqueId] = System.currentTimeMillis()
                sender.sendMessage(
                    Message.REPORT_USE,
                    TextReplacement("player", target),
                    TextReplacement("reason", formattedReason)
                )
            }
            return
        } else if (Settings.mode == Report.Mode.SIMPLE && Settings.customReason) {
            sender.sendMessage(Message.REPORT_NO_REASON_CUSTOM)
            return
        }

        //           1          2        3
        // Report <player> <category> <reason>
        if (args.size <= 3) {
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
                    if (Settings.mode == Report.Mode.CATEGORY) {
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
                    } else {
                        val enabledReasons = Settings.simple.filter { it.enabled }
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
                    }

                    page.append(Settings.formatMessage(footer).joinToString("\n"))
                    pages.add(page.toString().component())

                    sender.openBook(Book.book(title, title, pages))
                }
                2 -> {
                    if (Settings.mode == Report.Mode.CATEGORY) {
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
                    } else {
                        val reason = args.subList(1, args.size).joinToString(" ").lowercase()
                        val formattedReason = Settings.simple.filter { it.enabled }.findLast { it.id.lowercase() == reason.lowercase() }?.displayName ?: reason

                        if (!Settings.customReason && !Settings.simple.filter { it.enabled }.map { it.id.lowercase() }.contains(reason.lowercase())) {
                            sender.sendMessage(Message.INVALID_REASON, TextReplacement("reason", formattedReason))
                            return
                        }

                        if (plugin.cooldowns.containsKey(sender.uniqueId) && !sender.hasPermission("velocityreport.bypass.cooldown")) {
                            val cooldown = plugin.cooldowns[sender.uniqueId]!!
                            val allowedCooldown = Settings.cooldown

                            if (System.currentTimeMillis() > cooldown + allowedCooldown) {
                                sender.sendMessage(
                                    Message.REPORT_COOLDOWN,
                                    TextReplacement(
                                        "time",
                                        ((System.currentTimeMillis() + allowedCooldown - cooldown) / 1000).toString()
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
                            plugin.cooldowns[sender.uniqueId] = System.currentTimeMillis()
                            sender.sendMessage(
                                Message.REPORT_USE,
                                TextReplacement("player", target),
                                TextReplacement("reason", formattedReason)
                            )
                        }
                    }
                }
                3 -> {
                    if (Settings.mode == Report.Mode.CATEGORY) {
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
                            val cooldown = plugin.cooldowns[sender.uniqueId]!!
                            val allowedCooldown = Settings.cooldown

                            if (System.currentTimeMillis() > cooldown + allowedCooldown) {
                                sender.sendMessage(
                                    Message.REPORT_COOLDOWN,
                                    TextReplacement(
                                        "time",
                                        ((System.currentTimeMillis() + allowedCooldown - cooldown) / 1000).toString()
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
                            plugin.cooldowns[sender.uniqueId] = System.currentTimeMillis()
                            sender.sendMessage(
                                Message.REPORT_USE,
                                TextReplacement("player", target),
                                TextReplacement("reason", formattedReason)
                            )
                        }
                    } else {
                        sender.sendMessage(Message.REPORT_USAGE)
                        return
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
                return if (Settings.mode == Report.Mode.CATEGORY) {
                    Settings.categories.filter { it.enabled && it.id.startsWith(args[1], true) }.map { it.id }
                } else {
                    Settings.simple.filter { it.enabled && it.id.startsWith(args[1], true) }.map { it.id }
                }
            }
            3 -> {
                return if (Settings.mode == Report.Mode.CATEGORY) {
                    Settings.categories.findLast { it.id.startsWith(args[1], true) }?.reasons?.filter { it.enabled && it.id.startsWith(args[2], true) }?.map { it.id } ?: emptyList()
                } else {
                    emptyList()
                }
            }
        }

        return emptyList()
    }
}