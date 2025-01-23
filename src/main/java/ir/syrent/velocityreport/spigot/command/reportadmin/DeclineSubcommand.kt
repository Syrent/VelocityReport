package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.command.library.SubCommand
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeclineSubcommand : SubCommand("decline", "velocityreport.admin.decline", true) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            sender.sendMessage(Message.REPORTADMIN_DECLINE_USAGE)
            return
        }

        Database.getReportByID(args[0], ReportStage.ACTIVE).whenComplete { report, _ ->
            if (report == null) {
                Database.getLastReportByReported(args[0]).whenComplete { report, _ ->
                    report?.let { report ->
                        report.decline(sender)
                        report.update(true).whenComplete { _, _ ->
                            sender.sendMessage(Message.REPORTADMIN_DECLINE_USE, TextReplacement("id", report.reportID.toString()))
                        }
                    } ?: let {
                        sender.sendMessage(Message.REPORTADMIN_DECLINE_ALREADY_DECLINED, TextReplacement("id", args[0]))
                        return@whenComplete
                    }
                }
            } else {
                report.decline(sender)
                report.update(true).whenComplete { _, _ ->
                    sender.sendMessage(Message.REPORTADMIN_DECLINE_USE, TextReplacement("id", report.reportID.toString()))
                }
            }
        }
    }
}