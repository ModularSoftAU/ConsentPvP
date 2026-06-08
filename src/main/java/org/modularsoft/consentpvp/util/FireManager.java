package org.modularsoft.consentpvp.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireManager {

    private final Map<String, UUID> fireBlocks = new HashMap<>();

    private String blockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public void addFire(Block block, UUID playerId) {
        fireBlocks.put(blockKey(block), playerId);
    }

    public void removeFire(Block block) {
        fireBlocks.remove(blockKey(block));
    }

    // Returns the nearest tracked fire/lava owner within radius blocks of the given location.
    public UUID getNearbyOwner(Location location, int radius) {
        String worldName = location.getWorld().getName();
        int px = location.getBlockX();
        int py = location.getBlockY();
        int pz = location.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    String key = worldName + ":" + (px + dx) + ":" + (py + dy) + ":" + (pz + dz);
                    UUID owner = fireBlocks.get(key);
                    if (owner != null) return owner;
                }
            }
        }
        return null;
    }
}
