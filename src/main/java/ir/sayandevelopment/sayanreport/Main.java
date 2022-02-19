package ir.sayandevelopment.sayanreport;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.sayandevelopment.sayanreport.Commands.ReportCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "sayanreport",
        name = "SayanReport",
        version = "1.0",
        url = "sayandevelopment.ir",
        authors = {"Syrent"}
)
public class Main {

    public ProxyServer server;
    public Logger logger;

    public static JDA JDA;
    public static final String PREFIX = "<gradient:#FF0000:#FF2A00>Report</gradient> <color:#555197>| ";

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    @Inject
    public Main(ProxyServer server, Logger logger) {
        instance = this;
        this.server =  server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandMeta reportMeta = server.getCommandManager().metaBuilder("report").build();
        server.getCommandManager().register(reportMeta, new ReportCommand());

        new DiscordManager();



        while (!connect()) {
            try {
                connect();
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        if (connect()) {
            server.getScheduler()
                    .buildTask(this, () -> {
                        JDA.addEventListener(new DiscordManager());
                        server.getEventManager().register(this, new StaffManager());
                    })
                    .delay(3L, TimeUnit.SECONDS)
                    .schedule();
        }
    }

    public boolean connect() {
        try {
            JDA = JDABuilder.createDefault("ODU1NTk2OTYzNDk5MDgxNzI5.YM0yxA.k01a2PBR68T5v5BZ68LY52aO3R8").build();
            return true;
        } catch (LoginException e) {
            e.printStackTrace();
            return false;
        }
    }
}
