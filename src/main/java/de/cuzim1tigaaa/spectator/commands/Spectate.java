package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Main;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Spectate implements CommandExecutor, TabCompleter {

    private final Main instance;

    public Spectate(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                if(!player.hasPermission(Permissions.COMMAND_SPECTATE_GENERAL)) {
                    if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                        player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                        return true;
                    }
                    player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                    return true;
                }
                if(instance.getSpectators().contains(player)) {
                    instance.getMethods().unSpectate(player, false);
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                    return true;
                }
                instance.getMethods().spectate(player, null);
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null || !target.isOnline()) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_YOURSELF));
                return true;
            }
            if(instance.getRelation().get(player) == target) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getDisplayName()));
                return true;
            }
            if(instance.getRelation().get(target) == player || target.hasPermission(Permissions.BYPASS_SPECTATED)) {
                if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_BYPASS, "TARGET", target.getDisplayName()));
                    return true;
                }
            }
            instance.getMethods().spectate(player, target);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getDisplayName()));
            return true;
        }
        if(args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if(player == null) {
                sender.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                instance.getMethods().unSpectate(player, false);
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", player.getDisplayName()));
                return true;
            }
            instance.getMethods().spectate(player, null);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
            sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", player.getDisplayName()));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final List<String> tab = new ArrayList<>();
        if(args.length == 1) for(Player player : Bukkit.getOnlinePlayers()) tab.add(player.getDisplayName());
        return tab;
    }
}