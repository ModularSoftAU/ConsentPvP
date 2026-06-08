package org.modularsoft.consentpvp.util;

import org.bukkit.block.Block;

import java.util.UUID;
import java.util.WeakHashMap;

public class RespawnAnchorManager {

    private final WeakHashMap<Block, UUID> anchorOwners = new WeakHashMap<>();

    public void addAnchor(Block anchor, UUID owner) {
        anchorOwners.put(anchor, owner);
    }

    public UUID getOwner(Block anchor) {
        return anchorOwners.get(anchor);
    }

    public void removeAnchor(Block anchor) {
        anchorOwners.remove(anchor);
    }
}
