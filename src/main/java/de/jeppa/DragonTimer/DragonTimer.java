package de.jeppa.DragonTimer;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import de.jeppa.DragonSlayer.DragonSlayer;
import net.milkbowl.vault.economy.Economy;

@SuppressWarnings("deprecation")
public class DragonTimer extends JavaPlugin {
    public final Logger logger;
    DragonEvents dragonListener;
    PlayerListener playerListener;
    DragonCommands dragonCommandExecutor;
    static Economy econ;
    public File ConfigFile;
    boolean PAPIenabled;
    boolean MinVersion19;
    boolean spigot;
    private boolean dsl;
    String[] spawnTimeStrings;
    HashMap<String, String[]> SpawnTimerMap;
    HashMap<String, Scoreboard> timerDisplays;
    ArrayList<BossBar> BossBars;
    HashMap<EnderDragon, BossBar> DragonBarList;
    HashMap<EnderDragon, Integer> DragonList;
    HashMap<String, Integer> RemoveInProgress;
    HashMap<String, Integer> TimelimitActive;
    Field DragonKilled;
    Field EnumDragonRespawn;
    Method RespawnMethod;
    Plugin dsl_p;
    Method dslCheckWorld_m;
    Method dslStartRefresh_m;
    Method getDragonlist;
    int RunCounter;
    int wasrunning;

    private static int Sub;
    Method getEDBMethod;
    Class<?> CraftWorldClass;
    Class<?> CraftEnderDragonClass;
    boolean debugOn;

    static {
        DragonTimer.econ = null;
        DragonTimer.Sub = -1;
    }

    public DragonTimer() {
        this.logger = this.getLogger();
        this.dragonListener = null;
        this.playerListener = new PlayerListener(this);
        this.dragonCommandExecutor = new DragonCommands(this);
        this.ConfigFile = null;
        this.PAPIenabled = false;
        this.MinVersion19 = false;
        this.spigot = false;
        this.dsl = false;
        this.spawnTimeStrings = null;
        this.SpawnTimerMap = new HashMap<String, String[]>();
        this.timerDisplays = new HashMap<String, Scoreboard>();
        this.BossBars = new ArrayList<BossBar>();
        this.DragonBarList = new HashMap<EnderDragon, BossBar>();
        this.DragonList = new HashMap<EnderDragon, Integer>();
        this.RemoveInProgress = new HashMap<String, Integer>();
        this.TimelimitActive = new HashMap<String, Integer>();
        this.DragonKilled = null;
        this.EnumDragonRespawn = null;
        this.RespawnMethod = null;
        this.dsl_p = null;
        this.dslCheckWorld_m = null;
        this.dslStartRefresh_m = null;
        this.getDragonlist = null;
        this.RunCounter = 0;
        this.wasrunning = 99;
        this.getEDBMethod = null;
        this.CraftWorldClass = null;
        this.CraftEnderDragonClass = null;
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();
        DragonTimer.getVersion();
        this.PlaceholderApi();

        if (this.dsl_p == null) {
            this.dsl_p = Bukkit.getServer().getPluginManager().getPlugin("DragonSlayer");
        }
        if (this.dsl_p != null) {
            this.dsl = true;
            this.getLogger().info("DragonSlayer found, will be used!");
        }
        try {
            Class.forName("org.bukkit.attribute.Attribute");
            this.dragonListener = new DragonEvents(this);
            this.getServer().getPluginManager().registerEvents(this.dragonListener, this);
            this.MinVersion19 = true;
        } catch (final NoClassDefFoundError | ClassNotFoundException ex) {
        }
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            this.spigot = true;
        } catch (final NoClassDefFoundError | ClassNotFoundException ex2) {
            this.logger.severe("org.spigotmc.SpigotConfig not found, disabling");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
        if (this.PAPIenabled) {
            final DragonPlaceholderAPI DragonPlaceholderAPI = new DragonPlaceholderAPI(this);
            DragonPlaceholderAPI.register();
        }
        this.getCommand("dragontimer").setExecutor(this.dragonCommandExecutor);
        this.setDragonDefaults();
        if (this.PAPIenabled) {
            this.getLogger().info("PlaceholderAPI found, will be used!");
        }
        this.StartRepeatingTimer();
        if (DragonTimer.getSubVersion() >= 13) {
            for (final String DragonWorld : this.getMaplist()) {
                this.getDragonCount(DragonWorld);
            }
        }
        if (this.getConfig().contains("global.debug") && this.getConfig().isBoolean("global.debug"))
            this.debugOn = this.getConfig().getBoolean("global.debug");
    }

    @Override
    public void onDisable() { this.saveConfig(); }

    public void loadConfiguration() {
        boolean headerSet = false;
        if (DragonTimer.getSubVersion() >= 18) {
            try {
                this.getConfig().options().getHeader();
                this.saveDefaultConfig();
                this.getConfig().options().copyDefaults(true);
                final List<String> headerStrings = this.getConfig().options().getHeader();
                final InputStreamReader reader_defConf = new InputStreamReader(this.getResource("config.yml"));
                final YamlConfiguration defConf = YamlConfiguration.loadConfiguration(reader_defConf);
                final List<String> headerStrings_neu = defConf.options().getHeader();
                if (!headerStrings.equals(headerStrings_neu)) {
                    this.getConfig().options().setHeader(headerStrings_neu);
                }
                headerSet = true;
                for (final String confPunkt : this.getConfig().getKeys(true)) {
                    final List<String> comment = this.getConfig().getComments(confPunkt);
                    final List<String> comment_def = defConf.getComments(confPunkt);
                    if (!comment.equals(comment_def) && comment_def != null) {
                        this.getConfig().setComments(confPunkt, comment_def);
                    }
                    final List<String> inl_comment = this.getConfig().getInlineComments(confPunkt);
                    final List<String> inl_comment_def = defConf.getInlineComments(confPunkt);
                    if (!inl_comment.equals(inl_comment_def) && inl_comment_def != null) {
                        this.getConfig().setInlineComments(confPunkt, inl_comment_def);
                    }
                }
            } catch (final NoSuchMethodError noSuchMethodError) {
            }
        }
        if (!headerSet) {
            this.getConfig().options().copyDefaults(true);
            this.getConfig().options().parseComments(true);
            this.saveDefaultConfig();
        }
        this.saveConfig();
    }

