package org.modularsoft.consentpvp.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
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
        plugin.getNameTagManager().updatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPVPManager().saveConsentForPlayer(event.getPlayer());
        plugin.getPVPManager().cleanupOfflinePlayers();
        plugin.getNameTagManager().removePlayer(event.getPlayer());
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
        } else if (event.getDamager() instanceof ExplosiveMinecart tntMinecart) {
            // ExplosiveMinecart doesn't have a direct source in Bukkit API easily
            // But we can check for nearby players if we want to be aggressive,
            // or just block damage to non-consenting players from explosives.
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

        if (attackerId == null) {
            // If the damager is an explosive minecart or a general explosion, and the defender has PvP off, block it.
            if ((event.getDamager() instanceof ExplosiveMinecart) && !pvpManager.hasConsent(defender.getUniqueId())) {
                event.setCancelled(true);
                plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender_anonymous");
                return;
            }
            return;
        }

        if (attackerId.equals(defender.getUniqueId())) return;

        // Block attacks from vanished players (hidden via vanish plugin, not potion invisibility)
        if (attacker != null && !defender.canSee(attacker)) {
            event.setCancelled(true);
            return;
        }

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
            plugin.getNameTagManager().updatePlayer(player);
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
        if (event.getBlock().getType() == org.bukkit.Material.LAVA) {
            plugin.getLavaManager().addLava(event.getBlock(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == org.bukkit.Material.RESPAWN_ANCHOR) {
            plugin.getRespawnAnchorManager().removeAnchor(event.getBlock());
        }
        if (event.getBlock().getType() == org.bukkit.Material.LAVA) {
            plugin.getLavaManager().removeLava(event.getBlock());
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBucket() == org.bukkit.Material.LAVA_BUCKET) {
            org.bukkit.block.Block placed = event.getBlock().getRelative(event.getBlockFace());
            plugin.getLavaManager().addLava(placed, event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.getItemStack().getType() == org.bukkit.Material.LAVA_BUCKET) {
            plugin.getLavaManager().removeLava(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null) return;
        if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && event.getCause() != BlockIgniteEvent.IgniteCause.FIREBALL) return;

        Player attacker = event.getPlayer();
        PVPManager pvpManager = plugin.getPVPManager();

        for (Entity entity : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)) {
            if (entity instanceof Player defender) {
                if (attacker.equals(defender)) continue;

                if (!pvpManager.hasConsent(attacker.getUniqueId()) || !pvpManager.hasConsent(defender.getUniqueId())) {
                    event.setCancelled(true);
                    if (defender.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker_anonymous");
                    } else {
                        plugin.getMessageManager().sendAttemptMessage(attacker, "pvp_not_consented_attacker", "%player%", defender.getName());
                    }
                    return;
                }
            }
        }
    }

    // Prevent explosion damage for non-consenting players
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        PVPManager pvpManager = plugin.getPVPManager();

        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            org.bukkit.Location loc = defender.getLocation();
            org.bukkit.block.Block[] candidates = {
                loc.getBlock(),
                loc.clone().add(0, 1, 0).getBlock(),
                loc.clone().subtract(0, 1, 0).getBlock()
            };
            for (org.bukkit.block.Block block : candidates) {
                if (block.getType() == org.bukkit.Material.LAVA) {
                    UUID lavaPlacer = plugin.getLavaManager().getOwner(block);
                    if (lavaPlacer != null && !lavaPlacer.equals(defender.getUniqueId())) {
                        if (!pvpManager.hasConsent(lavaPlacer) || !pvpManager.hasConsent(defender.getUniqueId())) {
                            event.setCancelled(true);
                        }
                    }
                    return;
                }
            }
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;

        // If it's EntityDamageByEntityEvent, it's already handled by onEntityDamageByEntity
        if (event instanceof EntityDamageByEntityEvent) return;

        if (!pvpManager.hasConsent(defender.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessageManager().sendAttemptMessage(defender, "pvp_not_consented_defender_anonymous");
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
