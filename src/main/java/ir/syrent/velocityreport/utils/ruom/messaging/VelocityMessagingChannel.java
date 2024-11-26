package ir.syrent.velocityreport.utils.ruom.messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import ir.syrent.velocityreport.utils.ruom.VRUoMPlugin;
import ir.syrent.velocityreport.utils.ruom.VRuom;
import ir.syrent.velocityreport.utils.ruom.utils.GsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class VelocityMessagingChannel {

    private final Set<VelocityMessagingEvent> messagingEvents = new HashSet<>();
    private final ChannelIdentifier name;

    public VelocityMessagingChannel(String namespace, String name) {
        this.name = MinecraftChannelIdentifier.create(namespace, name);
        VRuom.getServer().getEventManager().register(VRUoMPlugin.get(), this);
        VRuom.getServer().getChannelRegistrar().register(this.name);
    }

    public void register(VelocityMessagingEvent messagingEvent) {
        messagingEvents.add(messagingEvent);
    }

    public void unregister(VelocityMessagingEvent messagingEvent) {
        messagingEvents.remove(messagingEvent);
    }

    @Subscribe
    public void onMessageReceive(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(name)) return; // Not our channel
        event.setResult(PluginMessageEvent.ForwardResult.handled()); // Drop the packet
        if (!(event.getSource() instanceof ServerConnection)) return; // Somebody is trying to hack us

        String rawMessage = new String(event.getData(), StandardCharsets.UTF_8);
        if (rawMessage.isEmpty()) return;
        try {
            JsonObject message = JsonParser.parseString(rawMessage.substring(2)).getAsJsonObject();
            messagingEvents.forEach(messagingEvent -> messagingEvent.onPluginMessageReceived(event.getSource(), message));
        } catch (StringIndexOutOfBoundsException ignored) { }
    }

    public void sendMessage(Player sender, JsonObject message) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
        byteArrayDataOutput.writeUTF(GsonUtils.get().toJson(message));
        sender.sendPluginMessage(name, byteArrayDataOutput.toByteArray());
    }

    public ChannelIdentifier getName() {
        return name;
    }

}
