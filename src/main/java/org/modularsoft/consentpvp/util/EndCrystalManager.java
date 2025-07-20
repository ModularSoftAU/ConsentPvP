package org.modularsoft.consentpvp.util;

import org.bukkit.entity.EnderCrystal;

import java.util.UUID;
import java.util.WeakHashMap;

public class EndCrystalManager {

    private final WeakHashMap<EnderCrystal, UUID> crystalOwners = new WeakHashMap<>();

    public void addCrystal(EnderCrystal crystal, UUID owner) {
        crystalOwners.put(crystal, owner);
    }

    public UUID getOwner(EnderCrystal crystal) {
        return crystalOwners.get(crystal);
    }

    public void removeCrystal(EnderCrystal crystal) {
        crystalOwners.remove(crystal);
    }
}
