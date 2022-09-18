package ir.syrent.velocityreport;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import ir.syrent.velocityreport.bridge.VelocityBridgeManager;
import ir.syrent.velocityreport.bridge.VelocityAdapter;
import ir.syrent.velocityreport.bridge.VelocityBridge;
import ir.syrent.velocityreport.listener.DisconnectListener;
import ir.syrent.velocityreport.listener.PostLoginListener;
import ir.syrent.velocityreport.listener.ServerConnectedListener;
import me.mohamad82.ruom.VRUoMPlugin;
import me.mohamad82.ruom.messaging.VelocityMessagingEvent;
import me.mohamad82.ruom.utils.MilliCounter;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Plugin(
        id = "velocityreport",
        name = "VelocityReport",
        version = BuildConstants.VERSION,
        url = "syrent.ir",
        description = "Report plugin for velocity servers",
        authors = {"Syrent"}
)
public class VelocityReport extends VRUoMPlugin {

    public final Map<UUID, MilliCounter> cooldowns = new HashMap<>();

    private VelocityBridgeManager bridgeManager;

    @Inject
    public VelocityReport(ProxyServer server, Logger logger) {
        super(server, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initializeMessagingChannels();
        initializeListeners();
    }

    private void initializeMessagingChannels() {
        VelocityBridge bridge = new VelocityBridge();
        VelocityAdapter adapter = new VelocityAdapter();
        bridgeManager = new VelocityBridgeManager(bridge, adapter, cooldowns);
        new VelocityMessagingEvent(bridge) {
            @Override
            protected void onPluginMessageReceived(ChannelMessageSource channelMessageSource, JsonObject jsonObject) {
                bridgeManager.handleMessage(jsonObject);
            }
        };
    }

    private void initializeListeners() {
        new PostLoginListener(this);
        new DisconnectListener(this);
        new ServerConnectedListener(this);
    }

    public VelocityBridgeManager getBridgeManager() {
        return bridgeManager;
    }
}
