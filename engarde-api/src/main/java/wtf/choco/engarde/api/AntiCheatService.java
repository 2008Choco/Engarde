package wtf.choco.engarde.api;

import com.google.common.util.concurrent.ServiceManager;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.engarde.api.event.EngardeEvent;

/**
 * A {@link ServiceManager Bukkit service} representing a plugin providing anti-cheat functionality.
 *
 * <h2>Anti Cheat Developers</h2>
 * Every anti cheat enabled on the server should register to Bukkit's {@link ServiceManager} an
 * instance of a class that implements this service interface. While anti cheats differ vastly from
 * plugin to plugin, it is expected that every implementation of this service make a best effort
 * attempt at properly implementing each method according to its specification.
 * <p>
 * In order to register this service, simply create an instance of the service implementation and
 * register it to Bukkit's service manager. It is strongly advised that a service should be instantiated
 * and registered when the anti cheat is <em>loaded</em> (not <em>enabled</em>) via JavaPlugin's
 * {@link JavaPlugin#onLoad()} method. This will ensure that dependent plugins will be able to access the
 * anti cheat service when <em>they</em> are enabled. An example of a registration may be found below:
 * <pre>
 * public final class MyAntiCheat extends JavaPlugin {
 *
 *     {@literal @Override}
 *     public void onLoad() {
 *         // You can use whichever priority you desire, but "normal" priority should suffice
 *         Bukkit.getServicesManager().register(AntiCheatService.class, new AntiCheatServiceImplementation(), this, ServicePriority.Normal);
 *     }
 *
 * }
 * </pre>
 * Engarde will attempt rudimentary implementation for as many anti cheats as reasonably possible for
 * those that have public, dependable artifacts. These implementations will be registered at
 * {@link ServicePriority#Lowest} to ensure that anti cheats can supply their own implementations that
 * take priority over the basic ones provided by Engarde.
 *
 * <h2>Dependent Developers</h2>
 * Depending and using an available AntiCheatService implementation is as easy as fetching it from
 * Bukkit's {@link ServiceManager}. While developers should be depending on Engarde to ensure that it
 * is enabled before, there is no guarantee that any given anti cheat has registered a service on load
 * as has been advised. Therefore, out of an abundance of caution, in order to ensure that an instance
 * of this service is correctly received, developers should be expected to lazy fetch the service.
 * <p>
 * All things considered, a common means to fetch an AntiCheatService may look like the following:
 * <pre>
 * public final class MyAntiCheat extends JavaPlugin {
 *
 *     private AntiCheatService antiCheatService = null;
 *
 *     public AntiCheatService getAntiCheatService() {
 *         // If the service is not yet instantiated, we're going to try and get it from Bukkit's service manager
 *         if (antiCheatService == null) {
 *             this.antiCheatService = Bukkit.getServicesManager().load(AntiCheatService.class);
 *
 *             /* Alternatively, you could use the following instead of the above to send a message if
 *              * no registered services are available...
 *
 *             RegisteredServiceProvider{@literal <AntiCheatService>} provider = Bukkit.getServicesManager().getRegistration(AntiCheatService.class);
 *             if (provider == null) {
 *                 this.getLogger().info("No Engarde anti cheat service found.");
 *                 return null;
 *             }
 *
 *             this.antiCheatService = provider.getProvider();
 *             {@literal *}/
 *         }
 *
 *         return antiCheatService;
 *     }
 *
 * }
 * </pre>
 * With the snippet above, calling {@code getAntiCheatService()} should find and load any implementation
 * of the AntiCheatService and allow the use of this service's methods.
 *
 * @apiNote While not all anti cheats may implement the interface, Engarde will attempt to provide a
 * minimal effort implementation of most anti cheats with a public, dependable artifact. Additionally,
 * there is no guarantee that any implementation of this interface will function exactly to this
 * service's specification. Every anti cheat is different and may or may not support the broad
 * functionality that this service seeks to provide.
 */
public interface AntiCheatService {

    /**
     * Get the name of the anti cheat with which this service is interfacing.
     *
     * @return the anti cheat name
     *
     * @implSpec This should return the name of the anti cheat, <strong>NOT</strong> the name of the
     * service
     */
    @NotNull
    public String getName();

