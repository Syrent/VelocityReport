package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.command.library.SubCommand
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AcceptSubcommand : SubCommand("accept", "velocityreport.admin.accept", true) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            sender.sendMessage(Message.REPORTADMIN_ACCEPT_USAGE)
            return
        }

        Database.getReportByID(args[0], ReportStage.ACTIVE).whenComplete { report, _ ->
            if (report == null) {
                sender.sendMessage(Message.REPORTADMIN_ACCEPT_ALREADY_ACCEPTED, TextReplacement("id", args[0]))
                return@whenComplete
            }

            report.setModerator(sender)
            report.pending()
            report.update(true).whenComplete { _, _ ->
                sender.sendMessage(Message.REPORTADMIN_ACCEPT_USE, TextReplacement("id", report.reportID.toString()))
            }
        }
    }
}