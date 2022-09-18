package ir.syrent.velocityreport.spigot.command.report

import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityreport.adventure.ComponentUtils
import ir.syrent.velocityreport.report.Report
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.command.library.PluginCommand
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.sendMessage
import net.kyori.adventure.inventory.Book
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

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

        if (!plugin.networkPlayers.map { it.lowercase() }.contains(args[1].lowercase())) {
            sender.sendMessage(Message.NO_TARGET)
            return
        }

        sender.sendMessage(Message.REPORT_USE)

        sender.openBook(Book.book(
            ComponentUtils.parse("<gradient:dark_red:red>Report</gradient>"),
            ComponentUtils.parse("<gradient:dark_red:red>Report System</gradient>"),
            ComponentUtils.parse(
                "Report Reason:" +
                        "\n" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura" +
                        "\n" +
                        "Killaura"
            ),
        ))
        /*Database.saveReport(
            Report(
                sender.uniqueId,
                sender.name,
                plugin.networkPlayers.first { it.lowercase() == args[1].lowercase() },
                System.currentTimeMillis()
            )
        )*/
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        if (args.size == 1) {
            return plugin.networkPlayers.filter { it.startsWith(args[0], true) }
        }

        return emptyList()
    }
}