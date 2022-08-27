package wtf.choco.engarde.api;

import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * A base implementation of {@link CheatExemption} that can be either permanent or temporary.
 * This class implements all methods provided by CheatExemption but also provides a method
 * {@link #onExpire()}, called when {@link #expire()} has been invoked or the exemption has
 * been expired in some other way. This method may be used to remove an exemption from
 * implementation details. An example implementation may look like the following:
 * <pre>
 * public final class MyCheatExemption extends SimpleCheatExemption {
 *
 *     public MyCheatExemption(Player player, Collection{@literal <CheatType>} cheatTypes, Plugin plugin, String reason, Instant expiration) {
 *         super(player, cheatTypes, plugin, reason, expiration);
 *     }
 *
 *     public MyCheatExemption(Player player, Collection{@literal <CheatType>} cheatTypes, Plugin plugin, String reason) {
 *         super(player, cheatTypes, plugin, reason);
 *     }
 *
 *     {@literal @Override}
 *     protected void onExpire() {
 *         // Here you would write functionality that would get called when the exemption expires
 *         SomeInternalAPI.removeExemptionFrom(getPlayer(), this);
 *     }
 *
 * }
 * </pre>
 *
 * @apiNote Callers of the {@link AntiCheatService} interface have no reason to use this
 * class directly. Use of CheatExemption should be preferred under all circumstances, and
 * for no reason should this class be instantiated or extended outside of service
 * implementations.
 * <p>
 * To receive a CheatExemption instance, {@link AntiCheatService#exempt(Player, Collection, Plugin)}
 * or any of its overloads should be used.
 */
public abstract class SimpleCheatExemption implements CheatExemption {

    private boolean expired = false;

    private final Player player;
    private final Collection<CheatType> cheatTypes;
    private final Plugin plugin;
    private final String reason;
    private final Instant expiration;

    /**
     * Construct a new {@link SimpleCheatExemption} that may expire automatically.
     *
     * @param player the exempt player
     * @param cheatTypes the cheat types from which the player is exempt
     * @param plugin the plugin that issued the exemption
     * @param reason the reason for the exemption
     * @param expiration the {@link Instant} at which this exemption will expire, or null
     * if this exemption does not expire automatically
     */
    public SimpleCheatExemption(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason, @Nullable Instant expiration) {
        this.player = player;
        this.cheatTypes = ImmutableSet.copyOf(cheatTypes);
        this.plugin = plugin;
        this.reason = reason;
        this.expiration = expiration;
    }

    /**
     * Construct a new permanent {@link SimpleCheatExemption}.
     *
     * @param player the exempt player
     * @param cheatTypes the cheat types from which the player is exempt
     * @param plugin the plugin that issued the exemption
     * @param reason the reason for the exemption
     */
    public SimpleCheatExemption(@NotNull Player player, @NotNull Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason) {
        this(player, cheatTypes, plugin, reason, null);
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    @NotNull
    @UnmodifiableView
    @Override
    public Collection<CheatType> getCheatTypes() {
        return cheatTypes;
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Nullable
    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public boolean isPermanent() {
        return expiration == null;
    }

    @Override
    public boolean isExpired() {
        // Expire the exemption if not already done
        if (!expired && expiration != null && Instant.now().isAfter(expiration)) {
            this.expire();
        }

        return expired;
    }

    @Override
    public boolean expire() {
        if (isExpired()) {
            return false;
        }

        this.expired = true;
        this.onExpire();
        return true;
    }

    /**
     * Called when this exemption has expired. This method may be used to remove an exemption
     * from implementation details.
     */
    protected abstract void onExpire();

}
