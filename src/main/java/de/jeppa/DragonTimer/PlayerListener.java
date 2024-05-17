
package de.jeppa.DragonTimer;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    DragonTimer plugin;

    public PlayerListener(final DragonTimer instance) { this.plugin = instance; }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final World TheWorld = p.getWorld();
        final String Worldname = TheWorld.getName().toLowerCase();
        if (!this.plugin.checkDSLWorld(Worldname) && this.plugin.checkWorld(Worldname)) {
            final int maxCount = this.plugin.getMaxDragons(Worldname);
            if (maxCount > 1) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    final Collection<EnderDragon> EntityList = TheWorld.getEntitiesByClass(EnderDragon.class);

                    final Iterator<EnderDragon> iterator = EntityList.iterator();
                    while (iterator.hasNext()) {
                        final EnderDragon Dragon = iterator.next();
                        if (Dragon.isValid()) {
                            this.plugin.OrigEnderDragonSetKilled(Dragon, true);
                        }
                    }
                    return;
                }, 110L);
            }
        }
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.setTimerdisplayToPlayer(p), 60L);
    }

    @EventHandler
    public void worldChange(final PlayerChangedWorldEvent event) {
        final Player p = event.getPlayer();
        final String ThisWorld = event.getFrom().getName();
        if (this.plugin.checkWorld(ThisWorld)) {
            if (!this.plugin.checkDSLWorld(ThisWorld.toLowerCase())) {
                this.plugin.deletePlayersBossBars(p);
            }
            this.plugin.delTimerdisplayFromPlayer(p);
        }
        final String TogoWorld = p.getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(TogoWorld)) {
            this.plugin.setTimerdisplayToPlayer(p);
        }
    }
}
