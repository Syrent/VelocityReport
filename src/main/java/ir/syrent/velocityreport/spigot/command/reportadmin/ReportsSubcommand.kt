package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.adventure.ComponentUtils
import ir.syrent.velocityreport.spigot.command.library.SubCommand
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.*
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReportsSubcommand : SubCommand("reports", "velocityreport.admin.reports", true) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        val title = ComponentUtils.parse("VelocityReport")
        var pageLines = 14

        sender.sendMessage(Message.REPORTADMIN_REPORTS_RECEIVE)

        Database.getReports(ReportStage.ACTIVE).whenComplete { reports, _ ->
            val pages = mutableListOf<Component>()
            val header = Settings.reportsBookHeader
            var lineCount = 0

            pageLines -= header.size

            val page = StringBuilder()

            page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
            for (report in reports) {
                if (lineCount == pageLines) {
                    pages.add(page.toString().component())
                    page.clear()
                    page.append(Settings.formatMessage(header).joinToString("\n")).append("\n")
                    lineCount = 0
                }
                page.append(Settings.formatMessage(
                    Message.REPORTADMIN_REPORTS_FORMAT,
                    TextReplacement("reporter", report.reporterName),
                    TextReplacement("reported", report.reportedName),
                    TextReplacement("server", report.server),
                    TextReplacement("reason", report.reason),
                    TextReplacement("id", report.reportID.toString())
                ).replace("\\n", "\n")).append("\n")
                lineCount++
            }
            pages.add(page.toString().component())

            sender.openBook(Book.book(title, title, pages))
        }
    }
}