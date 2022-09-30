package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.storage.Message
import ir.syrent.velocityreport.spigot.storage.Settings
import net.kyori.adventure.inventory.Book
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendMessage(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    this.playSound(this.location, Settings.commandSound, 1f, 1f)
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendMessageOnly(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendActionbar(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendActionBar(Settings.formatMessage(message, *replacements).component())
}

fun Player.openBook(book: Book) {
    this.playSound(this.location, Settings.bookSound, 1f, 1f)
    AdventureApi.get().sender(this).openBook(book)
}