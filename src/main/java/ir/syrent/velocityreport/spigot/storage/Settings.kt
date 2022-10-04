package ir.syrent.velocityreport.spigot.storage

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import com.cryptomorin.xseries.XSound
import ir.syrent.velocityreport.report.Reason
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.configuration.YamlConfig
import ir.syrent.velocityreport.utils.TextReplacement
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration

object Settings {

    lateinit var settings: YamlConfig
    lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()
    val bookHeader = mutableListOf<String>()
    val bookFooter = mutableListOf<String>()
    val reportsBookHeader = mutableListOf<String>()
    val myReportsBookHeader = mutableListOf<String>()

    lateinit var defaultLanguage: String
    var velocitySupport = false
    var bstats = true

    lateinit var commandSound: Sound
    lateinit var bookSound: Sound
    lateinit var newReportSound: Sound

    var cooldown = 60
    var staffActionbarEnabled = true
    var staffActionbarSendZero = false
    var preventSelfReport = true
    var customReason = false
    var reasons = mutableListOf<Reason>()
    var autoDoneEnabled = true
    var autoDoneTime = 3600
    var autoDoneCallUpdateEvent = false

    var acceptCommandsEnabled = false
    var acceptCommands = mutableListOf<String>()
    var doneCommandsEnabled = false
    var doneCommands = mutableListOf<String>()

    var webhookClient: WebhookClient? = null

    private var discordEnabled = false
    lateinit var discordWebhookURL: String
    lateinit var discordEmbedColor: String
    lateinit var discordEmbedAuthor: String
    lateinit var discordEmbedAuthorIconURL: String
    lateinit var discordEmbedAuthorURL: String
    lateinit var discordEmbedTitle: String
    lateinit var discordEmbedTitleURL: String
    lateinit var discordEmbedDescription: String
    lateinit var discordEmbedImageURL: String
    lateinit var discordEmbedThumbnailURL: String
    lateinit var discordEmbedFooter: String
    lateinit var discordEmbedFooterIconURL: String

    init {
        load()
    }

    fun load() {
        settings = YamlConfig(Ruom.getPlugin().dataFolder, "settings.yml")
        settingsConfig = settings.config

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"
        velocitySupport = settingsConfig.getBoolean("velocity_support")
        bstats = settingsConfig.getBoolean("bstats")

        commandSound = XSound.valueOf(settingsConfig.getString("sounds.command") ?: "ENTITY_EXPERIENCE_ORB_PICKUP").parseSound()!!
        bookSound = XSound.valueOf(settingsConfig.getString("sounds.book") ?: "ENTITY_EXPERIENCE_ORB_PICKUP").parseSound()!!
        newReportSound = XSound.valueOf(settingsConfig.getString("sounds.new_report") ?: "ENTITY_EXPERIENCE_ORB_PICKUP").parseSound()!!

        cooldown = settingsConfig.getInt("report.cooldown")
        staffActionbarEnabled = settingsConfig.getBoolean("report.staff_actionbar.enabled")
        staffActionbarSendZero = settingsConfig.getBoolean("report.staff_actionbar.send_zero")
        preventSelfReport = settingsConfig.getBoolean("report.prevent_self")
        customReason = settingsConfig.getBoolean("report.custom_reason")
        autoDoneEnabled = settingsConfig.getBoolean("report.auto_done.enabled")
        autoDoneTime = settingsConfig.getInt("report.auto_done.time")
        autoDoneCallUpdateEvent = settingsConfig.getBoolean("report.auto_done.call_update_event")

        acceptCommandsEnabled = settingsConfig.getBoolean("report.commands.accept.enabled")
        acceptCommands = settingsConfig.getStringList("report.commands.accept.commands")
        doneCommandsEnabled = settingsConfig.getBoolean("report.commands.done.enabled")
        doneCommands = settingsConfig.getStringList("report.commands.done.commands")

        discordEnabled = settingsConfig.getBoolean("discord.enabled")
        discordWebhookURL = settingsConfig.getString("discord.webhook_url")!!
        discordEmbedColor = settingsConfig.getString("discord.embed.color")!!
        discordEmbedAuthor = settingsConfig.getString("discord.embed.author")!!
        discordEmbedAuthorIconURL = settingsConfig.getString("discord.embed.author_icon_url")!!
        discordEmbedAuthorURL = settingsConfig.getString("discord.embed.author_url")!!
        discordEmbedTitle = settingsConfig.getString("discord.embed.title")!!
        discordEmbedTitleURL = settingsConfig.getString("discord.embed.title_url")!!
        discordEmbedDescription = settingsConfig.getString("discord.embed.description")!!
        discordEmbedImageURL = settingsConfig.getString("discord.embed.image_url")!!
        discordEmbedThumbnailURL = settingsConfig.getString("discord.embed.thumbnail_url")!!
        discordEmbedFooter = settingsConfig.getString("discord.embed.footer")!!
        discordEmbedFooterIconURL = settingsConfig.getString("discord.embed.footer_icon_url")!!

        language = YamlConfig(Ruom.getPlugin().dataFolder, "languages/$defaultLanguage.yml")
        languageConfig = language.config

        reasons.apply {
            this.clear()
            val reasons = settingsConfig.getConfigurationSection("report.reasons") ?: return@apply
            for (reason in reasons.getKeys(false)) {
                val reasonConfig = reasons.getConfigurationSection(reason) ?: return@apply
                this.add(Reason(reason, reasonConfig.getBoolean("enabled"), reasonConfig.getString("displayname", "Unknown")!!, reasonConfig.getString("description", "")!!))
            }
        }

        reportsBookHeader.apply {
            this.clear()
            this.addAll(languageConfig.getStringList("command.reportadmin.reports.book.header"))
        }

        myReportsBookHeader.apply {
            this.clear()
            this.addAll(languageConfig.getStringList("command.reportadmin.myreports.book.header"))
        }

        bookHeader.apply {
            this.clear()
            this.addAll(languageConfig.getStringList("command.report.book.header"))
        }

        bookFooter.apply {
            this.clear()
            this.addAll(languageConfig.getStringList("command.report.book.footer"))
        }

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path) ?: "Cannot find message: ${message.name}"
            }
        }

        webhookClient?.close()
        if (discordEnabled) {
            val discordWebhookBuilder = WebhookClientBuilder(discordWebhookURL)
            discordWebhookBuilder.setThreadFactory {
                val thread = Thread(it)
                thread.name = "VelocityReport Discord Webhook"
                thread.isDaemon = true
                thread
            }
            discordWebhookBuilder.setWait(true)
            webhookClient = discordWebhookBuilder.build()
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }


    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)
        }
        return formattedMessage
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
    }

    fun formatMessage(messages: List<String>, vararg replacements: TextReplacement): List<String> {
        val messageList = mutableListOf<String>()
        for (message in messages) {
            messageList.add(formatMessage(message, *replacements))
        }

        return messageList
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE]?.replace(
            "\$error_prefix",
            messages[Message.ERROR_PREFIX] ?: ""
        ) ?: "Unknown message ($message)"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }
}