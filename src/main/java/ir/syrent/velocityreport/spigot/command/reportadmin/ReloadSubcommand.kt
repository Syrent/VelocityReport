package ir.syrent.velocityreport.spigot.command.reportadmin

import ir.syrent.velocityreport.spigot.command.library.SubCommand
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.sendMessage
import org.bukkit.command.CommandSender

class ReloadSubcommand : SubCommand("reload", "velocityreport.admin.reload", false) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        Settings.load()
        sender.sendMessage(Message.RELOAD)
    }
}