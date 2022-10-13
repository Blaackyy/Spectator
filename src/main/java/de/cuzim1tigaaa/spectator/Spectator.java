package de.cuzim1tigaaa.spectator;

import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.listener.PlayerListener;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class Spectator extends JavaPlugin {

    private SpectateManager spectateManager;
    private final Set<Player> spectators = new HashSet<>();

    private final HashMap<Player, Player> relation = new HashMap<>();

    public HashMap<Player, Player> getRelation() {
        return relation;
    }

    public Set<Player> getSpectators() {
        return spectators;
    }

    public SpectateManager getSpectateManager() {
        return spectateManager;
    }

    @Override
    public void onEnable() {
        spectateManager = new SpectateManager(this);
        info();
        register();
    }

    private void info() {
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        getLogger().info("Plugin: " + getDescription().getName() + ", " +
                "v" + getDescription().getVersion() + " by " + getDescription().getAuthors().get(0));
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        getLogger().info("This Plugin is a modified Version");
        getLogger().info("of kosakriszi's spectator Plugin!");
        getLogger().info("spigotmc.org/resources/spectator.16745/");
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }

    private void register() {
        reload();
        getLogger().info("Register Events & Commands...");
        new PlayerListener(this);

        new Spectate(this);
        new SpectateCycle(this);
        new SpectateHere(this);
        new SpectateReload(this);
        new SpectateList(this);
        new UnSpectate(this);
    }

    public void reload() {
        getLogger().info("Loading config settings...");
        Config.loadConfig(this);
        getLogger().info("Loading plugin messages...");
        Messages.loadLanguageFile(this);
    }

    @Override
    public void onDisable() {
        spectateManager.restoreAll();
    }
}