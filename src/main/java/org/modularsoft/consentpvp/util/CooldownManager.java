package org.modularsoft.consentpvp.util;

import org.modularsoft.consentpvp.ConsentPVP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final ConsentPVP plugin;
    private final Map<UUID, Long> cooldowns;

    public CooldownManager(ConsentPVP plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    public boolean isOnCooldown(UUID playerId) {
        Long lastToggleTime = cooldowns.get(playerId);
        if (lastToggleTime == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long cooldownDuration = TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("cooldown.duration"));
        return currentTime - lastToggleTime < cooldownDuration;
    }

    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis());
    }

    public Long getLastToggleTime(UUID playerId) {
        return cooldowns.get(playerId);
    }
}