    public void StartRepeatingTimer() {

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            final Calendar now = Calendar.getInstance();
            if (now.get(13) == 0 || (now.get(13) > 0 && this.wasrunning != now.get(12))) {
                this.wasrunning = now.get(12);

                final Iterator<String> iterator = this.getMaplist().iterator();
                while (iterator.hasNext()) {
                    final String Mapname = iterator.next();
                    if (this.getSpawntimeMatch(Mapname)) {
                        final List<Player> Players = this.getDragonWorldFromString(Mapname).getPlayers();
                        final int Playercount = Players.size();
                        final int minpl = this.getMinPlayers(Mapname);
                        if (Playercount > 0 || minpl == 0) {
                            int MissingDrags = this.getMaxDragons(Mapname) - this.getDragonCount(Mapname);
                            if (Playercount >= minpl) {
                                if (this.getDragonTimelimit(Mapname) > 0L && !this.TimelimitActive.containsKey(Mapname)) {
                                    this.StartTimelimit(Mapname);
                                }
                                if (MissingDrags > 0) {
                                    if (this.getOneByOne(Mapname)) {
                                        MissingDrags = 1;
                                    }
                                    final int refreshDelay = this.getRefreshDelay(Mapname);
                                    if (this.getDSL() && this.MinVersion19 && refreshDelay >= 0) {
                                        ((DragonSlayer) this.dsl_p).WorldRefresh(Mapname);
                                        final int missD = MissingDrags;
                                        this.getServer().getScheduler().runTaskLater(this, () -> this.SpawnXDragons(missD, Mapname),
                                                refreshDelay * 1200);
                                    } else {
                                        this.SpawnXDragons(MissingDrags, Mapname);
                                    }
                                    final String Message = this
                                            .replaceValues(this.getConfig().getString("messages.dragonstospawn"), Mapname)
                                            .replace("$amount", String.valueOf(MissingDrags))
                                            .replace("$delay", (refreshDelay >= 0) ? String.valueOf(refreshDelay) : "0");
                                    if (Message != null && !Message.isEmpty()) {
                                        this.getServer().broadcastMessage(Message);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            } else if (MissingDrags > 0) {

                                final Iterator<Player> iterator2 = Players.iterator();
                                while (iterator2.hasNext()) {
                                    final Player player = iterator2.next();
                                    final String Message2 = this
                                            .replaceValues(this.getConfig().getString("messages.notenoughplayers"), Mapname)
                                            .replace("$minplayers", String.valueOf(minpl));
                                    if (Message2 != null && !Message2.isEmpty()) {
                                        player.sendMessage(ChatColor.RED + Message2);
                                    }
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        final List<Player> Players2 = this.getDragonWorldFromString(Mapname).getPlayers();
                        final int Playercount2 = Players2.size();
                        final int minpl2 = this.getMinPlayers(Mapname);
                        if (Playercount2 > 0 && Playercount2 < minpl2) {
                            final int DragsAlive = this.getDragonCount(Mapname);
                            final int MissingDrags2 = this.getMaxDragons(Mapname) - DragsAlive;
                            if (MissingDrags2 > 0 && this.RunCounter == 1) {

                                final Iterator<Player> iterator3 = Players2.iterator();
                                while (iterator3.hasNext()) {
                                    final Player player2 = iterator3.next();
                                    final String Message3 = this
                                            .replaceValues(this.getConfig().getString("messages.notenoughplayers"), Mapname)
                                            .replace("$minplayers", String.valueOf(minpl2));
                                    if (Message3 != null && !Message3.isEmpty()) {
                                        player2.sendMessage(ChatColor.RED + Message3);
                                    }
                                }
                            }
                            if (this.getDragonRemove(Mapname) && !this.RemoveInProgress.containsKey(Mapname) && DragsAlive > 0) {
                                this.RemoveDelay(Mapname);
                            }
                        }
                        ++this.RunCounter;
                        if (this.RunCounter >= 11) {
                            this.RunCounter = 0;
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (!this.timerDisplays.containsKey("none")) {
                this.addTimerDisplay("none");
            }

            final Iterator<String> iterator4 = this.getMaplist().iterator();
            while (iterator4.hasNext()) {
                final String Mapname2 = iterator4.next();
                if (!this.timerDisplays.containsKey(Mapname2)) {
                    this.addTimerDisplay(Mapname2);
                }
                this.updateTimerDisplay(Mapname2);
            }
        }, 400L, 20L);
    }

    public void PlaceholderApi() {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            this.PAPIenabled = false;
        } else {
            this.PAPIenabled = true;
        }
    }

    private boolean getSpawntimeMatch(final String Mapname) {
        final String[] spawnTimes = this.getSpawnTimerList(Mapname);
        String[] array;
        for (int length = (array = spawnTimes).length, i = 0; i < length; ++i) {
            String timeString = array[i];
            timeString = timeString.trim();
            if (timeString.length() >= 4) {
                if (!timeString.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")) {
                    this.getServer().getPluginManager().getPlugin("DragonTimer").getLogger()
                            .warning("Incorrect time specification. The format is HH:MM in 24h time.");
                }
                final Calendar now = Calendar.getInstance();
                final String[] spawntime = timeString.split(":");
                if (!timeString.isEmpty() && Integer.parseInt(spawntime[0]) == now.get(11)
                        && Integer.parseInt(spawntime[1]) == now.get(12)) {
                    this.getLogger().info("It's Respawn time ! " + spawntime[0] + ":" + spawntime[1] + "!");
                    return true;
                }
            }
        }
        return false;
    }

    int getRefreshDelay(final String Mapname) { return this.getConfigInt(Mapname, "dsl_refreshdelay"); }

    public String getDragonDefaultName(final String Mapname) {
        return this.getConfig().getString("dragon." + Mapname + ".name").replace('&', '§');
    }

    public String getDragonName(final String Mapname) {
        this.cleanupDragonList();
        final String TestForName = this.getConfig().getString("dragon." + Mapname + ".name");
        if (TestForName == null) {
            this.getConfig().set("dragon." + Mapname + ".name", this.getConfig().getString("dragon._default.name"));
        }
        final int maxD = this.getMaxDragons(Mapname);
        final Set<EnderDragon> Testdrags = this.DragonList.keySet();
        final Set<String> NameDrags = new HashSet<String>();
        for (final EnderDragon Testdrag : Testdrags) {
            if (Testdrag.getWorld().getName().toLowerCase().equals(Mapname)) {
                NameDrags.add(Testdrag.getName().replaceAll("§[f0r]", "").trim());
            }
        }
        for (int i = 1; i <= maxD; ++i) {
            String TestAddName = this.getConfig().getString("dragon." + Mapname + ".name_" + i);
            if (TestAddName != null) {
                TestAddName = TestAddName.replace('&', '§');
                final String Testname = TestAddName.replaceAll("§[f0r]", "");
                if (!NameDrags.contains(Testname)) {
                    return TestAddName;
                }
            }
        }
        return this.getConfig().getString("dragon." + Mapname + ".name").replace('&', '§');
    }

    public double getDragonHealth(final String Mapname) {
        final double h = this.getConfigDouble(Mapname, "health");
        double maxH = 2048.0;
        if (this.spigot) {
            maxH = Bukkit.spigot().getConfig().getInt("settings.attribute.maxHealth.max");
        }
        if (h > 0.0 && h <= maxH) {
            return h;
        }
        this.getLogger().warning("Invalid dragon health set, reverting to default: 200 (100 hearts)");
        this.getConfig().set("dragon." + Mapname + ".health", 200.0);
        this.saveConfig();
        return 200.0;
    }

    public Integer getDragonCount(final String Mapname) {
        final Location DragSpawnPos = this.getDragonSpawn(Mapname);
        final World MyWorld = this.getDragonWorldFromString(Mapname);
        if (MyWorld == null) {
            return 0;
        }
        this.activateChunksAroundPosition(DragSpawnPos, MyWorld, 12);
        int Counter = 0;
        final Collection<EnderDragon> dragons = MyWorld.getEntitiesByClass(EnderDragon.class);
        Counter = dragons.size();
        for (final EnderDragon dr : dragons) {
            if (!dr.isValid() || dr.isDead() || (this.MinVersion19 && dr.getPhase() == EnderDragon.Phase.DYING)) {
                --Counter;
            }
        }
        return Counter;
    }

    private void activateChunksAroundPosition(final Location StartPos, final World World, final int Radius) {
        if (World == null) {
            return;
        }
        final int baseX = (int) (StartPos.getX() / 16.0);
        final int baseZ = (int) (StartPos.getZ() / 16.0);
        for (int x = -1 * Radius; x <= Radius; ++x) {
            final int testX = baseX + x;
            for (int z = -1 * Radius; z <= Radius; ++z) {
                final int testZ = baseZ + z;
                if (this.keepChunksLoaded() && DragonTimer.getSubVersion() >= 13) {
                    try {
                        World.addPluginChunkTicket(testX, testZ, this);
                    } catch (final NoSuchMethodError e) {
                        try {
                            World.setChunkForceLoaded(testX, testZ, true);
                        } catch (final NoSuchMethodError noSuchMethodError) {
                        }
                    }
                }
                if (!World.isChunkLoaded(testX, testZ)) {
                    boolean load;
                    try {
                        load = World.loadChunk(testX, testZ, true);
                    } catch (final IllegalStateException e2) {
                        load = false;
                    }
                    if (!load) {
                        this.getLogger().warning(
                                "Failed to load and activate Chunk at X: " + testX * 16 + " Z: " + testZ * 16 + " in " + World.getName());
                    }
                }
            }
        }
    }

    boolean removeAllDragons(final String Mapname) {
        final Location DragSpawnPos = this.getDragonSpawn(Mapname);
        final World MyWorld = this.getDragonWorldFromString(Mapname);
        Collection<EnderDragon> dragons = null;
        if (this.dsl) {
            dragons = ((DragonSlayer) this.dsl_p).getDragonList(MyWorld, Mapname);
        }
        if (dragons == null) {
            this.activateChunksAroundPosition(DragSpawnPos, MyWorld, 12);
            dragons = MyWorld.getEntitiesByClass(EnderDragon.class);
        }
        boolean found = false;
        for (final EnderDragon dragon : dragons) {
            this.OrigEnderDragonSetKilled(dragon, true);
            if (this.dsl) {
                DragonSlayer.resetDragonsBossbar(dragon);
                dragon.remove();
            } else {
                this.resetDragonsBossbar(dragon);
                dragon.remove();
            }
            found = true;
        }
        return found;
    }

    public void RemoveDelay(final String ThisWorldsName) {
        final long delay = this.getRemoveDelay(ThisWorldsName);
        final String Message = this.replaceValues(this.getConfig().getString("messages.dragonsremwarn"), ThisWorldsName)
                .replace("$removedelay", String.valueOf(delay));
        if (Message != null && !Message.isEmpty()) {
            this.getServer().broadcastMessage(Message);
        }
        this.RemoveInProgress.put(ThisWorldsName, 1);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final int Playercount = this.getDragonWorldFromString(ThisWorldsName).getPlayers().size();
            final int minplayers = this.getMinPlayers(ThisWorldsName);
            this.RemoveInProgress.remove(ThisWorldsName);
            if (Playercount > 0 && Playercount < minplayers && this.removeAllDragons(ThisWorldsName)) {
                final String Message_ = this.replaceValues(this.getConfig().getString("messages.dragonsremoved"), ThisWorldsName);
                if (Message_ != null && !Message_.isEmpty()) {
                    this.getServer().broadcastMessage(Message_);
                }
            }
        }, delay * 60L * 20L + 1L);
    }

    public void StartTimelimit(final String ThisWorldsName) {
        final long TimeLimit = this.getDragonTimelimit(ThisWorldsName);
        this.TimelimitActive.put(ThisWorldsName, 1);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final int Playercount = this.getDragonWorldFromString(ThisWorldsName).getPlayers().size();
            final int minplayers = this.getMinPlayers(ThisWorldsName);
            if (Playercount >= minplayers) {
                if (this.getDragonCount(ThisWorldsName) > 0) {
                    this.RemoveDelayForTimelimit(ThisWorldsName);
                    final long delay = this.getRemoveDelay(ThisWorldsName);
                    final String Message = this.replaceValues(this.getConfig().getString("messages.timelimitwarn"), ThisWorldsName)
                            .replace("$removedelay", String.valueOf(delay));
                    if (Message != null && !Message.isEmpty()) {
                        this.getServer().broadcastMessage(Message);
                    }
                } else {
                    this.TimelimitActive.remove(ThisWorldsName);
                }
            } else {
                this.TimelimitActive.remove(ThisWorldsName);
            }
        }, TimeLimit * 60L * 20L + 1L);
    }

    public void RemoveDelayForTimelimit(final String ThisWorldsName) {
        final String key = ThisWorldsName;
        final long delay = this.getRemoveDelay(ThisWorldsName);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final int Playercount = this.getDragonWorldFromString(key).getPlayers().size();
            final int minplayers = this.getMinPlayers(key);
            if (Playercount >= minplayers && this.removeAllDragons(key)) {
                final String Message = this.replaceValues(this.getConfig().getString("messages.timelimit"), key);
                if (Message != null && !Message.isEmpty()) {
                    this.getServer().broadcastMessage(Message);
                }
            }
            this.TimelimitActive.remove(key);
        }, delay * 60L * 20L + 1L);
    }

