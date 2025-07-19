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
    private final String messagePrefix;

    public PVPCommand(ConsentPVP plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();
        this.messagePrefix = plugin.getMessagePrefix();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Only players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        PVPManager pvpManager = plugin.getPVPManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        MessageManager messageManager = plugin.getMessageManager();

        if (args.length == 0) {
            player.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Usage: /pvp <enable|disable>"));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "enable":
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
                if (cooldownManager.isOnCooldown(player.getUniqueId())) {
                    String remainingTime = messageManager.getRemainingCooldownTime(player.getUniqueId());
                    messageManager.sendMessage(player, "on_cooldown", "%time%", remainingTime);
                    return true;
                }
                pvpManager.setConsent(player.getUniqueId(), false);
                cooldownManager.setCooldown(player.getUniqueId());
                messageManager.sendMessage(player, "pvp_disabled");
                break;
            default:
                player.sendMessage(miniMessage.deserialize(messagePrefix + "<red>Usage: /pvp <enable|disable>"));
                break;
        }

        return true;
    }
}