package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.command.library.SubCommand
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DoneSubcommand : SubCommand("done", "velocityreport.admin.done", true) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            sender.sendMessage(Message.REPORTADMIN_DONE_USAGE)
            return
        }

        Database.getReportByID(args[0], ReportStage.PENDING).whenComplete { report, _ ->
            if (report == null) {
                sender.sendMessage(Message.REPORTADMIN_DONE_ALREADY_DONE, TextReplacement("id", args[0]))
                return@whenComplete
            }

            report.done()
            report.update(true).whenComplete { _, _ ->
                sender.sendMessage(Message.REPORTADMIN_DONE_USE, TextReplacement("id", report.reportID.toString()))
            }
        }
    }
}