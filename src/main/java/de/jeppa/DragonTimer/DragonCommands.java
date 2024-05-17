
package de.jeppa.DragonTimer;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

@SuppressWarnings("deprecation")
public class DragonCommands implements CommandExecutor {
    DragonTimer plugin;

    public DragonCommands(final DragonTimer instance) { this.plugin = instance; }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final String boolVarList = "removedragons,onebyone,timerdisplay,noguarding";
        final String intVarList = "minplayers,maxdragons,health,removedelay,timelimit,dsl_refreshdelay";
        if (sender instanceof final Player player) {
            if (cmd.getName().equalsIgnoreCase("dragontimer")) {
                if (args.length == 0) {
                    if (!player.hasPermission("dragontimer")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission");
                        return false;
                    }
                    this.dragonSpawnHelp(player);
                } else if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("set")) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final Location loc = player.getLocation();
                            final double x = loc.getX();
                            final double y = loc.getY();
                            final double z = loc.getZ();
                            final String w = player.getWorld().getName().toLowerCase();
                            this.setDragonSpawn(player, x, y, z, w);
                            this.plugin.setDragonDefaults();
                        } else if (args.length >= 4 && args.length <= 5) {
                            if (DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                String w2 = null;
                                if (args.length == 5) {
                                    if (this.isWorld(args[4])) {
                                        w2 = args[4].trim().toLowerCase();
                                    } else {
                                        player.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                    }
                                } else {
                                    w2 = player.getWorld().getName().toLowerCase();
                                }
                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (w2 != null) {
                                    this.setDragonSpawn(player, x, y, z, w2);
                                    this.plugin.setDragonDefaults();
                                }
                            } else {
                                this.dragonSpawnHelp(player);
                            }
                        } else {
                            this.dragonSpawnHelp(player);
                        }
                    } else if (args[0].equalsIgnoreCase("remspawn") || args[0].equalsIgnoreCase("rem")) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String w2 = player.getWorld().getName().toLowerCase();
                            this.remDragonSpawn(player, w2);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String w2 = args[1].trim().toLowerCase();
                                this.remDragonSpawn(player, w2);
                            } else {
                                player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn") || args[0].equalsIgnoreCase("get")) {
                        if (!player.hasPermission("dragontimer.info")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = player.getWorld().getName().toLowerCase();
                            this.getDragonSpawn(player, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getDragonSpawn(player, ThisWorld);
                            } else {
                                player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("killdragons") || args[0].equalsIgnoreCase("kill")) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = player.getWorld().getName().toLowerCase();
                            this.killDragons(player, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.killDragons(player, ThisWorld);
                            } else {
                                player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rel")) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        this.plugin.reloadConfig();
                        this.plugin.loadConfiguration();
                        this.plugin.SpawnTimerMap.clear();
                        for (final Scoreboard timerDisplay : this.plugin.timerDisplays.values()) {
                            timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                        }
                        this.plugin.timerDisplays.clear();
                        player.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at") || args[0].equalsIgnoreCase("remtime")
                            || args[0].equalsIgnoreCase("rt")) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        boolean add = false;
                        if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at")) {
                            add = true;
                        }
                        if (args.length == 2) {
                            final String ThisWorld2 = player.getWorld().getName().toLowerCase();
                            this.addSpawnTime(player, args[1].trim(), ThisWorld2, add);
                        } else if (args.length == 3) {
                            if (this.isWorld(args[2])) {
                                final String ThisWorld2 = args[2].trim().toLowerCase();
                                this.addSpawnTime(player, args[1].trim(), ThisWorld2, add);
                            } else {
                                player.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("gettime") || args[0].equalsIgnoreCase("gt")) {
                        if (!player.hasPermission("dragontimer.info")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = player.getWorld().getName().toLowerCase();
                            this.getSpawnTime(player, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getSpawnTime(player, ThisWorld);
                            } else {
                                player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("getnexttime") || args[0].equalsIgnoreCase("gn")) {
                        if (!player.hasPermission("dragontimer.info")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        String ThisWorld = "";
                        if (args.length == 1) {
                            ThisWorld = player.getWorld().getName().toLowerCase();
                        } else if (args.length >= 2) {
                            if (!this.isWorld(args[1])) {
                                player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[1].trim().toLowerCase();
                        }
                        final String NextTime = this.plugin.getNextSpawnTime(ThisWorld);
                        if (!NextTime.isEmpty()) {
                            player.sendMessage(ChatColor.YELLOW + "Next spawn in world " + ThisWorld + " is at " + NextTime + " !");
                        } else {
                            player.sendMessage(ChatColor.RED + "No spawntime or wrong world :" + ThisWorld);
                        }
                    } else if (Arrays.asList(intVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1 || (args.length == 2 && this.isWorld(args[1].trim()))) {
                            String ThisWorld = player.getWorld().getName().toLowerCase();
                            if (args.length == 2) {
                                ThisWorld = args[1].trim().toLowerCase();
                            }
                            this.getConfigVar(player, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        String ThisWorld;
                        if (args.length == 2) {
                            ThisWorld = player.getWorld().getName().toLowerCase();
                        } else {
                            if (args.length != 3) {
                                player.sendMessage(ChatColor.RED + "Wrong syntax.");
                                return false;
                            }
                            if (!this.isWorld(args[2].trim())) {
                                player.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[2].trim().toLowerCase();
                        }
                        final String Arg1 = args[1].trim();
                        if (!DragonCommands.isInteger(Arg1)) {
                            player.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final int value = Integer.parseInt(Arg1);
                        this.setConfigVar(player, args[0].toLowerCase().trim(), value, ThisWorld);
                    } else if (Arrays.asList(boolVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (!player.hasPermission("dragontimer.admin")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1 || (args.length == 2 && this.isWorld(args[1].trim()))) {
                            String ThisWorld = player.getWorld().getName().toLowerCase();
                            if (args.length == 2) {
                                ThisWorld = args[1].trim().toLowerCase();
                            }
                            this.getConfigBoolean(player, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        String ThisWorld;
                        if (args.length == 2) {
                            ThisWorld = player.getWorld().getName().toLowerCase();
                        } else {
                            if (args.length != 3) {
                                player.sendMessage(ChatColor.RED + "Wrong syntax.");
                                return false;
                            }
                            if (!this.isWorld(args[2].trim())) {
                                player.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[2].trim().toLowerCase();
                        }
                        final String Arg1 = args[1].trim().toLowerCase();
                        if (!Arg1.equals("true") && !Arg1.equals("false")) {
                            player.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final boolean value2 = Boolean.parseBoolean(Arg1);
                        this.setConfigBoolean(player, args[0].toLowerCase().trim(), value2, ThisWorld);
                    } else if (args[0].equalsIgnoreCase("forceallrespawn") || args[0].equalsIgnoreCase("forceall")) {
                        if (!player.hasPermission("dragontimer.forcerespawn")) {
                            player.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        this.plugin.SpawnForceAllDragons();
                        player.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                    } else {
                        this.dragonSpawnHelp(player);
                    }
                }
            }
        } else {
            final ConsoleCommandSender console = (ConsoleCommandSender) sender;
            if (cmd.getName().equalsIgnoreCase("dragontimer")) {
                if (args.length == 0) {
                    this.dragonSpawnHelp(console);
                } else if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("set")) {
                        if (args.length == 5) {
                            if (DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                String world = null;
                                if (this.isWorld(args[4])) {
                                    world = args[4].trim().toLowerCase();
                                } else {
                                    console.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                }
                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (world != null) {
                                    this.setDragonSpawn(console, x, y, z, world);
                                    this.plugin.setDragonDefaults();
                                }
                            } else {
                                this.dragonSpawnHelp(console);
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("remspawn") || args[0].equalsIgnoreCase("rem")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String w2 = args[1].trim().toLowerCase();
                                this.remDragonSpawn(console, w2);
                            } else {
                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn") || args[0].equalsIgnoreCase("get")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getDragonSpawn(console, ThisWorld);
                            } else {
                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("killdragons") || args[0].equalsIgnoreCase("kill")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.killDragons(console, ThisWorld);
                            } else {
                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rel")) {
                        this.plugin.reloadConfig();
                        this.plugin.loadConfiguration();
                        this.plugin.SpawnTimerMap.clear();
                        for (final Scoreboard timerDisplay : this.plugin.timerDisplays.values()) {
                            timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                        }
                        this.plugin.timerDisplays.clear();
                        console.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at") || args[0].equalsIgnoreCase("remtime")
                            || args[0].equalsIgnoreCase("rt")) {
                        boolean add = false;
                        if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at")) {
                            add = true;
                        }
                        if (args.length == 3) {
                            if (this.isWorld(args[2])) {
                                final String ThisWorld2 = args[2].trim().toLowerCase();
                                this.addSpawnTime(console, args[1].trim(), ThisWorld2, add);
                            } else {
                                console.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("gettime") || args[0].equalsIgnoreCase("gt")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getSpawnTime(console, ThisWorld);
                            } else {
                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("getnexttime") || args[0].equalsIgnoreCase("gn")) {
                        String ThisWorld = "";
                        if (args.length >= 2) {
                            if (!this.isWorld(args[1])) {
                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[1].trim().toLowerCase();
                        }
                        final String NextTime = this.plugin.getNextSpawnTime(ThisWorld);
                        if (!NextTime.isEmpty()) {
                            console.sendMessage(ChatColor.YELLOW + "Next spawn in world " + ThisWorld + " is at " + NextTime + " !");
                        } else {
                            console.sendMessage(ChatColor.RED + "No spawntime or wrong world :" + ThisWorld);
                        }
                    } else if (Arrays.asList(intVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (args.length == 2 && this.isWorld(args[1].trim())) {
                            final String ThisWorld = args[1].trim().toLowerCase();
                            this.getConfigVar(console, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        if (args.length != 3) {
                            console.sendMessage(ChatColor.RED + "Wrong syntax.");
                            return false;
                        }
                        if (!this.isWorld(args[2].trim())) {
                            console.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            return false;
                        }
                        final String ThisWorld = args[2].trim().toLowerCase();
                        final String Arg1 = args[1].trim();
                        if (!DragonCommands.isInteger(Arg1)) {
                            console.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final int value = Integer.parseInt(Arg1);
                        this.setConfigVar(console, args[0].toLowerCase().trim(), value, ThisWorld);
                    } else if (Arrays.asList(boolVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (args.length == 2 && this.isWorld(args[1].trim())) {
                            final String ThisWorld = args[1].trim().toLowerCase();
                            this.getConfigBoolean(console, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        if (args.length != 3) {
                            console.sendMessage(ChatColor.RED + "Wrong syntax.");
                            return false;
                        }
                        if (!this.isWorld(args[2].trim())) {
                            console.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            return false;
                        }
                        final String ThisWorld = args[2].trim().toLowerCase();
                        final String Arg1 = args[1].trim().toLowerCase();
                        if (!Arg1.equals("true") && !Arg1.equals("false")) {
                            console.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final boolean value2 = Boolean.parseBoolean(Arg1);
                        this.setConfigBoolean(console, args[0].toLowerCase().trim(), value2, ThisWorld);
                    } else if (args[0].equalsIgnoreCase("forceallrespawn") || args[0].equalsIgnoreCase("forceall")) {
                        this.plugin.SpawnForceAllDragons();
                        console.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                    } else {
                        this.dragonSpawnHelp(console);
                    }
                }
            }
        }
        return false;
    }

    private void getDragonSpawn(final CommandSender sender, final String worldName) {
        final World world = this.plugin.getDragonWorldFromString(worldName);
        if (world.getEnvironment() != World.Environment.THE_END) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " is not an End-World !");
            return;
        }
        if (!this.plugin.checkWorld(worldName)) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " is not used by DragonTimer !");
            return;
        }
        final double x = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".x");
        final double y = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".y");
        final double z = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".z");
        sender.sendMessage(ChatColor.GREEN + "Dragon spawn is set at: " + (int) x + " " + (int) y + " " + (int) z + " in " + worldName);
    }

    private void setDragonSpawn(final CommandSender sender, final double x, final double y, final double z, final String worldName) {
        final World world = this.plugin.getDragonWorldFromString(worldName);
        if (world.getEnvironment() != World.Environment.THE_END) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " is not an End-World !");
            return;
        }
        this.plugin.getConfig().set("spawnpoint." + worldName + ".x", x);
        this.plugin.getConfig().set("spawnpoint." + worldName + ".y", y);
        this.plugin.getConfig().set("spawnpoint." + worldName + ".z", z);
        this.plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Dragon spawn set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + worldName);
    }

    private void remDragonSpawn(final CommandSender sender, final String worldName) {
        this.plugin.getConfig().set("spawnpoint." + worldName, null);
        this.plugin.getConfig().set("dragon." + worldName, null);
        this.plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Dragon spawn removed from world: " + worldName);
    }

    private void addSpawnTime(final CommandSender sender, final String time, final String worldName, final Boolean add) {
        if (this.plugin.checkWorld(worldName)) {
            if (!time.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")) {
                sender.sendMessage(ChatColor.RED + "Incorrect time format. It is HH:MM in 24h time.");
                return;
            }
            this.plugin.getSpawnTimerList(worldName);
            final String spawnTimes = this.plugin.getConfig().getString("dragon." + worldName + ".spawntimes");
            boolean found = false;
            if (spawnTimes.contains(time)) {
                found = true;
            }
            if (add) {
                if (!found) {
                    if (spawnTimes.isEmpty()) {
                        this.plugin.getConfig().set("dragon." + worldName + ".spawntimes", time);
                    } else {
                        this.plugin.getConfig().set("dragon." + worldName + ".spawntimes", String.valueOf(spawnTimes) + "," + time);
                    }
                    this.plugin.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Time set for that world.");
                    this.plugin.SpawnTimerMap.remove(worldName);
                    return;
                }
                sender.sendMessage(ChatColor.RED + "This time is already set for that world.");
            } else {
                if (found) {
                    final String NewTimeString = spawnTimes.replace("," + time, "").replace(String.valueOf(time) + ",", "").replace(time,
                            "");
                    this.plugin.getConfig().set("dragon." + worldName + ".spawntimes", NewTimeString);
                    this.plugin.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Time removed from that world.");
                    this.plugin.SpawnTimerMap.remove(worldName);
                    return;
                }
                sender.sendMessage(ChatColor.RED + "This time was not found for that world.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getSpawnTime(final CommandSender sender, final String worldName) {
        if (!this.plugin.checkWorld(worldName)) {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
            return;
        }
        final String spawnTimes = this.plugin.getConfig().getString("dragon." + worldName + ".spawntimes");
        if (!spawnTimes.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Spawntimes for world " + worldName + ": " + spawnTimes);
            return;
        }
        sender.sendMessage(ChatColor.RED + "There are no times set for that world.");
    }

    private void killDragons(final CommandSender sender, final String worldName) {
        if (this.plugin.removeAllDragons(worldName)) {
            final String Message = this.plugin.replaceValues(this.plugin.getConfig().getString("messages.timelimit"), worldName);
            if (Message != null && !Message.isEmpty()) {
                this.plugin.getServer().broadcastMessage(Message);
            }
        }
    }

    private void setConfigVar(final CommandSender sender, final String key, final int value, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            this.plugin.getConfig().set("dragon." + worldName + "." + key, value);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + key + " set to " + value);
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getConfigVar(final CommandSender sender, final String key, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            final int value = this.plugin.getConfigInt(worldName, key);
            sender.sendMessage(ChatColor.GREEN + key + " is " + value + " !");
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void setConfigBoolean(final CommandSender sender, final String key, final boolean value, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            this.plugin.getConfig().set("dragon." + worldName + "." + key, value);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + key + " set to " + value);
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getConfigBoolean(final CommandSender sender, final String key, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            final boolean value = this.plugin.getConfigBoolean(worldName, key);
            sender.sendMessage(ChatColor.GREEN + key + " is " + value + " !");
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void dragonSpawnHelp(final CommandSender sender) {
        if (sender.hasPermission("dragontimer.info")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer getspawn [world]");
        }
        if (sender.hasPermission("dragontimer.admin")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer setspawn [x y z [world]]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer remspawn [world]");
        }
        if (sender.hasPermission("dragontimer.info")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer gettime [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer getnexttime [world]");
        }
        if (sender.hasPermission("dragontimer.admin")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer addtime hh:mm [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer remtime hh:mm [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer minplayers [value] [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer maxdragons [value] [world]");
            if (!this.plugin.getDSL()) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer health [value] [world]");
            }
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer removedragons true/false [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer noguarding true/false [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer timerdisplay true/false [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer onebyone true/false [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer removedelay [value] [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer timelimit [value] [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer killdragons [world]");
        }
        if (sender.hasPermission("dragontimer.admin")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer reload");
        }
        if (sender.hasPermission("dragontimer.forcerespawn")) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragontimer forceallrespawn");
        }
    }

    private boolean isWorld(final String worldName) { return Bukkit.getWorld(worldName) != null; }

    public static boolean isDouble(final String s) {
        try {
            Double.parseDouble(s);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
