package wtf.choco.engarde.internal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.SimpleCheatExemption;

import ac.grim.grimac.events.FlagEvent;

public final class AntiCheatServiceGrim extends InternalAntiCheatService implements Listener {

    /*
     * Grim's API is not very clear on what cheats are available, so we can't register
     * any individual cheats, and any cheat exemption is a global cheat exemption.
     */

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public AntiCheatServiceGrim(Plugin anticheatPlugin) {
        super(anticheatPlugin);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        GrimCheatExemption exemption = handleExemption(player, plugin, () -> new GrimCheatExemption(player, cheatTypes, plugin, reason, duration));
        this.executor.schedule(exemption::expire, duration.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.of(exemption);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        return Optional.of(handleExemption(player, plugin, () -> new GrimCheatExemption(player, cheatTypes, plugin, reason)));
    }

    private GrimCheatExemption handleExemption(Player player, Plugin plugin, Supplier<GrimCheatExemption> exemptionSupplier) {
        GrimCheatExemption exemption = exemptionSupplier.get();
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.computeIfAbsent(player.getUniqueId(), ignore -> MultimapBuilder.hashKeys().arrayListValues().build());
        exemptions.put(plugin, exemption);

        return exemption;
    }

    @Override
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType) {
        return 0.0; // Not well exposed by Grim. Unclear API
    }

    @Override
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason) {
        return false; // Not exposed by Grim
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onViolation(FlagEvent event) {
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(event.getPlayer().getUniqueId());
        if (exemptions == null) {
            return;
        }

        for (CheatExemption exemption : new ArrayList<>(exemptions.values()) /* Avoid ConcurrentModificationException */) {
            if (!exemption.isExpired()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private final class GrimCheatExemption extends SimpleCheatExemption {

        private GrimCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason, Duration duration) {
            super(player, cheatTypes, plugin, reason, Instant.now().plus(duration));
        }

        private GrimCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason) {
            super(player, cheatTypes, plugin, reason);
        }

        @Override
        protected void onExpire() {
            Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(getPlayer().getUniqueId());
            if (exemptions != null) {
                exemptions.remove(getPlugin(), this);
            }
        }

    }

}