    /**
     * Get the version of the anti cheat with which this service is interfacing.
     *
     * @return the anti cheat version
     *
     * @implSpec This should return the version of the anti cheat, <strong>NOT</strong> the version
     * of the service
     */
    @NotNull
    public String getVersion();

    /**
     * Get a {@link Collection} of all {@link CheatType CheatTypes} supported by this anti cheat.
     * The returned collection is immutable and cannot be modified.
     *
     * @return all supported cheat types
     *
     * @implSpec The returned collection should be unmodifiable. Under no circumstance should a
     * caller be able to manipulate the supported cheat types. It is advised to wrap the returned
     * collection with {@link Collections#unmodifiableCollection(Collection)}.
     */
    @NotNull
    @UnmodifiableView
    public Collection<CheatType> getSupportedCheatTypes();

    /**
     * Get a {@link Collection} of all {@link CheatType CheatTypes} supported by this anti cheat
     * under the given {@link CheatCategory}. The returned collection is immutable and cannot be
     * modified.
     *
     * @param category the category for which to get supported cheat types
     *
     * @return all supported cheat types under the given category
     *
     * @implSpec The returned collection should be unmodifiable. Under no circumstance should a
     * caller be able to manipulate the supported cheat types. It is advised to wrap the returned
     * collection with {@link Collections#unmodifiableCollection(Collection)}.
     */
    @NotNull
    @UnmodifiableView
    public Collection<CheatType> getSupportedCheatTypes(@NotNull CheatCategory category);

