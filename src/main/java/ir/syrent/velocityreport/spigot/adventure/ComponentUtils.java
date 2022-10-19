package ir.syrent.velocityreport.spigot.adventure;

import ir.syrent.velocityreport.spigot.Ruom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class ComponentUtils {

    static {
        Ruom.initializeAdventure();
    }

    public static void send(Player player, Component component) {
        AdventureApi.get().player(player).sendMessage(component);
    }

    public static void send(Component component, Player... players) {
        for (Player player : players) {
            send(player, component);
        }
    }

    public static Component parse(String string) {
        return MiniMessage.miniMessage().deserialize(parseComponentColors(string));
    }

    public static String parseComponentColors(String msg) {
        return msg.replace("&0", "<black>")
                .replace("&1", "<red>")
                .replace("&2", "<dark_green>")
                .replace("&3", "red")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<red>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")
                .replace("&k", "<obfuscated>");
    }

}