    public void setDefaults(final String Mapname) {
        this.getMaxDragons(Mapname);
        this.getMinPlayers(Mapname);
    }

    public void SpawnForceAllDragons() {
        this.cleanupDragonList();
        for (final String DragonWorld : this.getMaplist()) {
            final int ExistentDragons = this.getDragonCount(DragonWorld);
            final int MaxiDragons = this.getMaxDragons(DragonWorld);
            if (ExistentDragons < MaxiDragons) {
                this.SpawnXDragons(MaxiDragons - ExistentDragons, DragonWorld);
            }
        }
    }

    public void SpawnXDragons(final int x, final String World) {
        for (int i = 0; i < x; ++i) {
            final DragonRespawn Resp = new DragonRespawn(this);
            Resp.Mapname = World;
            this.getServer().getScheduler().runTaskLater(this, Resp, 180 + i * 40);
        }
    }

    public String[] getSpawnTimerList(final String Mapname) {
        final Set<String> Timers = this.SpawnTimerMap.keySet();
        final String[] SpawnTimeString = this.getTimeArrayFromCfgTimestring(Mapname);
        if (!Timers.contains(Mapname)) {
            this.SpawnTimerMap.put(Mapname, SpawnTimeString);
            this.saveTimeArrayToConfig(Mapname, SpawnTimeString);
        }
        return this.SpawnTimerMap.get(Mapname);
    }

