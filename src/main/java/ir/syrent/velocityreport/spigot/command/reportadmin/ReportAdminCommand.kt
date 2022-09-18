package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.spigot.command.library.PluginCommand
import org.bukkit.command.CommandSender

class ReportAdminCommand : PluginCommand("reportadmin", "velocityreport.admin.use", false) {

    init {
        this.register()

        addSubcommand(ReloadSubcommand())
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage("§c/reportadmin reload")
    }

}