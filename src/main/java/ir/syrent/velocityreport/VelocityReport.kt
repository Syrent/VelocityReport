package ir.syrent.velocityreport

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import ir.syrent.velocityreport.bridge.VelocityAdapter
import ir.syrent.velocityreport.bridge.VelocityBridge
import ir.syrent.velocityreport.bridge.VelocityBridgeManager
import ir.syrent.velocityreport.listener.DisconnectListener
import ir.syrent.velocityreport.listener.PostLoginListener
import ir.syrent.velocityreport.listener.ServerConnectedListener
import ir.syrent.velocityreport.utils.ruom.VRUoMPlugin
import ir.syrent.velocityreport.utils.ruom.VRuom
import ir.syrent.velocityreport.utils.ruom.messaging.VelocityMessagingEvent
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.*

class VelocityReport @Inject constructor(
    server: ProxyServer,
    logger: Logger,
    @DataDirectory dataDirectory: Path
) : VRUoMPlugin(server, logger, dataDirectory) {

    val cooldowns = mutableMapOf<UUID, MilliCounter>()
    lateinit var bridgeManager: VelocityBridgeManager
        private set

    @Subscribe
    private fun onProxyInitialization(event: ProxyInitializeEvent) {
        initializeMessagingChannels()
        initializeListeners()
        createFolder()
        sendMessages()
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

    private fun sendMessages() {
        VRuom.log("You should install VelocityReport on your backend servers (Spigot, Paper, Purpur, etc...) as well.")
        VRuom.log("To sync reports data between servers you have to use MySQL as database method.")
        VRuom.log("Wiki: https://github.com/Syrent/VelocityReport/wiki")
    }

    private fun createFolder() {
        val dataFile = getDataDirectory().toFile()
        if (!dataFile.exists()) {
            dataFile.mkdir()
        }
        val noteFile = File(dataFile, "! CONFIG FILES WILL GENERATE ON SPIGOT SERVERS !")
        if (!noteFile.exists()) {
            noteFile.createNewFile()
        }
    }

}