    public String[] getTimeArrayFromCfgTimestring(final String Mapname) {
        String[] newSpawnTimeString = new String[0];
        final String CfgSpawntimes = this.getConfigString(Mapname, "spawntimes");
        if (CfgSpawntimes.contains(",")) {
            newSpawnTimeString = CfgSpawntimes.split(",");
        } else {
            newSpawnTimeString = new String[] { CfgSpawntimes };
        }
        if (newSpawnTimeString.length == 0 || (newSpawnTimeString.length == 1 && newSpawnTimeString[0].isEmpty())) {
            newSpawnTimeString = new String[] { "12:00" };
        }
        return newSpawnTimeString;
    }

    private String[] sortTimeArray(final String[] array) {
        return Arrays.asList(array).stream().sorted().collect(Collectors.toList()).toArray(new String[0]);
    }

    private void saveTimeArrayToConfig(final String Mapname, String[] SpawnTimeString) {
        SpawnTimeString = this.sortTimeArray(SpawnTimeString);
        final String NewTimeString = Arrays.toString(SpawnTimeString).replaceAll("[\\[ \\]]", "");
        this.getConfig().set("dragon." + Mapname + ".spawntimes", NewTimeString);
        this.saveConfig();
    }

    public String getNextSpawnTime(final String ThisWorld) {
        if (!this.checkWorld(ThisWorld)) {
            return "";
        }
        int firstNextTime;
        int NextTime = firstNextTime = 2560;
        final Calendar now = Calendar.getInstance();
        final int now2 = now.get(11) * 100 + now.get(12);
        final String[] SpawnTimeArray = this.getTimeArrayFromCfgTimestring(ThisWorld);
        if (SpawnTimeArray.length != 0) {
            String[] array;
            for (int length = (array = SpawnTimeArray).length, i = 0; i < length; ++i) {
                final String oneTime = array[i];
                if (oneTime.length() >= 4) {
                    final int oneTimeInteger = Integer.parseInt(oneTime.trim().replace(":", ""));
                    if (oneTimeInteger < firstNextTime) {
                        firstNextTime = oneTimeInteger;
                    }
                    if (oneTimeInteger > now2 && oneTimeInteger < NextTime) {
                        NextTime = oneTimeInteger;
                    }
                }
            }
            if (NextTime == 2560) {
                NextTime = firstNextTime;
            }
            final String NextTimeSt = String.format("%04d", NextTime);
            return String.valueOf(NextTimeSt.substring(0, NextTimeSt.length() - 2)) + ":" + NextTimeSt.substring(NextTimeSt.length() - 2);
        }
        return "";
    }

