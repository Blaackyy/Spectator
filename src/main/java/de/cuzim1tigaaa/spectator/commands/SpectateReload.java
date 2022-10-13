package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SpectateReload implements CommandExecutor {

    private final Spectator plugin;

    public SpectateReload(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatereload")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(Permissions.COMMANDS_SPECTATE_RELOAD)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        this.plugin.reload();
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_RELOAD));
        return true;
    }
}