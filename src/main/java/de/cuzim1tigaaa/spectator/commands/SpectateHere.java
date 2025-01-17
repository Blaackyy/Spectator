package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SpectateHere implements CommandExecutor {

    private final Spectator plugin;

    public SpectateHere(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatehere")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission(Permissions.COMMAND_SPECTATE_HERE)) {
                if (player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                this.plugin.getSpectateManager().spectate(player, null);
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            this.plugin.getSpectateManager().unSpectate(player, true);
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            return true;
        }
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
        return true;
    }
}