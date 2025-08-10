package org.modularsoft.consentpvp.events;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.modularsoft.consentpvp.ConsentPVP;
import org.modularsoft.consentpvp.util.PVPManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PVPEventListener implements Listener {

    private final ConsentPVP plugin;

    public PVPEventListener(ConsentPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPVPManager().loadConsentForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPVPManager().saveConsentForPlayer(event.getPlayer());
        plugin.getPVPManager().cleanupOfflinePlayers();
    }

    // Core: Handles most direct and indirect entity damage
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player defender = (Player) event.getEntity();
        PVPManager pvpManager = plugin.getPVPManager();

        Player attacker = null;

        // Direct melee
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }

        // Projectile (arrow, trident, snowball, egg, potion, firework, etc.)
        else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) attacker = (Player) shooter;
        }

        // TNT, End Crystal, Firework, etc.
        else if (event.getDamager() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getDamager();
            if (tnt.getSource() instanceof Player) attacker = (Player) tnt.getSource();
        }

        // Allow self-damage (such as Ender Pearl teleportation)
        if (attacker.getUniqueId().equals(defender.getUniqueId())) {
            return; // Do not cancel self-inflicted damage
        }

        if (attacker != null) {
            if (!pvpManager.hasConsent(defender.getUniqueId()) || !pvpManager.hasConsent(attacker.getUniqueId())) {
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(attacker, "pvp_not_consented_attacker", "%player%", defender.getName());
                plugin.getMessageManager().sendMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
            }
        }
    }

    // Potions: Splash
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof Player)) return;
        Player attacker = (Player) event.getPotion().getShooter();
        PVPManager pvpManager = plugin.getPVPManager();

        List<String> nonConsentedDefenders = new ArrayList<>();

        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof Player) {
                Player defender = (Player) entity;

                // Allow self-harm: skip consent check if attacker == defender
                if (attacker.getUniqueId().equals(defender.getUniqueId())) {
                    return;
                }

                if (!pvpManager.hasConsent(defender.getUniqueId()) || !pvpManager.hasConsent(attacker.getUniqueId())) {
                    event.setIntensity(defender, 0);
                    nonConsentedDefenders.add(defender.getName());
                    plugin.getMessageManager().sendMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
                }
            }
        });

        if (!nonConsentedDefenders.isEmpty()) {
            String joinedNames = String.join(", ", nonConsentedDefenders);
            plugin.getMessageManager().sendMessage(attacker, "pvp_not_consented_attacker_multiple", "%players%", joinedNames);
        }
    }

    // Potions: Lingering (Area Effect Cloud)
    @EventHandler
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (!(cloud.getSource() instanceof ThrownPotion)) return;

        ThrownPotion potion = (ThrownPotion) cloud.getSource();
        if (!(potion.getShooter() instanceof Player)) return;

        Player attacker = (Player) potion.getShooter();
        PVPManager pvpManager = plugin.getPVPManager();

        List<String> nonConsentedDefenders = new ArrayList<>();
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity entity = it.next();
            if (entity instanceof Player) {
                Player defender = (Player) entity;

                // Allow self-harm: skip consent check if attacker == defender
                if (attacker.getUniqueId().equals(defender.getUniqueId())) {
                    continue;
                }

                if (!pvpManager.hasConsent(defender.getUniqueId()) || !pvpManager.hasConsent(attacker.getUniqueId())) {
                    it.remove();
                    nonConsentedDefenders.add(defender.getName());
                    plugin.getMessageManager().sendMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
                }
            }
        }

        if (!nonConsentedDefenders.isEmpty()) {
            String joinedNames = String.join(", ", nonConsentedDefenders);
            plugin.getMessageManager().sendMessage(attacker, "pvp_not_consented_attacker_multiple", "%players%", joinedNames);
        }
    }

    // Handles Mace AOE Smash Attack
    @EventHandler
    public void onEntitySweepAttack(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player defender = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        PVPManager pvpManager = plugin.getPVPManager();

        // Allow self-damage
        if (attacker.getUniqueId().equals(defender.getUniqueId())) {
            return;
        }

        if (!pvpManager.hasConsent(defender.getUniqueId()) || !pvpManager.hasConsent(attacker.getUniqueId())) {
            event.setCancelled(true);
            // Silently cancel the event to prevent message spam in chat.
            // The original attacker and target are in a consensual fight.
            // Bystanders should not be notified.
        }
    }
}
