package org.modularsoft.consentpvp.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LavaManager {

    private final Map<String, UUID> lavaBlocks = new HashMap<>();

    private String blockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public void addLava(Block block, UUID playerId) {
        lavaBlocks.put(blockKey(block), playerId);
    }

    public void removeLava(Block block) {
        lavaBlocks.remove(blockKey(block));
    }

    public UUID getOwner(Block block) {
        return lavaBlocks.get(blockKey(block));
    }

    // Returns the nearest tracked lava owner within radius blocks of the given location.
    // Needed because flowing lava blocks are at different coordinates than the source block.
    public UUID getNearbyOwner(Location location, int radius) {
        String worldName = location.getWorld().getName();
        int px = location.getBlockX();
        int py = location.getBlockY();
        int pz = location.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    String key = worldName + ":" + (px + dx) + ":" + (py + dy) + ":" + (pz + dz);
                    UUID owner = lavaBlocks.get(key);
                    if (owner != null) return owner;
                }
            }
        }
        return null;
    }
}
