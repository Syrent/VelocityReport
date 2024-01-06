package ir.syrent.velocityreport.spigot.storage

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import com.cryptomorin.xseries.XSound
import ir.syrent.velocityreport.report.Category
import ir.syrent.velocityreport.report.Reason
import ir.syrent.velocityreport.report.Report
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.configuration.YamlConfig
import ir.syrent.velocityreport.spigot.hook.DependencyManager
import ir.syrent.velocityreport.utils.TextReplacement
import ir.syrent.velocityreport.utils.component
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object Settings {

    const val latestSettingsConfigVersion = 5
    const val latestLanguageConfigVersion = 4

    lateinit var settings: YamlConfig
    lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()
    val bookHeader = mutableListOf<String>()
    val bookFooter = mutableListOf<String>()
    val reportsBookHeader = mutableListOf<String>()
    val myReportsBookHeader = mutableListOf<String>()

    var settingsConfigVersion = 1
    var languageConfigVersion = 1
    lateinit var defaultLanguage: String
    var showDependencySuggestions = true
    var velocitySupport = true
    var debugMode = false
    var bstats = true

    var commandSound: Sound? = null
    var bookSound: Sound? = null
    var newReportSound: Sound? = null

    var cooldown = 60 * 1000
    var staffActionbarEnabled = true
    var staffActionbarSendZero = false
    var preventSelfReport = true
    var customReason = false
    var mode = Report.Mode.SIMPLE
    val simple = mutableListOf<Reason>()
    var categories = mutableListOf<Category>()
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

        settingsConfigVersion = settingsConfig.getInt("config_version", 1)

        if (settingsConfigVersion < latestSettingsConfigVersion) {
            val backupFileName = "settings.yml-bak-${LocalDate.now()}"
            val settingsFile = File(Ruom.getPlugin().dataFolder, "settings.yml")
            val backupFile = File(Ruom.getPlugin().dataFolder, backupFileName)
            if (backupFile.exists()) backupFile.delete()
            Files.copy(settingsFile.toPath(), backupFile.toPath())
            settingsFile.delete()
            settings = YamlConfig(Ruom.getPlugin().dataFolder, "settings.yml")
            settingsConfig = settings.config
            sendBackupMessage(backupFileName)
        }

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"
//        velocitySupport = settingsConfig.getBoolean("velocity_support")
        showDependencySuggestions = settingsConfig.getBoolean("show_dependency_suggestions")
        debugMode = settingsConfig.getBoolean("debug_mode")
        Ruom.setDebug(debugMode)
        bstats = settingsConfig.getBoolean("bstats")

        commandSound = XSound.valueOf(settingsConfig.getString("sounds.command")!!).parseSound()
        bookSound = XSound.valueOf(settingsConfig.getString("sounds.book")!!).parseSound()
        newReportSound = XSound.valueOf(settingsConfig.getString("sounds.new_report")!!).parseSound()

        cooldown = settingsConfig.getInt("report.cooldown") * 1000
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

        languageConfigVersion = languageConfig.getInt("config_version", 1)

        if (languageConfigVersion < latestLanguageConfigVersion) {
            val backupFileName = "languages/$defaultLanguage.yml-bak-${LocalDate.now()}"
            val languageFile = File(Ruom.getPlugin().dataFolder, "languages/$defaultLanguage.yml")
            val backupFile = File(Ruom.getPlugin().dataFolder, backupFileName)
            if (backupFile.exists()) backupFile.delete()
            Files.copy(languageFile.toPath(), backupFile.toPath())
            languageFile.delete()
            language = YamlConfig(Ruom.getPlugin().dataFolder, "languages/$defaultLanguage.yml")
            languageConfig = language.config
            sendBackupMessage(backupFileName)
        }

        mode = Report.Mode.valueOf(settingsConfig.getString("report.mode")?.uppercase() ?: "NORMAL")

        simple.apply {
            this.clear()
            val reasons = settingsConfig.getConfigurationSection("report.modes.simple") ?: return@apply
            for (reason in reasons.getKeys(false)) {
                val reasonConfig = reasons.getConfigurationSection(reason) ?: continue
                this.add(
                    Reason(
                        reason,
                        "normal",
                        reasonConfig.getBoolean("enabled"),
                        reasonConfig.getString("displayname") ?: reason,
                        reasonConfig.getString("description") ?: ""
                    )
                )
            }
        }

        categories.apply {
            this.clear()
            val categories = settingsConfig.getConfigurationSection("report.modes.category") ?: return@apply
            for (category in categories.getKeys(false)) {
                val categoryConfig = categories.getConfigurationSection(category) ?: continue
                this.add(
                    Category(
                        category,
                        categoryConfig.getBoolean("enabled"),
                        categoryConfig.getString("displayname") ?: "Unknown displayname for $category",
                        let {
                            val reasons = mutableListOf<Reason>()
                            val categoryReasons = categoryConfig.getConfigurationSection("reasons") ?: return@let mutableListOf()
                            for (reason in categoryReasons.getKeys(false)) {
                                val reasonConfig = categoryReasons.getConfigurationSection(reason) ?: continue
                                reasons.add(Reason(reason, category, reasonConfig.getBoolean("enabled"), reasonConfig.getString("displayname") ?: "Unknown displayname for $reason", reasonConfig.getString("description") ?: ""))
                            }
                            reasons
                        }
                    )
                )
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

    fun formatMessage(player: Player, message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = formatMessage(message, *replacements)
        if (DependencyManager.placeholderAPIHook.exists) {
            formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage)
        }
        return formattedMessage
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

    fun formatMessage(player: Player, message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
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

    private fun sendBackupMessage(fileName: String) {
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
        AdventureApi.get().console().sendMessage("<red>Config version updated to $settingsConfigVersion. Please set your prefred values again.".component())
        AdventureApi.get().console().sendMessage("<gray>Previous values are still accessible via $fileName in plugin folder.".component())
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
    }
}