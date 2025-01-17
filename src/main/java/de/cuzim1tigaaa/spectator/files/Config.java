package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class Config {

    private static FileConfiguration config;
    private static File configFile;

    public static void loadConfig(Spectator plugin) {
        int serverVersion = Integer.parseInt(plugin.getServer().getBukkitVersion().split("\\.")[1].substring(0, 2)), currentVersion = 6;
        if (serverVersion < 18) {
            saveDefaultConfig(plugin);
            if (config.getInt("ConfigVersion") < currentVersion) replaceConfig(plugin, true);
            return;
        }
        try {
            configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                config = new YamlConfiguration();
                config.save(configFile);
            }

            config = YamlConfiguration.loadConfiguration(configFile);

            config.options().setHeader(comments(false,
                    "This is the configuration file of the plugin. Everything should be self-explanatory",
                    "If there is anything unclear, first take a look into the GitHub wiki:",
                    "https://github.com/CuzIm1Tigaaa/Spectator/wiki"));

            set(Paths.CONFIG_VERSION, comments(false,
                    "This is the current version of the config, DO NOT CHANGE!",
                    "If the version changes, the plugin will automatically",
                    "backup your current config and create the new one"), 6);

            set("Settings", comments(true), null);

            set(Paths.CONFIG_NOTIFY_UPDATE, comments(true,
                    "If the plugin gets updated, players with the following permission",
                    "will receive a message when they join",
                    "Permission: spectator.notify.update"), true);

            set(Paths.CONFIG_LANGUAGE, comments(true,
                    "Specify which language file should be used by the plugin",
                    "You can also add new languages! :)"), "en_US");

            set(Paths.CONFIG_HIDE_PLAYERS_TAB, comments(true,
                    "Spectators with the first following permission will be hidden in the tablist",
                    "Can be bypassed by players with the second permission.",
                    "Permission 1: spectator.utils.hidetab",
                    "Permission 2: spectator.bypass.tablist"), true);

            set(Paths.CONFIG_KICK_WHILE_CYCLING, comments(true,
                    "Cycling players cannot be kicked by any other player."), false);

            set("Settings.Save", comments(true), null);

            set(Paths.CONFIG_SAVE_PLAYERS_LOCATION, comments(true,
                    "The players' location (where he executed /spec) will be saved",
                    "Otherwise when the player leaves spectator mode, he will be at",
                    "his current location, equals to /spectatehere."), true);

            set(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE, comments(true,
                    "The players' flight mode will be saved. Otherwise, when the player",
                    "leaves spectator mode, he won't be he won't be flying anymore.",
                    "Requires allow-flight to true in server.properties!"), true);

            set("Settings.Cycle", comments(true), null);

            set(Paths.CONFIG_CYCLE_NO_PLAYERS, comments(true,
                    "Allows starting cycling even with no players online",
                    "Cycle will then work, when players are online!",
                    "Might be useful when using the plugin as a \"camera\""), true);

            set(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS, comments(true,
                    "The cycle gets paused if there are no longer any players online and will automatically restart",
                    "Otherwise the cycle will simply be stopped"), false);

            set(Paths.CONFIG_SHOW_BOSS_BAR, comments(true,
                    "Shows a bossbar to cycling players with the name of the current target"), false);

            config.save(configFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if (config.getInt(Paths.CONFIG_VERSION) < currentVersion) replaceConfig(plugin, false);
    }

    private static void set(String path, List<String> comment, Object value) {
        if (value == null && config.getConfigurationSection(path) == null) config.createSection(path);
        else config.set(path, config.get(path, value));
        if (comment != null && comment.size() > 0) config.setComments(path, comment);
    }

    private static List<String> comments(boolean empty, String... comment) {
        List<String> comments = new ArrayList<>();
        if (empty) comments.add(null);
        if (comment != null && comment.length > 0) comments.addAll(List.of(comment));
        return comments;
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    private static void replaceConfig(Spectator plugin, boolean old) {
        int i = 1;
        File backUp = new File(plugin.getDataFolder(), "configBackUp_" + i + ".yml");
        while (backUp.exists()) backUp = new File(plugin.getDataFolder(), "configBackUp_" + (i++) + ".yml");

        try {
            Files.copy(configFile.toPath(), backUp.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //noinspection ResultOfMethodCallIgnored
        configFile.delete();
        if (old) saveDefaultConfig(plugin);
        else loadConfig(plugin);
    }

    public static void saveDefaultConfig(Spectator plugin) {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}