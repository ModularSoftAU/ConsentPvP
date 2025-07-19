package org.modularsoft.consentpvp.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.modularsoft.consentpvp.ConsentPVP;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PVPManager {

    private final ConsentPVP plugin;
    private final Map<UUID, Boolean> pvpConsent;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    public PVPManager(ConsentPVP plugin) {
        this.plugin = plugin;
        this.pvpConsent = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAllConsent();
    }

    public boolean hasConsent(UUID playerId) {
        return pvpConsent.getOrDefault(playerId, false);
    }

    public void setConsent(UUID playerId, boolean consent) {
        pvpConsent.put(playerId, consent);
        dataConfig.set(playerId.toString(), consent);
        saveConsent();
    }

    public void loadAllConsent() {
        if (!dataFile.exists()) return;
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean consent = dataConfig.getBoolean(key, false);
                pvpConsent.put(uuid, consent);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveConsent() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    public void cleanupOfflinePlayers() {
        pvpConsent.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return !offlinePlayer.isOnline();
        });
    }

    public void loadConsentForPlayer(Player player) {
        boolean consent = dataConfig.getBoolean(player.getUniqueId().toString(), false);
        pvpConsent.put(player.getUniqueId(), consent);
    }

    public void saveConsentForPlayer(Player player) {
        dataConfig.set(player.getUniqueId().toString(), pvpConsent.getOrDefault(player.getUniqueId(), false));
        saveConsent();
    }
}
