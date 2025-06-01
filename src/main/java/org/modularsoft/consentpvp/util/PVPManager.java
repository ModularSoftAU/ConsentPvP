package org.modularsoft.consentpvp.util;

import org.modularsoft.consentpvp.ConsentPVP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PVPManager {

    private final ConsentPVP plugin;
    private final Map<UUID, Boolean> pvpConsent;

    public PVPManager(ConsentPVP plugin) {
        this.plugin = plugin;
        this.pvpConsent = new HashMap<>();
    }

    public boolean hasConsent(UUID playerId) {
        return pvpConsent.getOrDefault(playerId, false);
    }

    public void setConsent(UUID playerId, boolean consent) {
        pvpConsent.put(playerId, consent);
    }
}

