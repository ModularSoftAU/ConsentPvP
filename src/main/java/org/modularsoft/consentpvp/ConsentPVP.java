package org.modularsoft.consentpvp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.modularsoft.consentpvp.commands.PVPCommand;
import org.modularsoft.consentpvp.events.PVPEventListener;
import org.modularsoft.consentpvp.util.CooldownManager;
import org.modularsoft.consentpvp.util.MessageManager;
import org.modularsoft.consentpvp.util.PVPManager;

public class ConsentPVP extends JavaPlugin {

    private CooldownManager cooldownManager;
    private PVPManager pvpManager;
    private MessageManager messageManager;

    private MiniMessage miniMessage;
    private String messagePrefix;

    @Override
    public void onEnable() {
        // Initialize managers
        this.cooldownManager = new CooldownManager(this);
        this.pvpManager = new PVPManager(this);
        this.messageManager = new MessageManager(this);

        this.miniMessage = MiniMessage.miniMessage();
        this.messagePrefix = getConfig().getString("messages.prefix", "<gray>[<red>ConsentPVP<gray>] <white>");

        // Register commands
        getCommand("pvp").setExecutor(new PVPCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PVPEventListener(this), this);

        // Load configuration
        saveDefaultConfig();

        // Schedule periodic cleanup every 5 minutes (6000 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            cooldownManager.cleanupExpiredCooldowns();
            pvpManager.cleanupOfflinePlayers();
        }, 6000L, 6000L);
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public PVPManager getPVPManager() {
        return pvpManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }
}