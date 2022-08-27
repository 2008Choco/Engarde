package wtf.choco.engarde.api;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Represents an exemption from an anti cheat for a {@link Player} to bypass a select set of
 * {@link CheatType CheatTypes}. This exemption may be either permanent or temporary and can
 * be set to automatically expire, or manually expire early. When expired, the player is no
 * longer exempt from the cheat type checks.
 * <p>
 * For developers implementing this interface, see {@link SimpleCheatExemption} for a basic
 * abstract implementation that covers common functionality of an exemption.
 *
 * @see SimpleCheatExemption
 */
public interface CheatExemption {

    /**
     * Get the {@link Player} for which this exemption was granted.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer();

    /**
     * Get a {@link Collection} of the {@link CheatType CheatTypes} being bypassed with this
     * exemption.
     *
     * @return the cheat types
     *
     * @implSpec The returned collection should be unmodifiable. Under no circumstance should a
     * caller be able to manipulate the bypassed cheat types. It is advised to wrap the returned
     * collection with {@link Collections#unmodifiableCollection(Collection)}.
     */
    @NotNull
    @UnmodifiableView
    public Collection<CheatType> getCheatTypes();

    /**
     * Get the {@link Plugin} that requested this exemption.
     *
     * @return the plugin
     */
    @NotNull
    public Plugin getPlugin();

    /**
     * Get the reason for the exemption.
     *
     * @return the reason, or null if no reason provided
     */
    @Nullable
    public String getReason();

    /**
     * Check whether or not this exemption is permanent.
     *
     * @return true if permanent and must be manually expired, false otherwise
     */
    public boolean isPermanent();

    /**
     * Check whether or not this exemption has expired. If expired, the bypass is no longer
     * in effect and players are subject to cheat type checks.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired();

    /**
     * Expire this exemption.
     *
     * @return true if expired, false if this exemption was already expired
     */
    public boolean expire();

}