    public World getDragonWorldFromString(final String Mapname) { return Bukkit.getServer().getWorld(Mapname); }

    public Location getDragonSpawn(final String Mapname) {
        return new Location(this.getDragonWorldFromString(Mapname), this.getConfig().getDouble("spawnpoint." + Mapname + ".x"),
                this.getConfig().getDouble("spawnpoint." + Mapname + ".y"), this.getConfig().getDouble("spawnpoint." + Mapname + ".z"));
    }

    public long getDragonTimelimit(final String world) { return this.getConfigLong(world, "timelimit"); }

    public boolean getDragonRemove(final String world) { return this.getConfigBoolean(world, "removedragons"); }

    public int getMaxDragons(final String world) { return this.getConfigInt(world, "maxdragons"); }

    public int getMinPlayers(final String world) { return this.getConfigInt(world, "minplayers"); }

    public Long getRemoveDelay(final String world) { return this.getConfigLong(world, "removedelay"); }

    public String getDragonCommand(final String world) { return this.replaceValues(this.getConfigString(world, "command"), world); }

    public boolean getNoGuarding(final String world) { return this.getConfigBoolean(world, "noguarding"); }

    public boolean getTimerdisplay(final String world) { return this.getConfigBoolean(world, "timerdisplay"); }

    public boolean getOneByOne(final String world) { return this.getConfigBoolean(world, "onebyone"); }

    boolean getConfigBoolean(final String Mapname, final String var) { return Boolean.parseBoolean(this.getConfigString(Mapname, var)); }

    public int getConfigInt(final String Mapname, final String var) { return Integer.parseInt(this.getConfigString(Mapname, var)); }

    private double getConfigDouble(final String Mapname, final String var) {
        return Double.parseDouble(this.getConfigString(Mapname, var));
    }

    private long getConfigLong(final String Mapname, final String var) { return Long.parseLong(this.getConfigString(Mapname, var)); }

    private String getConfigString(final String Mapname, final String var) {
        final String TestWord = this.getConfig().getString("dragon." + Mapname + "." + var);
        if (TestWord == null) {
            this.getConfig().set("dragon." + Mapname + "." + var, this.getConfig().getString("dragon._default." + var));
        }
        return this.getConfig().getString("dragon." + Mapname + "." + var);
    }

    public String replaceValues(String s, final String world) {
        s = s.replace('&', '§');
        if (world != null) {
            final String baseworld = world.replace("_the_end", "");
            s = s.replace("$world", world).replace("$baseworld", baseworld).replace("$dragon",
                    this.getDragonDefaultName(world.toLowerCase()));
        } else {
            s = s.replace("$world", "-No World-").replace("$dragon", this.getConfig().getString("dragon._default.name")).replace("$reward",
                    this.getConfig().getString("dragon._default.reward"));
        }
        return s;
    }

    public Set<String> getMaplist() {
        Set<String> WorldsList = new HashSet<String>();
        if (this.getConfig().isConfigurationSection("spawnpoint")) {
            WorldsList = this.getConfig().getConfigurationSection("spawnpoint").getKeys(false);
        }
        if (WorldsList.contains("world")) {
            WorldsList.remove("world");
        }
        return WorldsList;
    }

    void setDragonDefaults() {
        for (final String Mapname : this.getMaplist()) {
            this.getDragonName(Mapname);
            this.getDragonHealth(Mapname);
            this.getDragonCommand(Mapname);
            this.getDragonRemove(Mapname);
            this.getNoGuarding(Mapname);
            this.getRemoveDelay(Mapname);
            this.getTimerdisplay(Mapname);
            this.getOneByOne(Mapname);
            this.getSpawnTimerList(Mapname);
            this.setDefaults(Mapname);
            this.getRefreshDelay(Mapname);
        }
        this.saveConfig();
    }

    public boolean checkWorld(final String World) { return this.getMaplist().contains(World.toLowerCase()); }

    boolean getDSL() { return this.dsl; }

    boolean checkDSLWorld(final String World) {
        boolean check = false;
        if (this.dsl && this.MinVersion19) {
            check = ((DragonSlayer) this.dsl_p).checkWorld(World);
        }
        return check;
    }

    private boolean getDark(final String Mapname) {
        return this.getConfig().getBoolean("dragon." + Mapname + ".darkness",
                Boolean.parseBoolean(this.getConfig().getString("dragon._default.darkness")));
    }

    private boolean keepChunksLoaded() { return this.getConfig().getBoolean("global.keepchunks", true); }

    public BossBar findFreeBar(final String worldname) {
        for (final BossBar BB : this.BossBars) {
            if (!BB.isVisible()) {
                BB.setVisible(true);
                this.setBBdark(BB, worldname);
                return BB;
            }
        }
        final BossBar BossBar = Bukkit.getServer().createBossBar("EnderDragon", BarColor.PURPLE, BarStyle.SOLID,
                new BarFlag[] { BarFlag.PLAY_BOSS_MUSIC });
        this.setBBdark(BossBar, worldname);
        BossBar.setVisible(true);
        this.BossBars.add(BossBar);
        return BossBar;
    }

