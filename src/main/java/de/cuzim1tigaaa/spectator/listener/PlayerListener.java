package de.cuzim1tigaaa.spectator.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class PlayerListener implements Listener {

    private final Spectator plugin;
    private final SpectateManager manager;

    public PlayerListener(Spectator plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = this.plugin.getSpectateManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
            for(Player paused : CycleHandler.getPausedCycles().keySet()) CycleHandler.restartCycle(paused);

        boolean hide = !player.hasPermission(Permissions.BYPASS_TABLIST) && Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB);
        for(Player hidden : this.manager.getHidden()) {
            if(hide) player.hidePlayer(this.plugin, hidden);
            else player.showPlayer(this.plugin, hidden);
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) this.manager.unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) this.manager.dismountTarget(spectator);
                else {
                    int onlineNonSpec = (Bukkit.getOnlinePlayers().size() - 1) - this.plugin.getSpectators().size();
                    if(onlineNonSpec <= 0) {
                        if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) CycleHandler.pauseCycle(spectator);
                        else CycleHandler.stopCycle(spectator);
                    }else {
                        spectator.setSpectatorTarget(null);
                        CycleHandler.next(spectator);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onDismount(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) {
            if(!CycleHandler.isPlayerCycling(player) || !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                if(event.isSneaking()) this.manager.dismountTarget(player);
            }else {
                if(event.isSneaking() && this.plugin.getRelation().getOrDefault(player, null) != null) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAdvancementCriteriaGrant(PlayerAdvancementCriterionGrantEvent event) {
        Player player = event.getPlayer();
        if(!plugin.getSpectators().contains(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if(!Config.getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING) && CycleHandler.isPlayerCycling(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(this.plugin.getSpectators().contains(player)) this.plugin.getSpectateManager().unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) this.manager.dismountTarget(spectator);
                else {
                    spectator.setSpectatorTarget(null);
                    CycleHandler.next(spectator);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) return;

        Player player = event.getPlayer();

        if(player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player target)) return;

        if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) return;

        if(target.hasPermission(Permissions.BYPASS_SPECTATED)) {
            if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS_INVENTORY, "TARGET", target.getName()));
                event.setCancelled(true);
                return;
            }
        }

        this.plugin.getRelation().put(player, target);
    }
}