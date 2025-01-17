package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpectateManager {

    private final Spectator plugin;

    private final HashMap<Player, PlayerAttributes> pAttributes = new HashMap<>();

    public SpectateManager(Spectator plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Player, Player> entry : plugin.getRelation().entrySet()) {
                final Player player = entry.getKey();
                final Player target = entry.getValue();

                if (!player.getWorld().equals(target.getWorld()) || player.getLocation().distanceSquared(target.getLocation()) > 1) {
                    player.setSpectatorTarget(null);
                    player.setSpectatorTarget(target);
                }
            }
        }, 0, 20);
    }

    private final Set<Player> hidden = new HashSet<>();

    public HashMap<Player, PlayerAttributes> getPAttributes() {
        return pAttributes;
    }

    public Set<Player> getHidden() {
        return hidden;
    }

    public void spectate(final Player player, final Player target) {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(p -> !this.plugin.getRelation().containsKey(p) || !this.plugin.getRelation().get(p).equals(player));

        for (Player spec : spectators) spectate(spec, null);

        if (!this.pAttributes.containsKey(player)) this.pAttributes.put(player, new PlayerAttributes(player));

        player.setGameMode(GameMode.SPECTATOR);
        this.plugin.getSpectators().add(player);

        if (player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) &&
                Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, true);

        if (target != null) {
            player.setSpectatorTarget(null);
            this.plugin.getRelation().remove(player);

            this.plugin.getRelation().put(player, target);
            player.setSpectatorTarget(target);
        }
    }

    public void unSpectate(final Player player, final boolean loc) {
        Location location = null;

        if (Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION) && !loc) {
            if (this.pAttributes.containsKey(player)) location = this.pAttributes.get(player).getLocation();
        }

        if (location == null) {
            location = player.getLocation();
            float pitch = location.getPitch();
            float yaw = location.getYaw();
            player.teleport(location);
            location.setPitch(pitch);
            location.setYaw(yaw);
        }

        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.plugin.getSpectators().remove(player);
        this.plugin.getRelation().remove(player);

        if (this.hidden.contains(player) && player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) &&
                Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, false);

        GameMode gameMode = null;
        boolean isFlying = false;

        if (this.pAttributes.containsKey(player)) {
            gameMode = this.pAttributes.get(player).getGameMode();
            isFlying = this.plugin.getServer().getAllowFlight() && this.pAttributes.get(player).getFlying();
            this.pAttributes.remove(player);
        }

        if (!Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE)) isFlying = false;

        if (gameMode == null) {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }

        player.setGameMode(gameMode);
        player.setFlying(isFlying);

        if (CycleHandler.isPlayerCycling(player)) CycleHandler.breakCycle(player, true);
    }

    public void restoreAll() {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        for (Player player : spectators) this.unSpectate(player, false);
    }

    private void hideFromTab(final Player player, final boolean hide) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(player.getUniqueId())) continue;
            if (target.hasPermission(Permissions.BYPASS_TABLIST)) continue;
            if (hide) {
                this.hidden.add(player);
                target.hidePlayer(this.plugin, player);
                player.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
            } else {
                this.hidden.remove(player);
                target.showPlayer(this.plugin, player);
                player.removeMetadata("vanished", this.plugin);
            }
        }
    }

    public void dismountTarget(Player player) {
        if (!player.getGameMode().equals(GameMode.SPECTATOR)) return;
        this.plugin.getRelation().remove(player);
        player.getInventory().clear();
        player.setSpectatorTarget(null);
    }

}