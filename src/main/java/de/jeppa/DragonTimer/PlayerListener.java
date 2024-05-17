
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
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final String worldName = world.getName().toLowerCase();
        if (!this.plugin.checkDSLWorld(worldName) && this.plugin.checkWorld(worldName)) {
            final int maxCount = this.plugin.getMaxDragons(worldName);
            if (maxCount > 1) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    final Collection<EnderDragon> EntityList = world.getEntitiesByClass(EnderDragon.class);

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
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.setTimerdisplayToPlayer(player), 60L);
    }

    @EventHandler
    public void worldChange(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final String world = event.getFrom().getName();
        if (this.plugin.checkWorld(world)) {
            if (!this.plugin.checkDSLWorld(world.toLowerCase())) {
                this.plugin.deletePlayersBossBars(player);
            }
            this.plugin.delTimerdisplayFromPlayer(player);
        }
        final String TogoWorld = player.getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(TogoWorld)) {
            this.plugin.setTimerdisplayToPlayer(player);
        }
    }
}