    private void setBBdark(final BossBar BossBar, final String worldname) {
        if (this.getDark(worldname)) {
            BossBar.addFlag(BarFlag.CREATE_FOG);
            BossBar.addFlag(BarFlag.DARKEN_SKY);
        } else {
            BossBar.removeFlag(BarFlag.CREATE_FOG);
            BossBar.removeFlag(BarFlag.DARKEN_SKY);
        }
    }

    public void putBossBarToDragon(final EnderDragon ThisDragon, final BossBar BossBar) { this.DragonBarList.put(ThisDragon, BossBar); }

    public void putDragonToList(final EnderDragon ThisDragon, final Integer Int) { this.DragonList.put(ThisDragon, Int); }

    public void delBossBarFromDragon(final EnderDragon ThisDragon) { this.DragonBarList.remove(ThisDragon); }

    public void delDragonFromList(final EnderDragon ThisDragon) { this.DragonList.remove(ThisDragon); }

    public BossBar getBossBarFromDragon(final EnderDragon ThisDragon) {
        BossBar BossBar = null;
        try {
            BossBar = this.DragonBarList.get(ThisDragon);
        } catch (final Exception ex) {
        }
        return BossBar;
    }

    public void resetDragonsBossbar(final Entity Dragon) {
        final BossBar BossBar = this.getBossBarFromDragon((EnderDragon) Dragon);
        if (BossBar != null) {
            BossBar.setProgress(0.0);
            BossBar.removeAll();
            this.delBossBarFromDragon((EnderDragon) Dragon);
            BossBar.setVisible(false);
        }
    }

    public void cleanupDragonList() {
        final Set<EnderDragon> Testdrags = this.DragonList.keySet();
        final Set<EnderDragon> ListDelDrags = new HashSet<EnderDragon>();
        for (final EnderDragon Testdrag : Testdrags) {
            if (!Testdrag.isValid()) {
                ListDelDrags.add(Testdrag);
            }
        }
        for (final EnderDragon Testdrag : ListDelDrags) {
            this.DragonList.remove(Testdrag);
        }
    }

    public void cleanupDragons() {
        final Set<EnderDragon> Testdrags = this.DragonBarList.keySet();
        final Set<EnderDragon> ListDelDrags = new HashSet<EnderDragon>();
        for (final EnderDragon Testdrag : Testdrags) {
            if (!Testdrag.isValid()) {
                final BossBar tobedeleted = this.getBossBarFromDragon(Testdrag);
                tobedeleted.setVisible(false);
                ListDelDrags.add(Testdrag);
            }
        }
        for (final EnderDragon Testdrag : ListDelDrags) {
            this.DragonBarList.remove(Testdrag);
        }
    }

    public void deletePlayersBossBars(final Player player) {
        for (final BossBar BB : this.BossBars) {
            if (BB.getPlayers().contains(player)) {
                BB.removePlayer(player);
            }
        }
    }

    public void setBossBarAmount(final EnderDragon e) {
        final String w = e.getWorld().getName();
        if (this.checkWorld(w)) {
            final BossBar BossBar = this.getBossBarFromDragon(e);
            if (BossBar != null) {
                this.setBossBarAmountNOW(e, BossBar);
            }
        }
    }

