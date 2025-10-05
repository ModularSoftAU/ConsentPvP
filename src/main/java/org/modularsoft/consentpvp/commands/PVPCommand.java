package org.modularsoft.consentpvp.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.modularsoft.consentpvp.ConsentPVP;
import org.modularsoft.consentpvp.util.CooldownManager;
import org.modularsoft.consentpvp.util.MessageManager;
import org.modularsoft.consentpvp.util.PVPManager;

public class PVPCommand implements CommandExecutor {

    private final ConsentPVP plugin;
    private final MiniMessage miniMessage;
    private String messagePrefix;

    public PVPCommand(ConsentPVP plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();
        this.messagePrefix = plugin.getMessagePrefix();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager messageManager = plugin.getMessageManager();
        PVPManager pvpManager = plugin.getPVPManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
                return true;
            }

            Player player = (Player) sender;
            showStatus(player, pvpManager, messageManager);
            return true;
        }

        String action = args[0].toLowerCase();

        Player player;

        switch (action) {
            case "reload":
                if (!sender.hasPermission("consentpvp.admin")) {
                    messageManager.sendMessage(sender, "no_permission");
                    return true;
                }

                plugin.reloadPluginConfig();
                this.messagePrefix = plugin.getMessagePrefix();
                messageManager.sendMessage(sender, "config_reloaded");
                break;
            case "status":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
                    return true;
                }
                showStatus((Player) sender, pvpManager, messageManager);
                break;
            case "enable":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
                    return true;
                }
                player = (Player) sender;
                if (cooldownManager.isOnCooldown(player.getUniqueId())) {
                    String remainingTime = messageManager.getRemainingCooldownTime(player.getUniqueId());
                    messageManager.sendMessage(player, "on_cooldown", "%time%", remainingTime);
                    return true;
                }
                pvpManager.setConsent(player.getUniqueId(), true);
                cooldownManager.setCooldown(player.getUniqueId());
                messageManager.sendMessage(player, "pvp_enabled");
                break;
            case "disable":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
                    return true;
                }
                player = (Player) sender;
                if (cooldownManager.isOnCooldown(player.getUniqueId())) {
                    String remainingTime = messageManager.getRemainingCooldownTime(player.getUniqueId());
                    messageManager.sendMessage(player, "on_cooldown", "%time%", remainingTime);
                    return true;
                }
                pvpManager.setConsent(player.getUniqueId(), false);
                cooldownManager.setCooldown(player.getUniqueId());
                messageManager.sendMessage(player, "pvp_disabled");
                break;
            case "death":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
                    return true;
                }
                player = (Player) sender;
                if (!sender.hasPermission("consentpvp.admin")) {
                    messageManager.sendMessage(player, "no_permission");
                    return true;
                }

                boolean pvpOnDeath = plugin.getConfig().getBoolean("pvp.disable-on-death");
                boolean newValue = !pvpOnDeath;
                plugin.getConfig().set("pvp.disable-on-death", newValue);
                plugin.saveConfig();
                plugin.setDisablePvpOnDeath(newValue);

                String status = newValue ? "enabled" : "disabled";
                messageManager.sendMessage(player, "pvp_death_toggle", "%status%", status);
                break;
            default:
                sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Usage: /pvp <enable|disable|death|status|reload>"));
                break;
        }

        return true;
    }

    private void showStatus(Player player, PVPManager pvpManager, MessageManager messageManager) {
        boolean hasConsent = pvpManager.hasConsent(player.getUniqueId());
        String status = hasConsent ? "enabled" : "disabled";
        messageManager.sendMessage(player, "pvp_status", "%status%", status);
    }
}