package wtf.choco.engarde;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import wtf.choco.engarde.api.AntiCheatService;
import wtf.choco.engarde.command.CommandEngarde;
import wtf.choco.engarde.internal.AntiCheatServiceAdvancedAntiCheat;
import wtf.choco.engarde.internal.AntiCheatServiceGrim;
import wtf.choco.engarde.internal.AntiCheatServiceMatrix;
import wtf.choco.engarde.internal.AntiCheatServiceNoCheatPlus;
import wtf.choco.engarde.internal.AntiCheatServiceSpartan;
import wtf.choco.engarde.listener.ServiceRegistrationListener;

public final class EngardePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ServiceRegistrationListener(this), this);

        // Commands
        this.registerCommandSafely("engarde", new CommandEngarde(this));

        // Metrics
        if (getConfig().getBoolean("Metrics", true)) {
            Metrics metrics = new Metrics(this, 0 /* TODO */);

            metrics.addCustomChart(new DrilldownPie("anticheats", () -> {
                Map<String, Map<String, Integer>> anticheats = new HashMap<>();

                /*
                 * Get all registered anti cheat service implementations. If a server is running an anti cheat that we
                 * have already implemented internally, their names should (hopefully) match and override one another
                 * anyways, so there should be no skewed data.
                 *
                 * While unlikely that a server is running more than one anti cheat, it's certainly possible. We should
                 * be checking for all known service registrations.
                 */
                Bukkit.getServicesManager().getRegistrations(AntiCheatService.class).forEach(provider -> {
                    AntiCheatService anticheat = provider.getProvider();

                    Map<String, Integer> versionInfo = Maps.newHashMapWithExpectedSize(1);
                    versionInfo.put(anticheat.getVersion(), 1); // There will only ever be 1 anti cheat with this version installed

                    anticheats.put(anticheat.getName(), versionInfo);
                });

                return anticheats;
            }));
        }

        // Register all default internal implementations
        this.registerServicesIfLoaded(ImmutableMap.of(
                "AAC5", AntiCheatServiceAdvancedAntiCheat::new,
                "GrimAC", AntiCheatServiceGrim::new,
                "Matrix", AntiCheatServiceMatrix::new,
                "NoCheatPlus", AntiCheatServiceNoCheatPlus::new,
                "Spartan", AntiCheatServiceSpartan::new
        ));
    }

    private void registerCommandSafely(String commandString, CommandExecutor executor) {
        PluginCommand command = getCommand(commandString);
        if (command == null) {
            this.getLogger().warning("Tried to register command \"" + commandString + "\" but is not registered in the plugin.yml");
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

    private void registerServiceIfLoaded(String pluginName, Function<Plugin, AntiCheatService> serviceSupplier) {
        Plugin anticheatPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        // If the plugin isn't installed, it will be null
        if (anticheatPlugin == null) {
            return;
        }

        AntiCheatService service = serviceSupplier.apply(anticheatPlugin);
        Bukkit.getServicesManager().register(AntiCheatService.class, service, this, ServicePriority.Lowest);

        if (service instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) service, this);
        }
    }

    private void registerServicesIfLoaded(Map<String, Function<Plugin, AntiCheatService>> services) {
        services.forEach(this::registerServiceIfLoaded);
    }

}
