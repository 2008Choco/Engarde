package wtf.choco.engarde.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;

import wtf.choco.engarde.EngardePlugin;
import wtf.choco.engarde.api.AntiCheatService;
import wtf.choco.engarde.internal.InternalAntiCheatService;

public final class ServiceRegistrationListener implements Listener {

    private final EngardePlugin plugin;

    public ServiceRegistrationListener(EngardePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onRegisterService(ServiceRegisterEvent event) {
        Object service = event.getProvider().getProvider();

        if (service instanceof AntiCheatService) {
            AntiCheatService anticheatService = (AntiCheatService) service;

            String prefix = (service instanceof InternalAntiCheatService) ? "Internal anti cheat" : "Anti cheat";
            this.plugin.getLogger().info(prefix + " service implementation was registered for " + anticheatService.getName() + " version " + anticheatService.getVersion());
        }
    }

}