    public void setBossBarAmountNOW(final EnderDragon e, final BossBar BossBar) {
        final EnderDragon enderDragon = e;
        final BossBar bossBar = BossBar;
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final double MaxHealth = enderDragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            final double DragHealth = enderDragon.getHealth();
            double BarHealthValue = DragHealth / MaxHealth;
            if (BarHealthValue < 0.0) {
                BarHealthValue = 0.0;
            }
            if (BarHealthValue > 1.0) {
                BarHealthValue = 1.0;
            }
            bossBar.setProgress(BarHealthValue);
        }, 0L);
    }

    public void AtKillCommand(final String ThisWorldsName, final Player player, final String DragonName) {
        if (ThisWorldsName != null && this.checkWorld(ThisWorldsName.toLowerCase())) {
            this.getServer().getScheduler().runTaskLater(this, () -> {
                final World ThisWorld = Bukkit.getServer().getWorld(ThisWorldsName);
                final String ThisWorldName = ThisWorld.getName();
                String command = this.getDragonCommand(ThisWorldsName.toLowerCase());
                if (command != null && !command.isEmpty()) {
                    final String PN = player == null ? "" : player.getName();
                    command = command.replace("$player", PN).replace(ThisWorldsName.toLowerCase(), ThisWorldName)
                            .replace(this.getDragonDefaultName(ThisWorldsName.toLowerCase()), String.valueOf(DragonName) + "§r");
                    final String[] stringArray = command.split(";");
                    final int n = stringArray.length;
                    int n2 = 0;
                    while (n2 < n) {
                        final String command2 = stringArray[n2];
                        this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command2);
                        this.getServer().getPluginManager().getPlugin("DragonTimer").getLogger()
                                .info(ChatColor.GREEN + "In the world " + ThisWorldName + " Command: '" + command2 + "' was executed...");
                        ++n2;
                    }
                }
            }, 60L);
        }
    }

    public static String getVersion() {
        final Pattern Version = Pattern.compile("\\d\\.\\d\\d\\.\\d");
        final Matcher matche = Version.matcher(Bukkit.getBukkitVersion());

        return matche.hasMatch() ? matche.group() : "1.20.6";
    }

    static int getSubVersion() {
        if (DragonTimer.Sub == -1) {
            final String version = DragonTimer.getVersion();
            DragonTimer.Sub = Integer.parseInt(version.substring(version.indexOf(".") + 1, version.lastIndexOf(".")));
        }
        return DragonTimer.Sub;
    }

    /** {@link net.minecraft.world.level.dimension.end.EndDragonFight} */
    protected Object getEnderDragonBattle(final World ThisWorld) {
        try {
            final Object worldServer = this.getWorldServer(ThisWorld);
            if (this.getEDBMethod == null) {
                this.getEDBMethod = this.getMethodByReturntype(worldServer.getClass(), "EndDragonFight", (Class<?>[]) null);
            }

            Object edb = this.getEDBMethod.invoke(worldServer);
            if (edb == null && ThisWorld.getEnvironment() == Environment.THE_END) {
                try {
                    final long ws_long = ThisWorld.getSeed();
                    final Object emptyNBT = Class.forName("net.minecraft.nbt.CompoundTag").newInstance();
                    edb = Class.forName("net.minecraft.world.level.dimension.end.EndDragonFight")
                            .getConstructor(worldServer.getClass(), Long.TYPE, emptyNBT.getClass())
                            .newInstance(worldServer, ws_long, emptyNBT);

                    final Field ws_edb_f = this.getFieldByType(worldServer.getClass(), "EndDragonFight");
                    ws_edb_f.setAccessible(true);
                    ws_edb_f.set(worldServer, edb);

                    this.logger.warning("Started Hot-Fix for DragonBattle in world " + ThisWorld.getName());

                } catch (final InstantiationException var7) {
                    this.logger.warning(
                            "unsupported Version :" + DragonTimer.getVersion() + ", can't create own dragonbattle for this version...");
                    if (this.debugOn) {
                        var7.printStackTrace();
                    }
                }
            }

            return edb;
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var8) {
            this.logger.warning("Unknown or unsupported Version :" + DragonTimer.getVersion() + ", can't handle dragonbattle...(yet?)");
            if (this.debugOn) {
                var8.printStackTrace();
            }
        }
        return null;
    }

    private Field getFieldByType(final Class<?> _class, final String returnType) { return this.getFieldByType(_class, returnType, false); }

    private Field getFieldByType(final Class<?> _class, final String returnType, final boolean onlyPublics) {
        final Field[] allFields = onlyPublics ? _class.getFields() : _class.getDeclaredFields();

        for (final Field field : allFields) {
            if (field.getGenericType().getTypeName().endsWith(returnType)) {
                return field;
            }
        }

        if (this.debugOn) {
            this.logger.warning("No Field found for " + returnType);
        }

        return null;
    }

    Method getMethodByReturntype(final Class<?> _class, final String returnType, final Class<?>[] parameters) {
        return this.getMethodByReturntype(_class, returnType, parameters, false);
    }

    Method getMethodByReturntype(final Class<?> _class, final String returnType, final Class<?>[] parameters, final boolean noRaw) {
        final Method[] allMeth = _class.getMethods();

        label47: for (final Method meth : allMeth) {
            if (parameters != null) {
                final int paramLength = parameters.length;
                final Class<?>[] methParameters = meth.getParameterTypes();
                if (methParameters == null || paramLength != methParameters.length) {
                    continue;
                }

                for (int i = 0; i < paramLength; ++i) {
                    if (!parameters[i].equals(methParameters[i])) {
                        continue label47;
                    }
                }
            }

            if ((!noRaw || !meth.getName().toLowerCase().endsWith("raw")) && meth.getReturnType().getSimpleName().equals(returnType)) {
                return meth;
            }
        }

        if (this.debugOn)
            this.logger.warning("No Methode found for " + returnType);

        return null;
    }

    /** {@link net.minecraft.server.level.ServerLevel} */
    Object getWorldServer(final World ThisWorld) {
        try {
            final Object castClass = this.getCraftWorld(ThisWorld);
            return this.CraftWorldClass.getDeclaredMethod("getHandle").invoke(castClass);
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException var3) {
            if (this.debugOn) {
                var3.printStackTrace();
            }

            return null;
        }
    }

    Object getCraftWorld(final World ThisWorld) {
        try {
            if (this.CraftWorldClass == null) {
                this.CraftWorldClass = (Class<?>) Class.forName("org.bukkit.craftbukkit.CraftWorld");
            }

            if (this.CraftWorldClass.isInstance(ThisWorld)) {
                return this.CraftWorldClass.cast(ThisWorld);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | ClassNotFoundException var3) {
            if (this.debugOn)
                var3.printStackTrace();

        }

        return null;
    }

    /** {@link net.minecraft.world.entity.boss.enderdragon.EnderDragon} */
    @SuppressWarnings("unchecked")
    Object getEntityEnderDragon(final EnderDragon dragon) {
        Object returnwert = null;

        try {
            if (this.CraftEnderDragonClass == null) {
                this.CraftEnderDragonClass = (Class<CraftEnderDragon>) Class.forName("org.bukkit.craftbukkit.entity.CraftEnderDragon");
            }

            if (this.CraftEnderDragonClass.isInstance(dragon)) {
                final Object craftDragon = this.CraftEnderDragonClass.cast(dragon);
                returnwert = this.CraftEnderDragonClass.getDeclaredMethod("getHandle").invoke(craftDragon);
            }
        } catch (final Exception var4) {
            this.logger
                    .warning("Unknown or unsupported Version :" + DragonTimer.getVersion() + ", can't handle EntityEnderDragon...(yet?)");
            if (this.debugOn) {
                var4.printStackTrace();
            }
        }

        return returnwert;
    }

    void OrigEnderDragonSetKilled(final EnderDragon ThisDragon, final boolean killed) {
        this.OrigEnderDragonSetKilled(ThisDragon.getWorld(), killed);
    }

    void OrigEnderDragonSetKilled(final World ThisWorld, final boolean killed) {
        final int sv = DragonTimer.getSubVersion();
        if (sv <= 8) {
            return;
        }
        final Object DrBatt = this.getEnderDragonBattle(ThisWorld);
        if (DrBatt != null) {
            try {
                if (this.DragonKilled == null) {
                    String DragonKilled_ = "k";
                    if (sv == 16) {
                        DragonKilled_ = "dragonKilled";
                    } else if (sv >= 20) {
                        DragonKilled_ = "t";
                    } else if (sv >= 17) {
                        DragonKilled_ = "s";
                    }
                    this.DragonKilled = DrBatt.getClass().getDeclaredField(DragonKilled_);
                }
                if (this.DragonKilled != null) {
                    this.DragonKilled.setAccessible(true);
                    this.DragonKilled.setBoolean(DrBatt, killed);
                }
            } catch (final IllegalAccessException | NoSuchFieldException | SecurityException | NullPointerException ee) {
                this.logger.warning("Unknown or unsupported Version :" + DragonTimer.getVersion() + " ,can't handle this here...(yet?)");
            }
        }
    }

    public String[] getWorldsNextSpawns(final String Mapname) {
        final String nextSpawn = this.getNextSpawnTime(Mapname);
        if (!nextSpawn.isEmpty()) {
            final String[] nextSpawnArray = nextSpawn.split(":");
            final int nextTime = Integer.parseInt(nextSpawn.replace(":", ""));
            final int nextHour = Integer.parseInt(nextSpawnArray[0]);
            final int nextMinute = Integer.parseInt(nextSpawnArray[1]);
            final Calendar now = Calendar.getInstance();
            final int nowHour = now.get(11);
            final int nowMinute = now.get(12);
            final int nowSecond = now.get(13);
            final int nowTime = Integer.parseInt(String.format("%02d%02d", nowHour, nowMinute));
            int hours = (nextTime > nowTime) ? (nextHour - nowHour) : (nextHour - nowHour + 24);
            int minutes = nextMinute - nowMinute;
            hours = ((minutes > 0) ? hours : (hours - 1));
            minutes = ((minutes > 0) ? minutes : (60 + minutes)) - 1;
            final int seconds = 59 - nowSecond;
            return String.format("%02d,%02d,%02d", hours, minutes, seconds).split(",");
        }
        return null;
    }

    private void addTimerDisplay(final String ThisWorld) {
        final Scoreboard timerDisplay = this.getServer().getScoreboardManager().getNewScoreboard();
        Objective ScoreObj;
        ScoreObj = timerDisplay.registerNewObjective("DTI", "DTITimer", ChatColor.GREEN + "Next Spawn Time");

        if (!ThisWorld.equals("none")) {
            ScoreObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            ScoreObj.setDisplayName("");
        }
        this.timerDisplays.put(ThisWorld, timerDisplay);
        final World world = Bukkit.getWorld(ThisWorld);
        if (world != null) {
            final Collection<Player> PlayerList = world.getPlayers();
            for (final Player player : PlayerList) {
                if (this.getTimerdisplay(ThisWorld)) {
                    player.setScoreboard(timerDisplay);
                }
            }
        }
    }

    private void updateTimerDisplay(final String ThisWorld) {
        if (this.timerDisplays.containsKey(ThisWorld) && this.timerDisplays.get(ThisWorld).getObjective("DTI") != null) {
            final Scoreboard sb = this.timerDisplays.get(ThisWorld);
            final Objective ScoreObj = sb.getObjective("DTI");
            if (ScoreObj != null) {
                final String[] times = this.getWorldsNextSpawns(ThisWorld);
                if (times != null) {
                    final String hours = times[0];
                    final String minutes = times[1];
                    final String seconds = times[2];
                    String timerline = this.getTimerline();
                    if (timerline != null) {
                        if (timerline.contains("$") || timerline.isEmpty()) {
                            final Set<String> scoreSet = sb.getEntries();
                            for (final String score : scoreSet) {
                                sb.resetScores(score);
                            }
                            timerline = timerline.replace("$hours", hours).replace("$minutes", minutes).replace("$seconds", seconds);
                        }
                        Score tseconds;
                        if (!timerline.trim().isEmpty()) {
                            tseconds = ScoreObj.getScore(ChatColor.RED + timerline);
                        } else {
                            tseconds = ScoreObj.getScore(ChatColor.RED + "H:" + hours + " M:" + minutes + " S:");
                        }
                        tseconds.setScore(Integer.valueOf(seconds));
                    }
                    ScoreObj.setDisplayName(ChatColor.GREEN + this.getTimertext().replace("$hours", String.valueOf(hours))
                            .replace("$minutes", String.valueOf(minutes)).replace("$seconds", String.valueOf(seconds)));
                    return;
                }
                ScoreObj.setDisplayName(ChatColor.GREEN + "No Spawntimer");
                final Set<String> scoreSet2 = sb.getEntries();
                for (final String score2 : scoreSet2) {
                    sb.resetScores(score2);
                }
            }
        }
    }

    private String getTimertext() { return this.getConfig().getString("messages.timertext"); }

    private String getTimerline() { return this.getConfig().getString("messages.timerline"); }

    public String getNoTimerPlaceholder() { return this.getConfig().getString("messages.notimer_ph"); }

    public void setTimerdisplayToPlayer(final Player player) {
        final String ThisWorld = player.getWorld().getName().toLowerCase();
        if (!this.checkWorld(ThisWorld)) {
            return;
        }
        if (!this.getTimerdisplay(ThisWorld)) {
            return;
        }
        final Scoreboard TimerSB = this.timerDisplays.get(ThisWorld);
        if (TimerSB != null && TimerSB.getObjective("DTI") != null) {
            player.setScoreboard(TimerSB);
        }
    }

    public void delTimerdisplayFromPlayer(final Player player) {
        Scoreboard TimerSB = player.getScoreboard();
        if (TimerSB != null && TimerSB.getObjective("DTI") != null) {
            TimerSB = this.timerDisplays.get("none");
            if (TimerSB == null) {
                this.addTimerDisplay("none");
                TimerSB = this.timerDisplays.get("none");
            }
            player.setScoreboard(TimerSB);
        }
    }

}
