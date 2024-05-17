package de.jeppa.DragonTimer;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

public class DragonRespawn implements Runnable {
    DragonTimer plugin;
    public String Mapname = null;
    public long StartTime = System.currentTimeMillis() / 50L;
    public long OrigRuntime = 0L;
    public int taskId;

    public DragonRespawn(final DragonTimer instance) { this.plugin = instance; }

    @Override
    public void run() {
        if (this.Mapname != null) {
            final Location DragSpawnPos = this.plugin.getDragonSpawn(this.Mapname);
            final World MyWorld = this.plugin.getDragonWorldFromString(this.Mapname);
            if (MyWorld == null)
                return;

            final Chunk MyChunk = MyWorld.getChunkAt(DragSpawnPos);

            if (!MyChunk.isLoaded() && !MyChunk.load())
                this.plugin.getLogger().warning("Failed to load Chunk: " + MyChunk.toString());

            try {
                MyWorld.spawn(DragSpawnPos, EnderDragon.class, dragon -> this.setWerte((EnderDragon) dragon));
            } catch (final Exception ignored) {
                if (this.plugin.debugOn) {
                    this.plugin.getLogger().info("Dragonspawn: New spawn method with consumer not possible, using NMS method...");
                }

                try {
                    this.BukkitNMSSpawn(DragSpawnPos);
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            }
        }

    }

    private EnderDragon BukkitNMSSpawn(final Location dragonSpawn)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        final SpawnReason spawnReason = SpawnReason.DEFAULT;
        final Object CraftWorld_o = this.plugin.getCraftWorld(dragonSpawn.getWorld());
        final Object entity = CraftWorld_o.getClass().getMethod("createEntity", Location.class, Class.class).invoke(CraftWorld_o,
                dragonSpawn, EntityType.ENDER_DRAGON.getEntityClass());
        final EnderDragon dragon = (EnderDragon) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        this.setWerte(dragon);

        final Class<?> entClass = Class.forName("net.minecraft.world.entity.Entity");

        CraftWorld_o.getClass().getMethod("addEntityToWorld", entClass, spawnReason.getClass()).invoke(CraftWorld_o, entity, spawnReason);
        return dragon;
    }

    private void setWerte(final EnderDragon drag) {
        drag.setPhase(Phase.CIRCLING);
        this.setDragonPosMeta(drag, drag.getLocation());
    }

    private void setDragonPosMeta(final EnderDragon dragon, final Location location) {
        final FixedMetadataValue MdV_DragonLocation = new FixedMetadataValue(this.plugin, location);
        dragon.setMetadata("DSL-Location", MdV_DragonLocation);
    }
}
