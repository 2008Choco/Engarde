package wtf.choco.engarde.internal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.engarde.api.CheatCategory;
import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.SimpleCheatExemption;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public final class AntiCheatServiceNoCheatPlus extends InternalAntiCheatService implements Listener {

    private final Map<CheatType, CheckType> cheatTypeToCheckType = new HashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public AntiCheatServiceNoCheatPlus(Plugin anticheatPlugin) {
        super(anticheatPlugin);

        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_BREAK, "Break", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_DIRECTION, "Direction (Block Break)", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_FASTBREAK, "Fast Break", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_FREQUENCY, "Frequency (Block Break)", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_NOSWING, "No Swing (Block Break)", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_REACH, "Reach (Block Break)", "");
        this.registerCheat(CheatCategory.BREAK, CheckType.BLOCKBREAK_WRONGBLOCK, "Wrong Block", "");
        this.registerCheat(CheatCategory.INTERACT, CheckType.BLOCKINTERACT_DIRECTION, "Direction (Block Interact)", "");
        this.registerCheat(CheatCategory.INTERACT, CheckType.BLOCKINTERACT_REACH, "Reach (Block Interact)", "");
        this.registerCheat(CheatCategory.INTERACT, CheckType.BLOCKINTERACT_SPEED, "Speed (Block Interact)", "");
        this.registerCheat(CheatCategory.INTERACT, CheckType.BLOCKINTERACT_VISIBLE, "Visible (Block Interact)", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_AGAINST, "Against (Block Place)", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_AUTOSIGN, "Auto Sign", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_DIRECTION, "Direction (Block Place)", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_FASTPLACE, "Fast Place", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_NOSWING, "No Swing (Block Place)", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_REACH, "Reach (Block Place)", "");
        this.registerCheat(CheatCategory.PLACE, CheckType.BLOCKPLACE_SPEED, "Speed (Block Place)", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_CAPTCHA, "Captcha", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_COLOR, "Color", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_COMMANDS, "Command Spam", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_TEXT, "Text Spam", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_LOGINS, "Login Spam", "");
        this.registerCheat(CheatCategory.CHAT, CheckType.CHAT_RELOG, "Relog", "");
        this.registerCheat(CheatCategory.MISC, CheckType.COMBINED_BEDLEAVE, "Leave Bed", "");
        this.registerCheat(CheatCategory.MISC, CheckType.COMBINED_IMPROBABLE, "Improbable", "");
        this.registerCheat(CheatCategory.MISC, CheckType.COMBINED_MUNCHHAUSEN, "Muchhausen", "");
        this.registerCheat(CheatCategory.MISC, CheckType.COMBINED_YAWRATE, "Yaw Rate", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_ANGLE, "Angle", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_CRITICAL, "Critical", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_DIRECTION, "Direction (Combat)", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_FASTHEAL, "Fast Heal", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_GODMODE, "God Mode", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_NOSWING, "No Swing (Combat)", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_REACH, "Reach (Combat)", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_SELFHIT, "Self Hit", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_SPEED, "Speed (Combat)", "");
        this.registerCheat(CheatCategory.COMBAT, CheckType.FIGHT_WRONGTURN, "Wrong Turn", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_DROP, "Item Drop", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_FASTCLICK, "Fast Click (Inventory)", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_FASTCONSUME, "Fast Consume", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_GUTENBERG, "Gutenberg", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_INSTANTBOW, "Instant Bow", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_INSTANTEAT, "Instant Eat", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_ITEMS, "Items", "");
        this.registerCheat(CheatCategory.INVENTORY, CheckType.INVENTORY_OPEN, "Open Inventory", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_CREATIVEFLY, "Creative Fly", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_MOREPACKETS, "More Packets", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_NOFALL, "No Fall", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_PASSABLE, "Passable", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_SURVIVALFLY, "Survival Fly", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_VEHICLE_MOREPACKETS, "More Packets (Vehicles)", "");
        this.registerCheat(CheatCategory.MOVEMENT, CheckType.MOVING_VEHICLE_ENVELOPE, "Envelope (Vehicles)", "");
        this.registerCheat(CheatCategory.MISC, CheckType.NET_ATTACKFREQUENCY, "Attack Frequency (Net)", "");
        this.registerCheat(CheatCategory.MISC, CheckType.NET_FLYINGFREQUENCY, "Flying Frequency (Net)", "");
        this.registerCheat(CheatCategory.MISC, CheckType.NET_KEEPALIVEFREQUENCY, "Keepalive Frequency (Net)", "");
        this.registerCheat(CheatCategory.MISC, CheckType.NET_PACKETFREQUENCY, "Packet Frequency (Net)", "");
        this.registerCheat(CheatCategory.MISC, CheckType.NET_SOUNDDISTANCE, "Sound Distance (Net)", "");
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        NoCheatPlusCheatExemption exemption = handleExemption(player, plugin, () -> new NoCheatPlusCheatExemption(player, cheatTypes, plugin, reason, duration));
        this.executor.schedule(exemption::expire, duration.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.of(exemption);
    }

    @NotNull
    @Override
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        return Optional.of(handleExemption(player, plugin, () -> new NoCheatPlusCheatExemption(player, cheatTypes, plugin, reason)));
    }

    private NoCheatPlusCheatExemption handleExemption(Player player, Plugin plugin, Supplier<NoCheatPlusCheatExemption> exemptionSupplier) {
        NoCheatPlusCheatExemption exemption = exemptionSupplier.get();
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.computeIfAbsent(player.getUniqueId(), ignore -> MultimapBuilder.hashKeys().arrayListValues().build());
        exemptions.put(plugin, exemption);

        exemption.getCheatTypes().forEach(cheatType -> {
            CheckType checkType = cheatTypeToCheckType.get(cheatType);
            if (checkType != null) {
                NCPExemptionManager.exemptPermanently(player, checkType);
            }
        });

        return exemption;
    }

    @Override
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType) {
        return 0.0; // Not exposed by NoCheatPlus
    }

    @Override
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason) {
        return false; // Not exposed by NoCheatPlus
    }

    protected void registerCheat(CheatCategory category, CheckType matrixCheckType, String cheatName, String cheatDescription) {
        CheatType cheatType = super.registerCheat(category, matrixCheckType.name(), cheatName, cheatDescription);
        this.cheatTypeToCheckType.put(cheatType, matrixCheckType);
    }

    private final class NoCheatPlusCheatExemption extends SimpleCheatExemption {

        private NoCheatPlusCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason, Duration duration) {
            super(player, cheatTypes, plugin, reason, Instant.now().plus(duration));
        }

        private NoCheatPlusCheatExemption(Player player, Collection<CheatType> cheatTypes, Plugin plugin, String reason) {
            super(player, cheatTypes, plugin, reason);
        }

        @Override
        protected void onExpire() {
            Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(getPlayer().getUniqueId());
            if (exemptions != null) {
                exemptions.remove(getPlugin(), this);
            }

            this.getCheatTypes().forEach(cheatType -> {
                CheckType checkType = cheatTypeToCheckType.get(cheatType);
                if (checkType != null) {
                    NCPExemptionManager.unexempt(getPlayer(), checkType);
                }
            });
        }

    }

}
