package org.modularsoft.consentpvp.util;

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
}
