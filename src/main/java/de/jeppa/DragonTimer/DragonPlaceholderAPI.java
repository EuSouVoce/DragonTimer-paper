
package de.jeppa.DragonTimer;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class DragonPlaceholderAPI extends PlaceholderExpansion {
    DragonTimer plugin;

    public DragonPlaceholderAPI(final DragonTimer instance) { this.plugin = instance; }

    @Override
    public String getAuthor() { return "Jeppa"; }

    @Override
    public String getIdentifier() { return this.plugin.getName().toLowerCase(); }

    @Override
    public String getVersion() { return this.plugin.getPluginMeta().getVersion(); }

    @Override
    public boolean canRegister() { return Bukkit.getPluginManager().isPluginEnabled(this.plugin.getName()); }

    @Override
    public boolean register() {
        if (this.canRegister()) {
            try {
                return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().register(this);
            } catch (Exception | NoSuchMethodError e) {
                this.plugin.getLogger().warning("Error while registering DragonTimer into PAPI");
            }
        }
        return false;
    }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(final Player player, final String identifier) {
        if (identifier.equals("playersnexttime")) {
            return this.plugin.getNextSpawnTime(player.getWorld().getName().toLowerCase());
        }
        if (identifier.equals("playerscountdown") || identifier.startsWith("countdown")) {
            String[] nextTimeCD = null;
            if (identifier.equals("playerscountdown")) {
                nextTimeCD = this.plugin.getWorldsNextSpawns(player.getWorld().getName().toLowerCase());
            }
            if (identifier.equals("countdown")) {
                nextTimeCD = this.plugin.getWorldsNextSpawns(this.getNextTimeOrMap("nextmap"));
            }
            if (identifier.startsWith("countdown_")) {
                final String world = identifier.substring(identifier.indexOf("_") + 1, identifier.length()).toLowerCase();
                nextTimeCD = this.plugin.getWorldsNextSpawns(world);
            }
            if (nextTimeCD != null) {
                return String.valueOf(nextTimeCD[0]) + ":" + nextTimeCD[1] + ":" + nextTimeCD[2];
            }
            return this.plugin.getNoTimerPlaceholder();
        }
        if (identifier.equals("nexttime") || identifier.equals("nextmap")) {
            return this.getNextTimeOrMap(identifier);
        }
        return "";
    }

    private String getNextTimeOrMap(final String identifier) {
        int NextTime;
        final Calendar now = Calendar.getInstance();
        final int now2 = now.get(11) * 100 + now.get(12);
        String NextMap = "";
        int firstTime = NextTime = 2560;
        String firstTimeString = "";
        String firstMap = "";
        String NextTimeString = "";
        for (final String TestMap : this.plugin.getMaplist()) {
            final String Time = this.plugin.getNextSpawnTime(TestMap);
            if (Time.isEmpty())
                continue;
            final int MapTime = Integer.parseInt(Time.replace(":", ""));
            if (MapTime < NextTime && MapTime > now2) {
                NextTime = MapTime;
                NextTimeString = Time;
                NextMap = TestMap;
                continue;
            }
            if (MapTime == NextTime) {
                NextMap = String.valueOf(NextMap) + " & " + TestMap;
                continue;
            }
            if (MapTime >= NextTime || MapTime >= now2 || MapTime >= firstTime)
                continue;
            firstTime = MapTime;
            firstMap = TestMap;
            firstTimeString = Time;
        }
        if (NextTime != 2560) {
            if (identifier.equals("nexttime")) {
                return NextTimeString;
            }
            if (identifier.equals("nextmap")) {
                return NextMap;
            }
        } else if (!firstTimeString.isEmpty()) {
            if (identifier.equals("nexttime")) {
                return firstTimeString;
            }
            if (identifier.equals("nextmap")) {
                return firstMap;
            }
        } else {
            return this.plugin.getNoTimerPlaceholder();
        }
        return "";
    }
}
