package ir.sayandevelopment.sayanreport;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;

import java.time.LocalDateTime;
import java.util.HashMap;

public class StaffManager {

    HashMap<String, Staff> staff = new HashMap<>();

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("sayanreport.staff.log"))
            staff.put(player.getUsername(), new Staff(player.getUsername(), LocalDateTime.now(), null));
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("sayanreport.staff.log")) {
            staff.put(player.getUsername(), new Staff(player.getUsername(), staff.get(player.getUsername()).getJoinDate(), LocalDateTime.now()));
            DiscordManager.getInstance().sendStaffLogMessage(staff.get(player.getUsername()));
            staff.remove(player.getUsername());
        }
    }

}
