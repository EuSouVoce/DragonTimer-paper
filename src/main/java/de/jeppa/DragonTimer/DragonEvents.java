
package de.jeppa.DragonTimer;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class DragonEvents implements Listener {
    DragonTimer plugin;
    Random random = new Random();

    public DragonEvents(final DragonTimer instance) { this.plugin = instance; }

    @EventHandler
    public void onDragonDeath(final EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final String w = entity.getWorld().getName().toLowerCase();
        if ((!this.plugin.getDSL() || this.plugin.getDSL() && !this.plugin.checkDSLWorld(w)) && this.plugin.checkWorld(w)
                && entity instanceof EnderDragon) {
            this.plugin.resetDragonsBossbar(entity);
            this.plugin.delDragonFromList((EnderDragon) ((Object) entity));
            this.plugin.AtKillCommand(w, ((EnderDragon) ((Object) entity)).getKiller(), entity.getName());
            this.plugin.cleanupDragonList();
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onDragonSpawn(final CreatureSpawnEvent e) {
        final String w = e.getEntity().getWorld().getName().toLowerCase();
        if ((!this.plugin.getDSL() || this.plugin.getDSL() && !this.plugin.checkDSLWorld(w)) && this.plugin.checkWorld(w)
                && e.getEntityType() == EntityType.ENDER_DRAGON) {
            BossBar BossBar2;
            final EnderDragon ThisDrag = (EnderDragon) ((Object) e.getEntity());
            final int maxCount = this.plugin.getMaxDragons(w);
            ThisDrag.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.plugin.getDragonHealth(w));
            this.plugin.getEnderDragonBattle(ThisDrag.getWorld());
            ThisDrag.setPhase(EnderDragon.Phase.CIRCLING);
            ThisDrag.setHealth(this.plugin.getDragonHealth(w));
            ThisDrag.setCustomName(this.plugin.getDragonName(w));
            ThisDrag.setCustomNameVisible(true);
            if (maxCount > 1 && (BossBar2 = this.plugin.findFreeBar(w)) != null) {
                BossBar2.setTitle(ThisDrag.getName());
                this.plugin.setBossBarAmountNOW(ThisDrag, BossBar2);
                this.plugin.putBossBarToDragon(ThisDrag, BossBar2);
                this.FindPlayerAndAddToBossBar(ThisDrag, BossBar2);
            }
            this.plugin.putDragonToList(ThisDrag, 1);
            this.PlayDragonSound();
        }
    }

    private void PlayDragonSound() {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            final Sound growl = Sound.ENTITY_ENDER_DRAGON_GROWL;
            final Location l = p.getLocation();
            if (growl != null) {
                p.playSound(l, growl, 1.0f, 0.0f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDragonMoves(final EnderDragonChangePhaseEvent event) {
        final Entity e = event.getEntity();
        final String w = e.getWorld().getName().toLowerCase();
        if (!this.plugin.getDSL() || (this.plugin.getDSL() && !this.plugin.checkDSLWorld(w))) {
            final EnderDragon.Phase NextPhase = event.getNewPhase();
            BossBar BossBar = null;
            if (e instanceof final EnderDragon ThisDrag) {
                if (this.plugin.checkWorld(w)) {
                    BossBar = this.plugin.getBossBarFromDragon(ThisDrag);
                    if (BossBar != null) {
                        this.FindPlayerAndAddToBossBar(ThisDrag, BossBar);
                        this.plugin.setBossBarAmountNOW(ThisDrag, BossBar);
                        this.plugin.OrigEnderDragonSetKilled(ThisDrag, true);
                    } else if (e.isValid() && !e.isDead() && NextPhase != EnderDragon.Phase.DYING) {
                        final int maxCount = this.plugin.getMaxDragons(w);
                        boolean forceBar = false;
                        if (DragonTimer.getSubVersion() == 19) {
                            this.plugin.getEnderDragonBattle(e.getWorld());
                            if (maxCount == 1 && (e.getWorld().getEnderDragonBattle().getEnderDragon() == null
                                    || !e.getWorld().getEnderDragonBattle().getEnderDragon().equals(ThisDrag))) {
                                forceBar = true;
                            }
                        }
                        if (maxCount > 1 || forceBar) {
                            BossBar = this.plugin.findFreeBar(w);
                            if (BossBar != null) {
                                BossBar.setTitle(ThisDrag.getName());
                                this.plugin.setBossBarAmountNOW(ThisDrag, BossBar);
                                this.plugin.putBossBarToDragon(ThisDrag, BossBar);
                                this.FindPlayerAndAddToBossBar(ThisDrag, BossBar);
                            }
                        }
                    }
                    this.plugin.cleanupDragons();
                    if (NextPhase == EnderDragon.Phase.DYING) {
                        Set<Entity> EntityList = new HashSet<Entity>();
                        EntityList = this.FindEntities(new Location(e.getWorld(), 0.0, 70.0, 0.0), 1);
                        for (final Entity Dragon : EntityList) {
                            if (Dragon instanceof EnderDragon && Dragon.getEntityId() != e.getEntityId() && Dragon.isValid()) {
                                final EnderDragon.Phase DragPhase = ((EnderDragon) Dragon).getPhase();
                                if (DragPhase == EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET) {
                                    ((EnderDragon) Dragon).setPhase(EnderDragon.Phase.LEAVE_PORTAL);
                                } else {
                                    if (DragPhase != EnderDragon.Phase.FLY_TO_PORTAL && DragPhase != EnderDragon.Phase.LAND_ON_PORTAL
                                            && DragPhase != EnderDragon.Phase.HOVER) {
                                        continue;
                                    }
                                    ((EnderDragon) Dragon).setPhase(EnderDragon.Phase.CIRCLING);
                                }
                            }
                        }
                    }
                    if ((NextPhase == EnderDragon.Phase.FLY_TO_PORTAL || NextPhase == EnderDragon.Phase.LAND_ON_PORTAL)
                            && e.getTicksLived() > 200) {
                        if (this.plugin.getNoGuarding(w)) {
                            event.setCancelled(true);
                            ((EnderDragon) e).setPhase(EnderDragon.Phase.CIRCLING);
                            return;
                        }
                        final Set<Entity> EntityList = this.FindEntities(new Location(e.getWorld(), 0.0, 70.0, 0.0), 1);
                        for (final Entity Dragon : EntityList) {
                            if (Dragon instanceof EnderDragon && Dragon.getEntityId() != e.getEntityId() && Dragon.isValid()) {
                                final EnderDragon.Phase DragPhase = ((EnderDragon) Dragon).getPhase();
                                if (DragPhase != EnderDragon.Phase.FLY_TO_PORTAL && DragPhase != EnderDragon.Phase.LAND_ON_PORTAL
                                        && DragPhase != EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                        && DragPhase != EnderDragon.Phase.HOVER) {
                                    continue;
                                }
                                if (DragPhase == EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                        || DragPhase == EnderDragon.Phase.HOVER) {
                                    ((EnderDragon) Dragon).setPhase(EnderDragon.Phase.LEAVE_PORTAL);
                                }
                                event.setCancelled(true);
                                ((EnderDragon) e).setPhase(EnderDragon.Phase.CIRCLING);
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<Entity> FindEntities(final Location loc, final int Radius) {
        final Block Block2 = loc.getBlock();
        final HashSet<Entity> ents = new HashSet<Entity>();
        int x = -16 * Radius;
        while (x <= 16 * Radius) {
            int z = -16 * Radius;
            while (z <= 16 * Radius) {
                final Chunk MyChunk = Block2.getRelative(x, 0, z).getChunk();
                if (!MyChunk.isLoaded()) {
                    MyChunk.load();
                }
                final Entity[] entityArray = MyChunk.getEntities();
                final int n = entityArray.length;
                int n2 = 0;
                while (n2 < n) {
                    final Entity Ent = entityArray[n2];
                    ents.add(Ent);
                    ++n2;
                }
                z += 16;
            }
            x += 16;
        }
        return ents;
    }

    private void FindPlayerAndAddToBossBar(final EnderDragon ThisDrag, final BossBar BossBar) {
        final List<Player> PlayerList = ThisDrag.getWorld().getPlayers();
        final List<Player> AddedPlayers = BossBar.getPlayers();
        for (final Player player : PlayerList) {
            if (!AddedPlayers.contains(player)) {
                BossBar.addPlayer(player);
            }
        }
    }

    @EventHandler
    public void onDamageTheDragon(final EntityDamageByEntityEvent event) {
        final Entity e = event.getEntity();
        if (e instanceof EnderDragon) {
            this.plugin.setBossBarAmount((EnderDragon) e);
            if (DragonTimer.getSubVersion() == 19) {
                this.plugin.getEnderDragonBattle(e.getWorld());
            }
        }
    }

    @EventHandler
    public void onHealTheDragon(final EntityRegainHealthEvent event) {
        final Entity e = event.getEntity();
        if (e instanceof EnderDragon) {
            this.plugin.setBossBarAmount((EnderDragon) e);
        }
    }
}
