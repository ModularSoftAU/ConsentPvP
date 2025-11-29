package org.modularsoft.consentpvp.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.modularsoft.consentpvp.ConsentPVP;
import org.modularsoft.consentpvp.util.PVPManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        PVPManager pvpManager = plugin.getPVPManager();
        Player attacker = null;
        UUID attackerId = null;
        String attackerName = null;

        if (event.getDamager() instanceof Player playerDamager) {
            attacker = playerDamager;
            attackerId = playerDamager.getUniqueId();
            attackerName = playerDamager.getName();
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player playerShooter) {
                attacker = playerShooter;
                attackerId = playerShooter.getUniqueId();
                attackerName = playerShooter.getName();
            }
        } else if (event.getDamager() instanceof TNTPrimed tnt) {
            if (tnt.getSource() instanceof Player playerSource) {
                attacker = playerSource;
                attackerId = playerSource.getUniqueId();
                attackerName = playerSource.getName();
            }
        } else if (event.getDamager() instanceof EnderCrystal crystal) {
            UUID ownerUUID = plugin.getEndCrystalManager().getOwner(crystal);
            if (ownerUUID != null) {
                attackerId = ownerUUID;
                Player owner = plugin.getServer().getPlayer(ownerUUID);
                if (owner != null) {
                    attacker = owner;
                    attackerName = owner.getName();
                } else {
                    OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(ownerUUID);
                    attackerName = offlineOwner.getName();
                }
            }
        }

        if (attackerId == null) {
            DamageSource damageSource = event.getDamageSource();
            if (damageSource != null) {
                Entity causingEntity = damageSource.getCausingEntity();
                if (causingEntity instanceof Player causingPlayer) {
                    attacker = causingPlayer;
                    attackerId = causingPlayer.getUniqueId();
                    attackerName = causingPlayer.getName();
                }
            }
        }

        if (attackerName == null && attacker != null) {
            attackerName = attacker.getName();
        }

        if (attackerId == null) return;

        if (attackerId.equals(defender.getUniqueId())) return;

        boolean isSweepAttack = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;

        if (!pvpManager.hasConsent(attackerId) || !pvpManager.hasConsent(defender.getUniqueId())) {
            event.setCancelled(true);
            if (!isSweepAttack && attacker != null) {
                if (defender.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker_anonymous");
                } else {
                    plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker", "%player%", defender.getName());
                }
            }
            if (!isSweepAttack && plugin.shouldNotifyDefenderOnDenial() && attackerName != null) {
                if (attacker != null && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender_anonymous");
                } else {
                    plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender", "%player%", attackerName);
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof Player attacker)) return;

        PVPManager pvpManager = plugin.getPVPManager();
        List<String> nonConsented = new ArrayList<>();

        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof Player defender) {
                if (attacker.equals(defender)) return;

                if (!pvpManager.hasConsent(attacker.getUniqueId()) || !pvpManager.hasConsent(defender.getUniqueId())) {
                    event.setIntensity(defender, 0);
                    nonConsented.add(defender.getName());
                    if (plugin.shouldNotifyDefenderOnDenial()) {
                        plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
                    }
                }
            }
        });

        if (!nonConsented.isEmpty()) {
            String names = String.join(", ", nonConsented);
            plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker_multiple", "%players%", names);
        }
    }

    @EventHandler
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (!(cloud.getSource() instanceof ThrownPotion potion)) return;
        if (!(potion.getShooter() instanceof Player attacker)) return;

        PVPManager pvpManager = plugin.getPVPManager();
        List<String> nonConsented = new ArrayList<>();
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();

        while (it.hasNext()) {
            LivingEntity entity = it.next();
            if (entity instanceof Player defender) {
                if (attacker.equals(defender)) continue;

                if (!pvpManager.hasConsent(attacker.getUniqueId()) || !pvpManager.hasConsent(defender.getUniqueId())) {
                    it.remove();
                    nonConsented.add(defender.getName());
                    if (plugin.shouldNotifyDefenderOnDenial()) {
                        plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender", "%player%", attacker.getName());
                    }
                }
            }
        }

        if (!nonConsented.isEmpty()) {
            String names = String.join(", ", nonConsented);
            plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker_multiple", "%players%", names);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.isPvpDisabledOnDeath()) {
            Player player = event.getEntity();
            plugin.getPVPManager().setConsent(player.getUniqueId(), false);
            plugin.getMessageManager().sendMessage(player, "pvp_disabled_on_death");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getItem() == null) return;

        Player player = event.getPlayer();

        if (event.getItem().getType() == org.bukkit.Material.END_CRYSTAL) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof EnderCrystal crystal) {
                        plugin.getEndCrystalManager().addCrystal(crystal, player.getUniqueId());
                    }
                }
            }, 1L);
        }

        else if (
                event.getItem().getType() == org.bukkit.Material.GLOWSTONE &&
                        event.getClickedBlock() != null &&
                        event.getClickedBlock().getType() == org.bukkit.Material.RESPAWN_ANCHOR
        ) {
            UUID ownerUUID = plugin.getRespawnAnchorManager().getOwner(event.getClickedBlock());
            if (ownerUUID != null && !ownerUUID.equals(player.getUniqueId())) {
                Player owner = plugin.getServer().getPlayer(ownerUUID);
                if (owner != null && (!plugin.getPVPManager().hasConsent(owner.getUniqueId()) || !plugin.getPVPManager().hasConsent(player.getUniqueId()))) {
                    event.setCancelled(true);
                    plugin.getMessageManager().sendAttemptMessage(player, "pvp_not_consented_attacker", "%player%", owner.getName());
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            plugin.getEndCrystalManager().removeCrystal(crystal);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            plugin.getEndCrystalManager().addCrystal(crystal, event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == org.bukkit.Material.RESPAWN_ANCHOR) {
            plugin.getRespawnAnchorManager().addAnchor(event.getBlock(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == org.bukkit.Material.RESPAWN_ANCHOR) {
            plugin.getRespawnAnchorManager().removeAnchor(event.getBlock());
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
