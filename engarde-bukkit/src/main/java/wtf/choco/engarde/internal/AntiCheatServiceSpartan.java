package wtf.choco.engarde.internal;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
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

import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.api.PlayerViolationEvent;
import me.vagdedes.spartan.system.Enums;
import me.vagdedes.spartan.system.Enums.HackType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.engarde.api.CheatCategory;
import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.SimpleCheatExemption;

public final class AntiCheatServiceSpartan extends InternalAntiCheatService implements Listener {

    /*
     * Spartan's API is very strangely named and it's difficult to tell which methods grant a bypass,
     * so we're just going to track bypasses ourselves and use their events.
     */

    private final BiMap<Enums.HackType, CheatType> hackTypeToCheatType = EnumHashBiMap.create(Enums.HackType.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public AntiCheatServiceSpartan(Plugin anticheatPlugin) {
        super(anticheatPlugin);

        this.registerCheat(CheatCategory.MISC, HackType.XRay, "XRay", "");
        this.registerCheat(CheatCategory.MISC, HackType.Exploits, "Exploits", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.EntityMove, "Entity Move", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.NoSwing, "No Swing", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.IrregularMovements, "Irregular Movements", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.ImpossibleActions, "Impossible Actions", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.ItemDrops, "Item Drops", "");
        this.registerCheat(CheatCategory.MISC, HackType.AutoRespawn, "Auto Respawn", "");
        this.registerCheat(CheatCategory.INVENTORY, HackType.InventoryClicks, "Inventory Clicks", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.NoSlowdown, "No Slowdown", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.Criticals, "Criticals", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.GhostHand, "Ghost Hand", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.BlockReach, "Block Reach", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.FastBow, "Fast Bow", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.FastClicks, "Fast Clicks", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.FastHeal, "Fast Heal", "");
        this.registerCheat(CheatCategory.INVENTORY, HackType.ImpossibleInventory, "Impossible Inventory", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.HitReach, "Hit Reach", "");
        this.registerCheat(CheatCategory.BREAK, HackType.FastBreak, "Fast Break", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.Speed, "Speed", "");
        this.registerCheat(CheatCategory.PLACE, HackType.FastPlace, "Fast Place", "");
        this.registerCheat(CheatCategory.MISC, HackType.MorePackets, "More Packets", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.NoFall, "No Fall", "");
        this.registerCheat(CheatCategory.INTERACT, HackType.FastEat, "Fast Eat", "");
        this.registerCheat(CheatCategory.MOVEMENT, HackType.Velocity, "Velocity", "");
        this.registerCheat(CheatCategory.COMBAT, HackType.KillAura, "Kill Aura", "");
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        SpartanCheatExemption exemption = handleExemption(player, plugin, () -> new SpartanCheatExemption(player, cheatTypes, plugin, reason, duration));
        this.executor.schedule(exemption::expire, duration.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.of(exemption);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        return Optional.of(handleExemption(player, plugin, () -> new SpartanCheatExemption(player, cheatTypes, plugin, reason)));
    }

    private SpartanCheatExemption handleExemption(Player player, Plugin plugin, Supplier<SpartanCheatExemption> exemptionSupplier) {
        SpartanCheatExemption exemption = exemptionSupplier.get();
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.computeIfAbsent(player.getUniqueId(), ignore -> MultimapBuilder.hashKeys().arrayListValues().build());
        exemptions.put(plugin, exemption);

        return exemption;
    }

    @Override
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType) {
        return API.getDecimalVL(player, hackTypeToCheatType.inverse().get(cheatType));
    }

    @Override
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason) {
        return false; // Not exposed by Spartan
    }

    protected void registerCheat(CheatCategory category, HackType spartanHackType, String cheatName, String cheatDescription) {
        CheatType cheatType = super.registerCheat(category, spartanHackType.name(), cheatName, cheatDescription);
        this.hackTypeToCheatType.put(spartanHackType, cheatType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onViolation(PlayerViolationEvent event) { // me.vagdedes.spartan.api.PlayerViolationEvent, not Engarde's event
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(event.getPlayer().getUniqueId());
        if (exemptions == null) {
            return;
        }

        CheatType cheatType = hackTypeToCheatType.get(event.getHackType());
        if (cheatType == null) {
            return;
        }

        for (CheatExemption exemption : new ArrayList<>(exemptions.values()) /* Avoid ConcurrentModificationException */) {
            if (exemption.getCheatTypes().contains(cheatType) && !exemption.isExpired()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private final class SpartanCheatExemption extends SimpleCheatExemption {

        private SpartanCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason, Duration duration) {
            super(player, cheatTypes, plugin, reason, Instant.now().plus(duration));
        }

        private SpartanCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason) {
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
