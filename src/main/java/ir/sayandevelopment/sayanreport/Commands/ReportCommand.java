package ir.sayandevelopment.sayanreport.Commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.command.VanishCommand;
import ir.sayandevelopment.sayanreport.DiscordManager;
import ir.sayandevelopment.sayanreport.Main;
import ir.sayandevelopment.sayanreport.Utils.MilliCounter;
import ir.sayandevelopment.sayanreport.Utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.aopalliance.intercept.Invocation;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReportCommand implements SimpleCommand {

    HashMap<String, MilliCounter> playerMilliCounterHashMap = new HashMap<>();

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Only players can run this command!", NamedTextColor.RED));
            return;
        }

        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        if (args.length > 0) {
            String reportedName = args[0];

            if (!Main.getInstance().server.getPlayer(reportedName).isPresent()) {
                player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>Player not found!"));
                return;
            }

            if (Main.getInstance().server.getPlayer(reportedName).isPresent()) {
                Player vanishedPlayer = Main.getInstance().server.getPlayer(reportedName).get();
                if (VelocityMain.SQL.isVanished(vanishedPlayer.getUniqueId())) {
                    player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>Player not found!"));
                    return;
                }
            }

            if (player.getUsername().equalsIgnoreCase(reportedName)) {
                player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>You can't report yourself!"));
                return;
            }

            if (args.length == 1) {
                player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>Please provide report reason."));
                player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#929CCB>Valid reasons:"));
                player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#929CCB>TpKill/Killfarm/Killaura/Scaffold/Reach/CrossTeam/Fly/AntiKB/Others"));
                return;
            }

            String reason = args[1];
            String serverName = player.getCurrentServer().get().getServerInfo().getName();
            if (Arrays.toString(ReportReason.values()).contains(reason.toUpperCase())) {
                MilliCounter milliCounter;
                if (!playerMilliCounterHashMap.containsKey(player.getUsername())) {
                    milliCounter = new MilliCounter();
                    playerMilliCounterHashMap.put(player.getUsername(), milliCounter);
                } else {
                    milliCounter = playerMilliCounterHashMap.get(player.getUsername());
                    milliCounter.stop();
                    if (milliCounter.get() <= 60000) {
                        player.sendMessage(miniMessage.parse(String.format(
                                Main.PREFIX + "<color:#929CCB>Please wait %s second(s) to use the report command again.", (int) ((60000 - milliCounter.get()) / 1000))));
                        return;
                    }
                }
                milliCounter.start();

                player.sendMessage(miniMessage.parse(String.format(
                        Main.PREFIX + "<color:#63ff00>%s successfully reported for %s!", reportedName, reason)));

                for (Player staff : Main.getInstance().server.getAllPlayers()) {
                    if (staff.hasPermission("sayanreport.notify")) {
                        staff.sendMessage(miniMessage.parse("<color:#ff6a00>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                        staff.sendMessage(miniMessage.parse("<color:#63ff00>Reporter: <color:#929CCB>" + player.getUsername()));
                        staff.sendMessage(miniMessage.parse("<color:#63ff00>Reported: <color:#929CCB>" + reportedName));
                        staff.sendMessage(miniMessage.parse("<color:#63ff00>Reason: <color:#929CCB>" + reason.toUpperCase()));
                        staff.sendMessage(miniMessage.parse("<color:#63ff00>Server: <color:#929CCB>" + serverName));
                        staff.sendMessage(miniMessage.parse("<color:#ff6a00>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    }
                }

                DiscordManager.getInstance().sendReportMessage(player.getUsername(), reportedName, reason.toUpperCase(), serverName);
                return;
            }

            player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>Please provide report reason."));
            player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#929CCB>Valid reasons:"));
            player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#929CCB>TpKill/Killfarm/Killaura/Scaffold/Reach/CrossTeam/Fly/AntiKB/Others"));

        }
        player.sendMessage(miniMessage.parse(Main.PREFIX + "<color:#ff6a00>/Report <Player> <Reason>"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> list = new ArrayList<>();

        if (invocation.arguments().length <= 1) {
            for (Player player : Main.getInstance().server.getAllPlayers()) {
                if (VanishCommand.vanishedPlayers.containsKey(player.getUniqueId()) &&
                VanishCommand.vanishedPlayers.get(player.getUniqueId()).isVanished()) continue;
                if (invocation.arguments().length == 0) {
                    list.add(player.getUsername());
                } else {
                    String arg = invocation.arguments()[0];
                    if (player.getUsername().startsWith(arg)) {
                        list.add(player.getUsername());
                    }
                }
            }
        } else {
            for (ReportReason reportReason : ReportReason.values()) {
                String reason = StringUtils.capitalize(reportReason.name());
                list.add(reason);
            }
        }
        return list;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CompletableFuture<List<String>> listCompletableFuture = new CompletableFuture<>();
        List<String> list = new ArrayList<>();

        if (invocation.arguments().length <= 1) {
            for (Player player : Main.getInstance().server.getAllPlayers()) {
                if (VanishCommand.vanishedPlayers.containsKey(player.getUniqueId()) &&
                        VanishCommand.vanishedPlayers.get(player.getUniqueId()).isVanished()) continue;
                if (invocation.arguments().length == 0) {
                    list.add(player.getUsername());
                } else {
                    String arg = invocation.arguments()[0];
                    if (player.getUsername().toLowerCase().startsWith(arg.toLowerCase())) {
                        list.add(player.getUsername());
                    }
                }
            }
        } else {
            for (ReportReason reportReason : ReportReason.values()) {
                String reason = StringUtils.capitalize(reportReason.name());
                list.add(reason);
            }
        }
        listCompletableFuture.complete(list);
        return listCompletableFuture;
    }
}
