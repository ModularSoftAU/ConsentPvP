package org.modularsoft.consentpvp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.modularsoft.consentpvp.commands.PVPCommand;
import org.modularsoft.consentpvp.events.PVPEventListener;
import org.modularsoft.consentpvp.util.*;

public class ConsentPVP extends JavaPlugin {

    private CooldownManager cooldownManager;
    private PVPManager pvpManager;
    private MessageManager messageManager;
    private EndCrystalManager endCrystalManager;
    private RespawnAnchorManager respawnAnchorManager;

    private MiniMessage miniMessage;
    private String messagePrefix;
    private boolean disablePvpOnDeath;

    @Override
    public void onEnable() {
        // Initialize managers
        this.cooldownManager = new CooldownManager(this);
        this.pvpManager = new PVPManager(this);
        this.messageManager = new MessageManager(this);
        this.endCrystalManager = new EndCrystalManager();
        this.respawnAnchorManager = new RespawnAnchorManager();

        this.miniMessage = MiniMessage.miniMessage();

        // Load configuration
        saveDefaultConfig();
        reloadConfig();
        this.messagePrefix = getConfig().getString("messages.prefix", "<gray>[<red>ConsentPVP<gray>] <white>");
        this.disablePvpOnDeath = getConfig().getBoolean("pvp.disable-on-death", false);


        // Register commands
        getCommand("pvp").setExecutor(new PVPCommand(this));
        getCommand("pvp").setTabCompleter(new org.modularsoft.consentpvp.commands.PVPTabCompleter());

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PVPEventListener(this), this);

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

    public EndCrystalManager getEndCrystalManager() {
        return endCrystalManager;
    }

    public RespawnAnchorManager getRespawnAnchorManager() {
        return respawnAnchorManager;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public boolean isPvpDisabledOnDeath() {
        return disablePvpOnDeath;
    }
}