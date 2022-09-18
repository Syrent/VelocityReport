package ir.syrent.velocityreport.adventure;

import ir.syrent.velocityreport.spigot.Ruom;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import java.util.regex.Pattern;

public class AdventureApi {

    public static final Pattern escapeTokenPattern = Pattern.compile("((?<start><)(?<token>[^<>]+(:(?<inner>['\"]?([^'\"](\\\\['\"])?)+['\"]?))*)(?<end>>))+?");

    private static BukkitAudiences adventure;

    public static BukkitAudiences get() {
        return adventure;
    }

    public static void initialize() {
        if (adventure == null)
            adventure = BukkitAudiences.create(Ruom.getPlugin());
    }

}
