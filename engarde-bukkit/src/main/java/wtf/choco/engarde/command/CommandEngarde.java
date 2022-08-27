package wtf.choco.engarde.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import wtf.choco.engarde.EngardePlugin;
import wtf.choco.engarde.api.AntiCheatService;
import wtf.choco.engarde.internal.InternalAntiCheatService;

public final class CommandEngarde implements TabExecutor {

    private static final List<String> ARGS_0 = Arrays.asList("anticheats");

    private final EngardePlugin plugin;

    public CommandEngarde(EngardePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            PluginDescriptionFile description = plugin.getDescription();
            sender.sendMessage(plugin.getName() + " version " + description.getVersion() + " by " + userList(description.getAuthors()) + ".");
            return true;
        }

        if (args[0].equalsIgnoreCase("anticheats")) {
            Map<String, RegisteredAntiCheat> anticheats = new HashMap<>();

            Bukkit.getServicesManager().getRegistrations(AntiCheatService.class).forEach(provider -> {
                AntiCheatService service = provider.getProvider();
                anticheats.put(service.getName(), new RegisteredAntiCheat(service, service instanceof InternalAntiCheatService));
            });

            // If there are no supported anti cheats, we need to let the server owner know to ask these anti cheat authors to support us!
            if (anticheats.isEmpty()) {
                sender.sendMessage((sender instanceof Player ? ChatColor.RED.toString() : "")
                    + "There are no supported anti cheats on this server. If you have an anti cheat plugin installed, contact the author(s) and "
                    + "ask them to support " + plugin.getName() + ". This will allow your other plugins to easily support it."
                );

                return true;
            }

            boolean isPlayer = sender instanceof Player;

            sender.sendMessage("Supported anti cheats:");
            anticheats.forEach((anticheatName, anticheat) -> {
                // Only players can be send components
                if (isPlayer) {
                    ComponentBuilder hoverBuilder = new ComponentBuilder("Version: ").color(ChatColor.GRAY)
                            .append(anticheat.service.getVersion()).color(ChatColor.GREEN);

                    if (anticheat.internal) {
                        hoverBuilder.append(
                            "\n\nThis implementation is internal. While it will still work,"
                            + "\nit is implemented by " + plugin.getName() + " and may not be entirely"
                            + "\nfunctional. Speak with the author of " + anticheatName + " to add"
                            + "\nnative support for Engarde."
                        ).color(ChatColor.RED).italic(true);
                    }

                    ComponentBuilder messageBuilder = new ComponentBuilder("- ")
                            .append(anticheatName + " " + anticheat.service.getVersion()).color(ChatColor.GREEN).event(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    hoverBuilder.create()
                            ));

                    if (anticheat.internal) {
                        messageBuilder.append(" (").color(ChatColor.GRAY)
                            .append("internal").color(ChatColor.RED)
                            .append(")").color(ChatColor.GRAY);
                    }

                    ((Player) sender).spigot().sendMessage(messageBuilder.create());
                }

                // Non-players cannot receive chat components
                else {
                    String message = "- " + anticheatName + " " + anticheat.service.getVersion();
                    if (anticheat.internal) {
                        message += " (internal)";
                    }

                    sender.sendMessage(message);
                }
            });

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return (args.length == 1) ? StringUtil.copyPartialMatches(args[0], ARGS_0, new ArrayList<>()) : Collections.emptyList();
    }

    private String userList(List<String> users) {
        int userCount = users.size();

        if (userCount == 0) {
            return "Unknown";
        } else if (userCount == 1) {
            return users.get(0);
        }

        StringJoiner authorsJoiner = new StringJoiner(", ");
        for (int i = 0; i < userCount; i++) {
            String authorName = users.get(i);

            if (i == (userCount - 1)) {
                authorName = "and " + authorName;
            }

            authorsJoiner.add(authorName);
        }

        return authorsJoiner.toString();
    }

    private final class RegisteredAntiCheat {

        private final AntiCheatService service;
        private final boolean internal;

        private RegisteredAntiCheat(AntiCheatService service, boolean internal) {
            this.service = service;
            this.internal = internal;
        }

    }

}
