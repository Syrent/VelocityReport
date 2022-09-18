package ir.syrent.velocityreport.spigot.adventure;

import ir.syrent.velocityreport.spigot.Ruom;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class AdventureApi {

    private static BukkitAudiences adventure;

    public static BukkitAudiences get() {
        return adventure;
    }

    public static void initialize() {
        if (adventure == null)
            adventure = BukkitAudiences.create(Ruom.getPlugin());
    }

}
