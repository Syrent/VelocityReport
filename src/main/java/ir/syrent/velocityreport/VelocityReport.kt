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
import me.mohamad82.ruom.messaging.VelocityMessagingEvent
import me.mohamad82.ruom.utils.MilliCounter
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

}