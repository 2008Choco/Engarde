package wtf.choco.engarde.internal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import me.rerere.matrix.api.HackType;
import me.rerere.matrix.api.MatrixAPIProvider;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.engarde.api.CheatCategory;
import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.SimpleCheatExemption;

public final class AntiCheatServiceMatrix extends InternalAntiCheatService implements Listener {

    private final Map<CheatType, HackType> cheatTypeToHackType = new HashMap<>();

    public AntiCheatServiceMatrix(Plugin anticheatPlugin) {
        super(anticheatPlugin);

        this.registerCheat(CheatCategory.COMBAT, HackType.KILLAURA, "Kill Aura", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.HITBOX, "Hitbox", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.SPEED, "Speed", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.FLY, "Fly", "");
        this.registerCheat(CheatCategory.MISC, HackType.BADPACKETS, "Bad Packets", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.FASTUSE, "Fast Use", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.FASTBOW, "Fast Bow", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.FASTHEAL, "Fast Heal", "");
        this.registerCheat(CheatCategory.BREAK, HackType.BLOCK, "Block", "");
        this.registerCheat(CheatCategory.PLACE, HackType.SCAFFOLD, "Scaffold", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.JESUS, "Jesus", "");
        this.registerCheat(CheatCategory.INVENTORY, HackType.INVENTORY, "Inventory", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.VELOCITY, "Velocity", "");
        this.registerCheat(CheatCategory.CHAT, HackType.CHAT, "Chat", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.INTERACT, "Interact", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.AUTOFISH, "Autofish", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.PHASE, "Phase", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.AUTOBOT, "Autobot", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.ELYTRA, "Elytra", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.VEHICLE, "Vehicle", "");
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        return Optional.of(handleExemption(player, plugin, duration, () -> new MatrixCheatExemption(player, cheatTypes, plugin, reason, duration)));
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        return Optional.of(handleExemption(player, plugin, null, () -> new MatrixCheatExemption(player, cheatTypes, plugin, reason)));
    }

    private MatrixCheatExemption handleExemption(Player player, Plugin plugin, Duration duration, Supplier<MatrixCheatExemption> exemptionSupplier) {
        MatrixCheatExemption exemption = exemptionSupplier.get();
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.computeIfAbsent(player.getUniqueId(), ignore -> MultimapBuilder.hashKeys().arrayListValues().build());
        exemptions.put(plugin, exemption);

        // Matrix only supports temporary exemptions via their API
        long durationMillis = (duration != null) ? duration.toMillis() : Long.MAX_VALUE;
        exemption.getCheatTypes().forEach(cheatType -> {
            HackType hackType = cheatTypeToHackType.get(cheatType);
            if (hackType != null) {
                MatrixAPIProvider.getAPI().tempBypass(player, hackType, durationMillis);
            }
        });

        return exemption;
    }

    @Override
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType) {
        HackType hackType = cheatTypeToHackType.get(cheatType);
        if (hackType == null) {
            return 0.0;
        }

        return MatrixAPIProvider.getAPI().getViolations(player, hackType);
    }

    @Override
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason) {
        MatrixAPIProvider.getAPI().flag(player, cheatTypeToHackType.get(cheatType), reason, "", (int) Math.round(violationLevel));
        return true;
    }

    protected void registerCheat(CheatCategory category, HackType matrixHackType, String cheatName, String cheatDescription) {
        CheatType cheatType = super.registerCheat(category, matrixHackType.name(), cheatName, cheatDescription);
        this.cheatTypeToHackType.put(cheatType, matrixHackType);
    }

    private final class MatrixCheatExemption extends SimpleCheatExemption {

        private MatrixCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason, Duration duration) {
            super(player, cheatTypes, plugin, reason, Instant.now().plus(duration));
        }

        private MatrixCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason) {
            super(player, cheatTypes, plugin, reason);
        }

        @Override
        protected void onExpire() {
            Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(getPlayer().getUniqueId());
            if (exemptions != null) {
                exemptions.remove(getPlugin(), this);
            }

            // If the exemption was permanent, we need to remove the bypass
            if (isPermanent()) {
                this.getCheatTypes().forEach(cheatType -> {
                    HackType hackType = cheatTypeToHackType.get(cheatType);
                    if (hackType != null) {
                        // Set the bypass time to zero so it can be removed
                        MatrixAPIProvider.getAPI().tempBypass(getPlayer(), hackType, 0L);
                    }
                });
            }
        }

    }

}
