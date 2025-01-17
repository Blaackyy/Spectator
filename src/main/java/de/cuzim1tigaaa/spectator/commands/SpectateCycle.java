package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpectateCycle implements CommandExecutor, TabCompleter {

    private final Spectator plugin;

    public SpectateCycle(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatecycle")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
            return true;
        }
        if (!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length < 2) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <Interval>"));
                return true;
            }
            
            if (Bukkit.getAllOnlinePlayers().size() <= 1 && !Config.getBoolean(Paths.CONFIG_CYCLE_NO_PLAYERS)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NOPLAYERS));
                return true;
            }

            if (CycleHandler.isPlayerCycling(player)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING));
                return true;
            }

            try {
                CycleHandler.startCycle(player, Integer.parseInt(args[1]), false);
            } catch (NumberFormatException exception) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NUMBERFORMAT));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            Player target = player;

            if (args.length >= 2 && player.hasPermission(Permissions.COMMANDS_CYCLE_STOP_OTHERS)) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                    return true;
                }
            }

            if (!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                this.plugin.getSpectateManager().unSpectate(player, false);
                return true;
            }

            if (!CycleHandler.isPlayerCycling(target)) {
                String message = Messages.getMessage((player.equals(target) ? Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING : Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING), "TARGET", target.getDisplayName());
                player.sendMessage(message);
                return true;
            }

            CycleHandler.stopCycle(target);
            return true;
        }

        player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) tab = List.of("start", "stop");
        if (args.length == 2 && args[0].equalsIgnoreCase("stop") && sender.hasPermission(Permissions.COMMANDS_CYCLE_STOP_OTHERS))
            for (Player all : Bukkit.getAllOnlinePlayers()) tab.add(all.getDisplayName());
        return tab;
    }
}