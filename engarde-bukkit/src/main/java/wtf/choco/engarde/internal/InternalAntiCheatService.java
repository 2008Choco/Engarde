package wtf.choco.engarde.internal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.engarde.api.AntiCheatService;
import wtf.choco.engarde.api.CheatCategory;
import wtf.choco.engarde.api.CheatExemption;
import wtf.choco.engarde.api.CheatType;
import wtf.choco.engarde.api.event.EngardeEvent;

/**
 * An internal, low-priority {@link AntiCheatService} implementations written by Engarde.
 */
public abstract class InternalAntiCheatService implements AntiCheatService {

    protected final Multimap<CheatCategory, CheatType> cheatTypes = MultimapBuilder.enumKeys(CheatCategory.class).arrayListValues().build();
    protected final Map<String, CheatType> cheatTypeById = new HashMap<>();

    protected final Map<UUID, Multimap<Plugin, CheatExemption>> activeExemptions = new HashMap<>();

    protected final Plugin anticheatPlugin;

    public InternalAntiCheatService(Plugin anticheatPlugin) {
        this.anticheatPlugin = anticheatPlugin;
    }

    @NotNull
    @Override
    public String getName() {
        return anticheatPlugin.getName();
    }

    @NotNull
    @Override
    public String getVersion() {
        return anticheatPlugin.getDescription().getVersion();
    }

    @NotNull
    @UnmodifiableView
    @Override
    public Collection<CheatType> getSupportedCheatTypes() {
        return Collections.unmodifiableCollection(cheatTypes.values());
    }

    @NotNull
    @UnmodifiableView
    @Override
    public Collection<CheatType> getSupportedCheatTypes(@NotNull CheatCategory category) {
        return Collections.unmodifiableCollection(cheatTypes.get(category));
    }

    @NotNull
    @Override
    public Optional<CheatType> getCheatType(@NotNull String id) {
        return Optional.ofNullable(cheatTypeById.get(id));
    }

    @Override
    public boolean isExempt(@NotNull Player player, @NotNull Plugin plugin) {
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(player.getUniqueId());
        if (exemptions == null) {
            return false;
        }

        // Remove any expired exemptions
        Collection<CheatExemption> pluginExemptions = exemptions.get(plugin);
        pluginExemptions.removeIf(CheatExemption::isExpired);

        return !exemptions.isEmpty();
    }

    @Override
    public boolean isExempt(@NotNull Player player) {
        Multimap<Plugin, CheatExemption> exemptions = activeExemptions.get(player.getUniqueId());
        if (exemptions == null) {
            return false;
        }

        // Remove any expired exemptions
        exemptions.values().removeIf(CheatExemption::isExpired);

        return !exemptions.isEmpty();
    }

    @Override
    public boolean callsEvent(@NotNull Class<? extends EngardeEvent> event) {
        return false; // We can't call internal events anywhere, unfortunately
    }

    protected CheatType registerCheat(CheatCategory category, String cheatId, String cheatName, String cheatDescription) {
        CheatType cheatType = new CheatType(cheatId, cheatName, cheatDescription, category);

        this.cheatTypes.put(category, cheatType);
        this.cheatTypeById.put(cheatId, cheatType);

        return cheatType;
    }

}
