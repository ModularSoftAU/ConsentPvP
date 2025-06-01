package org.modularsoft.consentpvp.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.modularsoft.consentpvp.ConsentPVP;
import org.modularsoft.consentpvp.util.PVPManager;

public class PVPEventListener implements Listener {

    private final ConsentPVP plugin;

    public PVPEventListener(ConsentPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player defender = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            PVPManager pvpManager = plugin.getPVPManager();
            if (!pvpManager.hasConsent(defender.getUniqueId()) || !pvpManager.hasConsent(attacker.getUniqueId())) {
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(attacker, "pvp_not_consented_attacker", "%player%", defender.getName());
                plugin.getMessageManager().sendMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
            }
        }
    }
}

