package org.modularsoft.consentpvp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.modularsoft.consentpvp.ConsentPVP;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MessageManager {

    private final ConsentPVP plugin;
    private final MiniMessage miniMessage;

    public MessageManager(ConsentPVP plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void sendMessage(Player player, String messageKey, String... replacements) {
        sendMessage((CommandSender) player, messageKey, replacements);
    }

    public void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        Component message = buildMessage(messageKey, replacements);
        if (message == null) {
            return;
        }
        sender.sendMessage(message);
    }

    public void sendAttemptMessage(Player player, String messageKey, String... replacements) {
        Component message = buildMessage(messageKey, replacements);
        if (message == null) {
            return;
        }

        if (plugin.getAttemptMessageDelivery() == AttemptMessageDelivery.ACTION_BAR) {
            player.sendActionBar(message);
        } else {
            player.sendMessage(message);
        }
    }

    private Component buildMessage(String messageKey, String... replacements) {
        String message = plugin.getConfig().getString("messages." + messageKey);
        if (message == null) {
            plugin.getLogger().warning("Missing message key: messages." + messageKey);
            return null;
        }
        if (replacements.length % 2 != 0) {
            plugin.getLogger().warning("Replacements array must have even number of elements");
            return null;
        }
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return miniMessage.deserialize(message);
    }

    public String getRemainingCooldownTime(UUID playerId) {
        CooldownManager cooldownManager = plugin.getCooldownManager();
        Long lastToggleTime = cooldownManager.getLastToggleTime(playerId);
        if (lastToggleTime == null) {
            return "0 minutes and 0 seconds";
        }

        long currentTime = System.currentTimeMillis();
        long cooldownDuration = TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("cooldown.duration"));
        long timeLeft = cooldownDuration - (currentTime - lastToggleTime);

        if (timeLeft <= 0) {
            return "0 minutes and 0 seconds";
        }

        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeft);
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeft) - TimeUnit.MINUTES.toSeconds(minutesLeft);

        return String.format("%d minutes and %d seconds", minutesLeft, secondsLeft);
    }
}

