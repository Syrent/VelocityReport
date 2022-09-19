package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.spigot.command.library.PluginCommand
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.utils.sendMessage
import org.bukkit.command.CommandSender

class ReportAdminCommand : PluginCommand("reportadmin", "velocityreport.admin.use", false) {

    init {
        this.register()

        addSubcommand(ReloadSubcommand())
        addSubcommand(ReportsSubcommand())
        addSubcommand(MyReportsSubcommand())
        addSubcommand(AcceptSubcommand())
        addSubcommand(DoneSubcommand())
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(Message.REPORTADMIN_USAGE)
    }

}