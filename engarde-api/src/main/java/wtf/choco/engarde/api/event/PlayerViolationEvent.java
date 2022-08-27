package wtf.choco.engarde.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.engarde.api.CheatCategory;
import wtf.choco.engarde.api.CheatType;

/**
 * Called when a {@link Player} has raised a violation for a {@link CheatType}. If this event
 * is cancelled, the violation will not be processed.
 *
 * @apiNote This event may or may not be called by implementations of Engarde's service. This
 * event exists purely as an additional layer of API that may be supplied by the service,
 * however it should not be expected to be called.
 */
public class PlayerViolationEvent extends PlayerEvent implements EngardeEvent, Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final CheatType cheatType;
    private final double violationLevel;

    /**
     * Construct a new {@link PlayerViolationEvent}.
     *
     * @param player the player that raised the violation
     * @param cheatType the cheat type that was violated
     * @param violationLevel the violation level (if applicable)
     */
    public PlayerViolationEvent(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel) {
        super(player);

        this.cheatType = cheatType;
        this.violationLevel = violationLevel;
    }

    /**
     * Get the {@link CheatType} that was violated.
     *
     * @return the cheat type
     */
    @NotNull
    public CheatType getCheatType() {
        return cheatType;
    }

    /**
     * Get the {@link CheatCategory} that was violated.
     * <p>
     * Convenience method. Equivalent to {@code getCheatType().getCategory()}.
     *
     * @return the cheat category
     */
    @NotNull
    public CheatCategory getCheatCategory() {
        return cheatType.getCategory();
    }

    /**
     * Get the violation level of this violation.
     *
     * @return the violation level
     */
    public double getViolationLevel() {
        return violationLevel;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
