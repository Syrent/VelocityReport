package ir.syrent.velocityreport

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import ir.syrent.velocityreport.bridge.VelocityAdapter
import ir.syrent.velocityreport.bridge.VelocityBridge
import ir.syrent.velocityreport.bridge.VelocityBridgeManager
import ir.syrent.velocityreport.listener.DisconnectListener
import ir.syrent.velocityreport.listener.PostLoginListener
import ir.syrent.velocityreport.listener.ServerConnectedListener
import me.mohamad82.ruom.VRUoMPlugin
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.messaging.VelocityMessagingEvent
import me.mohamad82.ruom.utils.MilliCounter
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger
import java.util.*

class VelocityReport @Inject constructor(server: ProxyServer, logger: Logger) : VRUoMPlugin(server, logger) {

    val cooldowns = mutableMapOf<UUID, MilliCounter>()
    lateinit var bridgeManager: VelocityBridgeManager
        private set

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        initializeMessagingChannels()
        initializeListeners()
        sendFiglet()
    }

    private fun initializeMessagingChannels() {
        val bridge = VelocityBridge()
        val adapter = VelocityAdapter()
        bridgeManager = VelocityBridgeManager(bridge, adapter, cooldowns)
        object : VelocityMessagingEvent(bridge) {
            override fun onPluginMessageReceived(channelMessageSource: ChannelMessageSource, jsonObject: JsonObject) {
                bridgeManager.handleMessage(jsonObject)
            }
        }
    }

    private fun initializeListeners() {
        PostLoginListener(this)
        DisconnectListener(this)
        ServerConnectedListener(this)
    }

    private fun sendFiglet() {
        log(" ")
        log("<dark_red>__     __   _            _ _         ____                       _   ")
        log("<dark_red>\\ \\   / /__| | ___   ___(_) |_ _   _|  _ \\ ___ _ __   ___  _ __| |_ ")
        log("<dark_red> \\ \\ / / _ \\ |/ _ \\ / __| | __| | | | |_) / _ \\ '_ \\ / _ \\| '__| __|")
        log("<dark_red>  \\ V /  __/ | (_) | (__| | |_| |_| |  _ <  __/ |_) | (_) | |  | |_ ")
        log("<dark_red>   \\_/ \\___|_|\\___/ \\___|_|\\__|\\__, |_| \\_\\___| .__/ \\___/|_|   \\__|")
        log("<dark_red>                               |___/          |_|                   v${getServer().pluginManager.getPlugin("velocityreport").get().description.version.get()}")
        log(" ")
        log("<yellow>You should install VelocityReport on your backend servers (Spigot, Paper, Purpur, etc...) as well.")
        log("<yellow>To sync reports data between servers you have to use MySQL as database method.")
        log(" ")
        log("<white>Wiki: <blue><u>https://github.com/Syrent/VelocityReport/wiki</u></blue>")
        log(" ")
    }

    private fun log(message: String) {
        getServer().sendMessage(MiniMessage.miniMessage().deserialize("<gray>$message"))
    }

}