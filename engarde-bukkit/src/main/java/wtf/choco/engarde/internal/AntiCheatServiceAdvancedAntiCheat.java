package wtf.choco.engarde.internal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.AACExemption;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.SimpleCheatExemption;

public final class AntiCheatServiceAdvancedAntiCheat extends InternalAntiCheatService {

    /*
     * No in-built CheatType provided by AAC. Could not find them.
     * Doesn't matter though because AAC doesn't discriminate cheat types. An exemption is a total bypass.
     *
     * Consequently, AAC has been discontinued and its API is entirely broken. This doesn't even work.
     * This service will remain registered in the hopes that perhaps in the future someone will maintain
     * AAC5 and resolve the issues with the API, though hopefully by that point they'll have implemented
     * Engarde natively anyways.
     */

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private AACAPI aacAPI;

    public AntiCheatServiceAdvancedAntiCheat(Plugin anticheatPlugin) {
        super(anticheatPlugin);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        AACCheatExemption exemption = handleExemption(player, reason, plugin, aacExemption -> new AACCheatExemption(aacExemption, player, plugin, reason, duration));
        this.executor.schedule(exemption::expire, duration.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.of(exemption);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        return Optional.of(handleExemption(player, reason, plugin, aacExemption -> new AACCheatExemption(aacExemption, player, plugin, reason)));
    }

    private AACCheatExemption handleExemption(Player player, String reason, Plugin plugin, Function<AACExemption, AACCheatExemption> exemptionSupplier) {
        AACExemption aacExemption = new AACExemption(reason);
        this.getAACAPI().addExemption(player, aacExemption); // Register the exemption to AAC

        AACCheatExemption exemption = exemptionSupplier.apply(aacExemption);
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.computeIfAbsent(player.getUniqueId(), ignore -> MultimapBuilder.hashKeys().arrayListValues().build());
        exemptions.put(plugin, exemption);

        return exemption;
    }

    @Override
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType) {
        return 0; // Not exposed by AAC
    }

    @Override
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason) {
        return false; // Not exposed by AAC
    }

    private AACAPI getAACAPI() {
        return (aacAPI != null) ? aacAPI : (aacAPI = Bukkit.getServicesManager().load(AACAPI.class));
    }

    private final class AACCheatExemption extends SimpleCheatExemption {

        private final AACExemption aacExemption;

        private AACCheatExemption(AACExemption aacExemption, Player player, Plugin plugin, String reason, Duration duration) {
            super(player, Collections.emptyList(), plugin, reason, Instant.now().plus(duration));
            this.aacExemption = aacExemption;
        }

        private AACCheatExemption(AACExemption aacExemption, Player player, Plugin plugin, String reason) {
            super(player, Collections.emptyList(), plugin, reason);
            this.aacExemption = aacExemption;
        }

        @Override
        protected void onExpire() {
            getAACAPI().removeExemption(getPlayer(), aacExemption);

            Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(getPlayer().getUniqueId());
            if (exemptions != null) {
                exemptions.remove(getPlugin(), this);
            }
        }

    }

}
