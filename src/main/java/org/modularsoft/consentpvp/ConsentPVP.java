package org.modularsoft.consentpvp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConsentPVP extends JavaPlugin implements Listener {

    private Map<UUID, UUID> pvpRequests = new HashMap<>();
    private FileConfiguration playerData;
    private File playerDataFile;
    private long cooldownDuration;
    private long combatTimeout;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        loadConfig();

        // Setup player data file
        setupPlayerData();

        // Register commands
        getCommand("pvp").setExecutor(new PVPCommand());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        cooldownDuration = TimeUnit.MINUTES.toMillis(config.getLong("cooldown.duration"));
        combatTimeout = config.getLong("combat.timeout");
    }

    private void setupPlayerData() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            try {
                playerDataFile.createNewFile(); // Create a new file if it doesn't exist
                playerData = YamlConfiguration.loadConfiguration(playerDataFile);
            } catch (IOException e) {
                getLogger().severe("Could not create player data file: " + e.getMessage());
            }
        } else {
            playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        }
    }

    private void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save player data: " + e.getMessage());
        }
    }

    public boolean hasConsent(Player player) {
        return playerData.getBoolean(player.getUniqueId() + ".consent", false);
    }

    public String getRemainingCooldownTime(Player player) {
        Long lastToggleTime = playerData.getLong(player.getUniqueId() + ".lastToggle", 0);
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastToggleTime;
        long timeLeft = cooldownDuration - timeElapsed;

        if (timeLeft <= 0) {
            return "0 minutes and 0 seconds";
        }

        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeft);
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeft) - TimeUnit.MINUTES.toSeconds(minutesLeft);

        return minutesLeft + " minutes and " + secondsLeft + " seconds";
    }

    public boolean isOnCooldown(Player player) {
        Long lastToggleTime = playerData.getLong(player.getUniqueId() + ".lastToggle", 0);
        long currentTime = System.currentTimeMillis();
        return currentTime - lastToggleTime < cooldownDuration;
    }

    public boolean isInCombat(Player player) {
        return playerData.getBoolean(player.getUniqueId() + ".inCombat", false);
    }

    public void setConsent(Player player, boolean consent) {
        if (isOnCooldown(player) && !player.hasPermission("consentpvp.admin")) {
            String remainingTime = getRemainingCooldownTime(player);
            player.sendMessage(getConfig().getString("messages.on_cooldown").replace("%time%", remainingTime));
            return;
        }

        if (!consent && isInCombat(player) && !player.hasPermission("consentpvp.admin")) {
            player.sendMessage(getConfig().getString("messages.in_combat"));
            return;
        }

        playerData.set(player.getUniqueId() + ".consent", consent);
        playerData.set(player.getUniqueId() + ".lastToggle", System.currentTimeMillis());
        savePlayerData();
        player.sendMessage(consent ? getConfig().getString("messages.pvp_enabled") : getConfig().getString("messages.pvp_disabled"));
    }

    public void sendPVPRequest(Player fromPlayer, Player toPlayer) {
        if (!hasConsent(fromPlayer)) {
            fromPlayer.sendMessage(getConfig().getString("messages.pvp_disabled"));
            return;
        }

        pvpRequests.put(toPlayer.getUniqueId(), fromPlayer.getUniqueId());
        toPlayer.sendMessage(String.format(getConfig().getString("messages.request_received"), fromPlayer.getName()));
        fromPlayer.sendMessage(String.format(getConfig().getString("messages.request_sent"), toPlayer.getName()));
    }

    public void acceptPVPRequest(Player player) {
        UUID requesterId = pvpRequests.get(player.getUniqueId());
        if (requesterId != null) {
            Player requester = getServer().getPlayer(requesterId);
            if (requester != null && hasConsent(requester) && hasConsent(player)) {
                player.sendMessage(String.format(getConfig().getString("messages.request_accepted"), requester.getName()));
                requester.sendMessage(String.format(getConfig().getString("messages.request_accepted"), player.getName()));
                enablePVP(requester, player);
            } else {
                player.sendMessage(getConfig().getString("messages.requester_unavailable"));
            }
            pvpRequests.remove(player.getUniqueId());
        } else {
            player.sendMessage(getConfig().getString("messages.no_request"));
        }
    }

    private void enablePVP(Player player1, Player player2) {
        player1.sendMessage("PVP is now enabled between you and " + player2.getName());
        player2.sendMessage("PVP is now enabled between you and " + player1.getName());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player defender = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            // Check if both players have consented to PVP
            if (!hasConsent(defender) || !hasConsent(attacker)) {
                // Cancel the damage event since PVP is not consented
                event.setCancelled(true);

                // Notify both players
                attacker.sendMessage(getConfig().getString("messages.pvp_not_consented_attacker").replace("%player%", defender.getName()));
                defender.sendMessage(getConfig().getString("messages.pvp_not_consented_defender").replace("%player%", attacker.getName()));
                return;
            }

            // If PVP is consented, proceed with combat status
            playerData.set(defender.getUniqueId() + ".inCombat", true);
            playerData.set(attacker.getUniqueId() + ".inCombat", true);
            savePlayerData();

            getServer().getScheduler().runTaskLater(this, () -> {
                playerData.set(defender.getUniqueId() + ".inCombat", false);
                playerData.set(attacker.getUniqueId() + ".inCombat", false);
                savePlayerData();
            }, 20 * combatTimeout); // combatTimeout seconds later
        }
    }

    private class PVPCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("Usage: /pvp <enable|disable|request|accept>");
                return true;
            }

            String action = args[0].toLowerCase();

            switch (action) {
                case "enable":
                    setConsent(player, true);
                    break;
                case "disable":
                    setConsent(player, false);
                    break;
                case "request":
                    if (args.length < 2) {
                        player.sendMessage("Usage: /pvp request <player>");
                        break;
                    }
                    Player targetPlayer = getServer().getPlayer(args[1]);
                    if (targetPlayer != null) {
                        sendPVPRequest(player, targetPlayer);
                    } else {
                        player.sendMessage("Player not found.");
                    }
                    break;
                case "accept":
                    acceptPVPRequest(player);
                    break;
                default:
                    player.sendMessage("Usage: /pvp <enable|disable|request|accept>");
                    break;
            }

            return true;
        }
    }
}