    /**
     * Get a {@link CheatType} by its id.
     *
     * @param id the id of the cheat
     *
     * @return an optional containing the cheat type, or an empty optional if one did not exist
     * with the given id
     */
    @NotNull
    public Optional<CheatType> getCheatType(@NotNull String id);

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatType
     * CheatTypes}. Players with an exemption will not be flagged for the provided cheat types such
     * that the exemption remains in effect. This is a temporary exemption and will automatically
     * expire, but can still be expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatTypes the cheat types from which the player should be exempt. The passed Collection
     * will not be modified by the implementation
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     * @param reason the reason for the exemption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     *
     * @implSpec Under no circumstance should the {@code cheatTypes} collection be modified. The
     * collection may, however, be empty, in which case the anti cheat should not provide an exemption.
     */
    @NotNull
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull final Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason);

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatType
     * CheatTypes}. Players with an exemption will not be flagged for the provided cheat types such
     * that the exemption remains in effect. This is a temporary exemption and will automatically
     * expire, but can still be expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatTypes the cheat types from which the player should be exempt. The passed Collection
     * will not be modified by the implementation
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     *
     * @implSpec Under no circumstance should the {@code cheatTypes} collection be modified. The
     * collection may, however, be empty, in which case the anti cheat should not provide an exemption.
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull final Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @NotNull Duration duration) {
        return exempt(player, cheatTypes, plugin, duration, null);
    }

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatCategory}.
     * Players with an exemption will not be flagged for the provided cheat types such that the exemption
     * remains in effect. This is a temporary exemption and will automatically expire, but can still be
     * expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. One such reason includes if the provided {@code category} is not supported by this service
     * and does not have any associated {@link CheatType CheatTypes} (i.e.
     * {@link #getSupportedCheatTypes(CheatCategory)} returns an empty {@link Collection}). In such a
     * situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param category the category from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     * @param reason the reason for the exemption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatCategory category, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        return exempt(player, getSupportedCheatTypes(category), plugin, duration, reason);
    }

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatCategory}.
     * Players with an exemption will not be flagged for the provided cheat types such that the exemption
     * remains in effect. This is a temporary exemption and will automatically expire, but can still be
     * expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. One such reason includes if the provided {@code category} is not supported by this service
     * and does not have any associated {@link CheatType CheatTypes} (i.e.
     * {@link #getSupportedCheatTypes(CheatCategory)} returns an empty {@link Collection}). In such a
     * situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param category the category from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatCategory category, @NotNull Plugin plugin, @NotNull Duration duration) {
        return exempt(player, getSupportedCheatTypes(category), plugin, duration);
    }

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatType
     * CheatType}. Players with an exemption will not be flagged for the provided cheat type such
     * that the exemption remains in effect. This is a temporary exemption and will automatically
     * expire, but can still be expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatType the cheat type from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     * @param reason the reason for the exemption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatType cheatType, @NotNull Plugin plugin, @NotNull Duration duration, @Nullable String reason) {
        return exempt(player, Arrays.asList(cheatType), plugin, duration, reason);
    }

    /**
     * Grant a temporary exemption to the specified {@link Player} for the given {@link CheatType
     * CheatType}. Players with an exemption will not be flagged for the provided cheat type such
     * that the exemption remains in effect. This is a temporary exemption and will automatically
     * expire, but can still be expired early.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatType the cheat type from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param duration the duration of time for which the exemption should last
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatType cheatType, @NotNull Plugin plugin, @NotNull Duration duration) {
        return exempt(player, cheatType, plugin, duration, null);
    }

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatType
     * CheatTypes}. Players with an exemption will not be flagged for the provided cheat types such
     * that the exemption remains in effect. This is a permanent exemption and must be manually
     * expired with {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption or to
     * track information related to the exemption such as the expiration status or any information
     * passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatTypes the cheat types from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param reason the reason for the exmption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     *
     * @implSpec Under no circumstance should the {@code cheatTypes} collection be modified. The
     * collection may, however, be empty, in which case the anti cheat should not provide an exemption.
     */
    @NotNull
    public Optional<CheatExemption> exempt(@NotNull Player player, @NotNull final Collection<CheatType> cheatTypes, @NotNull Plugin plugin, @Nullable String reason);

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatType
     * CheatTypes}. Players with an exemption will not be flagged for the provided cheat types such
     * that the exemption remains in effect. This is a permanent exemption and must be manually
     * expired with {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption or to
     * track information related to the exemption such as the expiration status or any information
     * passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatTypes the cheat types from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     *
     * @implSpec Under no circumstance should the {@code cheatTypes} collection be modified. The
     * collection may, however, be empty, in which case the anti cheat should not provide an exemption.
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull final Collection<CheatType> cheatTypes, @NotNull Plugin plugin) {
        return exempt(player, cheatTypes, plugin, (String) null);
    }

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatCategory}.
     * Players with an exemption will not be flagged for the provided cheat types such that the exemption
     * remains in effect. This is a permanent exemption and must be manually expired with
     * {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. One such reason includes if the provided {@code category} is not supported by this service
     * and does not have any associated {@link CheatType CheatTypes} (i.e.
     * {@link #getSupportedCheatTypes(CheatCategory)} returns an empty {@link Collection}). In such a
     * situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param category the category from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param reason the reason for the exemption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatCategory category, @NotNull Plugin plugin, @Nullable String reason) {
        return exempt(player, getSupportedCheatTypes(category), plugin, reason);
    }

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatCategory}.
     * Players with an exemption will not be flagged for the provided cheat types such that the exemption
     * remains in effect. This is a permanent exemption and must be manually expired with
     * {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption early
     * should the need arise, or to track information related to the exemption such as the expiration
     * status or any information passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. One such reason includes if the provided {@code category} is not supported by this service
     * and does not have any associated {@link CheatType CheatTypes} (i.e.
     * {@link #getSupportedCheatTypes(CheatCategory)} returns an empty {@link Collection}). In such a
     * situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param category the category from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatCategory category, @NotNull Plugin plugin) {
        return exempt(player, getSupportedCheatTypes(category), plugin);
    }

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatType
     * CheatType}. Players with an exemption will not be flagged for the provided cheat type such
     * that the exemption remains in effect. This is a permanent exemption and must be manually
     * expired with {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption or to
     * track information related to the exemption such as the expiration status or any information
     * passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatType the cheat type from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     * @param reason the reason for the exemption. May be null
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatType cheatType, @NotNull Plugin plugin, @Nullable String reason) {
        return exempt(player, Arrays.asList(cheatType), plugin, reason);
    }

    /**
     * Grant a permanent exemption to the specified {@link Player} for the given {@link CheatType
     * CheatType}. Players with an exemption will not be flagged for the provided cheat type such
     * that the exemption remains in effect. This is a permanent exemption and must be manually
     * expired with {@link CheatExemption#expire()}.
     * <p>
     * The {@link CheatExemption} returned by this method may be used to expire the exemption or to
     * track information related to the exemption such as the expiration status or any information
     * passed by this method.
     * <p>
     * There is no guarantee that this service will grant an exemption to the player for whatever
     * reason. In such a situation, the returned {@link Optional} will be empty.
     *
     * @param player the player for whom to grant an exemption
     * @param cheatType the cheat type from which the player should be exempt
     * @param plugin the plugin requesting the exemption
     *
     * @return an optional containing the cheat exemption, or an empty optional if an exemption
     * could not be granted
     *
     * @implNote Implementations are not required to grant exemptions. If, for whatever reason, an
     * exemption is rejected, an {@link Optional#empty() empty Optional} should be returned. It is
     * expected that anti cheats grant exemptions under as many situations as possible
     */
    @NotNull
    public default Optional<CheatExemption> exempt(@NotNull Player player, @NotNull CheatType cheatType, @NotNull Plugin plugin) {
        return exempt(player, cheatType, plugin, (String) null);
    }

    /**
     * Check whether or not the given {@link Player} has an active exemption from the given plugin.
     *
     * @param player the player to check
     * @param plugin the plugin that granted the exemption
     *
     * @return true if the player is currently exempted by the given plugin, false otherwise
     */
    public boolean isExempt(@NotNull Player player, @NotNull Plugin plugin);

    /**
     * Check whether or not the given {@link Player} has an active exemption.
     *
     * @param player the player to check
     *
     * @return true if the player is currently exempted, false otherwise
     */
    public boolean isExempt(@NotNull Player player);

    /**
     * Get the violation level for the given {@link Player} under the provided {@link CheatType}.
     *
     * @param player the player whose violation level to get
     * @param cheatType the cheat type
     *
     * @return the violation level
     *
     * @apiNote There is no standard definition of "violation level". This is entirely dependent
     * from implementation to implementation.
     */
    public double getViolationLevel(@NotNull Player player, @NotNull CheatType cheatType);

    /**
     * Raise a {@link CheatType} violation for the given {@link Player} with a supplied reason.
     *
     * @param player the player that violated the cheat type
     * @param cheatType the cheat type that was violated
     * @param violationLevel the violation level
     * @param reason the reason for the violation. May be null
     *
     * @return true if the violation was raised successfully, false otherwise
     */
    public boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel, @Nullable String reason);

    /**
     * Raise a {@link CheatType} violation for the given {@link Player} with no supplied reason.
     *
     * @param player the player that violated the cheat type
     * @param cheatType the cheat type that was violated
     * @param violationLevel the violation level
     *
     * @return true if the violation was raised successfully, false otherwise
     *
     * @see #raiseViolation(Player, CheatType, double, String)
     */
    public default boolean raiseViolation(@NotNull Player player, @NotNull CheatType cheatType, double violationLevel) {
        return raiseViolation(player, cheatType, violationLevel, null);
    }

    /**
     * Check whether or not this anti cheat implementation calls the provided {@link EngardeEvent}.
     *
     * @param event the event class to check
     *
     * @return true if the event is called, false otherwise
     *
     * @implNote Because events in the {@link wtf.choco.engarde.api.event} package are optional
     * (although implementations are strongly advised to comply and call), this method acts as a
     * way for callers to check if the specified event is being called by the implementation.
     * This method is not a binding contract, but if an implementation is calling one of the events
     * in the aforementioned package, it is expected that true is returned for the specific event
     * being called. Additionally, it is expected that this method returns false for any event that
     * is <strong>not</strong> being called.
     * <p>
     * It is not required for an implementation to call <em>all</em> events in the event package
     * (especially for features that are not supported), therefore this method may return a value
     * for each individual event.
     */
    public boolean callsEvent(@NotNull Class<? extends EngardeEvent> event);

}
