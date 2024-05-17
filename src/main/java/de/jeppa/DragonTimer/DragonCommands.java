// Class Version: 8
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
        final String BoolVarList = "removedragons,onebyone,timerdisplay,noguarding";
        final String IntVarList = "minplayers,maxdragons,health,removedelay,timelimit,dsl_refreshdelay";
        if (sender instanceof final Player p) {
            if (cmd.getName().equalsIgnoreCase("dragontimer")) {
                if (args.length == 0) {
                    if (!p.hasPermission("dragontimer")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission");
                        return false;
                    }
                    this.dragonSpawnHelp(p);
                } else if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("set")) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final Location loc = p.getLocation();
                            final double x = loc.getX();
                            final double y = loc.getY();
                            final double z = loc.getZ();
                            final String w = p.getWorld().getName().toLowerCase();
                            this.setDragonSpawn(p, x, y, z, w);
                            this.plugin.setDragonDefaults();
                        } else if (args.length >= 4 && args.length <= 5) {
                            if (DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                String w2 = null;
                                if (args.length == 5) {
                                    if (this.isWorld(args[4])) {
                                        w2 = args[4].trim().toLowerCase();
                                    } else {
                                        p.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                    }
                                } else {
                                    w2 = p.getWorld().getName().toLowerCase();
                                }
                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (w2 != null) {
                                    this.setDragonSpawn(p, x, y, z, w2);
                                    this.plugin.setDragonDefaults();
                                }
                            } else {
                                this.dragonSpawnHelp(p);
                            }
                        } else {
                            this.dragonSpawnHelp(p);
                        }
                    } else if (args[0].equalsIgnoreCase("remspawn") || args[0].equalsIgnoreCase("rem")) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String w2 = p.getWorld().getName().toLowerCase();
                            this.remDragonSpawn(p, w2);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String w2 = args[1].trim().toLowerCase();
                                this.remDragonSpawn(p, w2);
                            } else {
                                p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn") || args[0].equalsIgnoreCase("get")) {
                        if (!p.hasPermission("dragontimer.info")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = p.getWorld().getName().toLowerCase();
                            this.getDragonSpawn(p, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getDragonSpawn(p, ThisWorld);
                            } else {
                                p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("killdragons") || args[0].equalsIgnoreCase("kill")) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = p.getWorld().getName().toLowerCase();
                            this.killDragons(p, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.killDragons(p, ThisWorld);
                            } else {
                                p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rel")) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        this.plugin.reloadConfig();
                        this.plugin.loadConfiguration();
                        this.plugin.SpawnTimerMap.clear();
                        for (final Scoreboard timerDisplay : this.plugin.timerDisplays.values()) {
                            timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                        }
                        this.plugin.timerDisplays.clear();
                        p.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at") || args[0].equalsIgnoreCase("remtime")
                            || args[0].equalsIgnoreCase("rt")) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        boolean add = false;
                        if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at")) {
                            add = true;
                        }
                        if (args.length == 2) {
                            final String ThisWorld2 = p.getWorld().getName().toLowerCase();
                            this.addSpawnTime(p, args[1].trim(), ThisWorld2, add);
                        } else if (args.length == 3) {
                            if (this.isWorld(args[2])) {
                                final String ThisWorld2 = args[2].trim().toLowerCase();
                                this.addSpawnTime(p, args[1].trim(), ThisWorld2, add);
                            } else {
                                p.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("gettime") || args[0].equalsIgnoreCase("gt")) {
                        if (!p.hasPermission("dragontimer.info")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1) {
                            final String ThisWorld = p.getWorld().getName().toLowerCase();
                            this.getSpawnTime(p, ThisWorld);
                        } else if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getSpawnTime(p, ThisWorld);
                            } else {
                                p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("getnexttime") || args[0].equalsIgnoreCase("gn")) {
                        if (!p.hasPermission("dragontimer.info")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        String ThisWorld = "";
                        if (args.length == 1) {
                            ThisWorld = p.getWorld().getName().toLowerCase();
                        } else if (args.length >= 2) {
                            if (!this.isWorld(args[1])) {
                                p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[1].trim().toLowerCase();
                        }
                        final String NextTime = this.plugin.getNextSpawnTime(ThisWorld);
                        if (!NextTime.isEmpty()) {
                            p.sendMessage(ChatColor.YELLOW + "Next spawn in world " + ThisWorld + " is at " + NextTime + " !");
                        } else {
                            p.sendMessage(ChatColor.RED + "No spawntime or wrong world :" + ThisWorld);
                        }
                    } else if (Arrays.asList(IntVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1 || (args.length == 2 && this.isWorld(args[1].trim()))) {
                            String ThisWorld = p.getWorld().getName().toLowerCase();
                            if (args.length == 2) {
                                ThisWorld = args[1].trim().toLowerCase();
                            }
                            this.getConfigVar(p, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        String ThisWorld;
                        if (args.length == 2) {
                            ThisWorld = p.getWorld().getName().toLowerCase();
                        } else {
                            if (args.length != 3) {
                                p.sendMessage(ChatColor.RED + "Wrong syntax.");
                                return false;
                            }
                            if (!this.isWorld(args[2].trim())) {
                                p.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[2].trim().toLowerCase();
                        }
                        final String Arg1 = args[1].trim();
                        if (!DragonCommands.isInteger(Arg1)) {
                            p.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final int value = Integer.parseInt(Arg1);
                        this.setConfigVar(p, args[0].toLowerCase().trim(), value, ThisWorld);
                    } else if (Arrays.asList(BoolVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (!p.hasPermission("dragontimer.admin")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        if (args.length == 1 || (args.length == 2 && this.isWorld(args[1].trim()))) {
                            String ThisWorld = p.getWorld().getName().toLowerCase();
                            if (args.length == 2) {
                                ThisWorld = args[1].trim().toLowerCase();
                            }
                            this.getConfigBoolean(p, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        String ThisWorld;
                        if (args.length == 2) {
                            ThisWorld = p.getWorld().getName().toLowerCase();
                        } else {
                            if (args.length != 3) {
                                p.sendMessage(ChatColor.RED + "Wrong syntax.");
                                return false;
                            }
                            if (!this.isWorld(args[2].trim())) {
                                p.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[2].trim().toLowerCase();
                        }
                        final String Arg1 = args[1].trim().toLowerCase();
                        if (!Arg1.equals("true") && !Arg1.equals("false")) {
                            p.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final boolean value2 = Boolean.parseBoolean(Arg1);
                        this.setConfigBoolean(p, args[0].toLowerCase().trim(), value2, ThisWorld);
                    } else if (args[0].equalsIgnoreCase("forceallrespawn") || args[0].equalsIgnoreCase("forceall")) {
                        if (!p.hasPermission("dragontimer.forcerespawn")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission");
                            return false;
                        }
                        this.plugin.SpawnForceAllDragons();
                        p.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                    } else {
                        this.dragonSpawnHelp(p);
                    }
                }
            }
        } else {
            final ConsoleCommandSender p2 = (ConsoleCommandSender) sender;
            if (cmd.getName().equalsIgnoreCase("dragontimer")) {
                if (args.length == 0) {
                    this.dragonSpawnHelp(p2);
                } else if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("set")) {
                        if (args.length == 5) {
                            if (DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                String w2 = null;
                                if (this.isWorld(args[4])) {
                                    w2 = args[4].trim().toLowerCase();
                                } else {
                                    p2.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                }
                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (w2 != null) {
                                    this.setDragonSpawn(p2, x, y, z, w2);
                                    this.plugin.setDragonDefaults();
                                }
                            } else {
                                this.dragonSpawnHelp(p2);
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("remspawn") || args[0].equalsIgnoreCase("rem")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String w2 = args[1].trim().toLowerCase();
                                this.remDragonSpawn(p2, w2);
                            } else {
                                p2.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn") || args[0].equalsIgnoreCase("get")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getDragonSpawn(p2, ThisWorld);
                            } else {
                                p2.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("killdragons") || args[0].equalsIgnoreCase("kill")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.killDragons(p2, ThisWorld);
                            } else {
                                p2.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rel")) {
                        this.plugin.reloadConfig();
                        this.plugin.loadConfiguration();
                        this.plugin.SpawnTimerMap.clear();
                        for (final Scoreboard timerDisplay : this.plugin.timerDisplays.values()) {
                            timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                        }
                        this.plugin.timerDisplays.clear();
                        p2.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at") || args[0].equalsIgnoreCase("remtime")
                            || args[0].equalsIgnoreCase("rt")) {
                        boolean add = false;
                        if (args[0].equalsIgnoreCase("addtime") || args[0].equalsIgnoreCase("at")) {
                            add = true;
                        }
                        if (args.length == 3) {
                            if (this.isWorld(args[2])) {
                                final String ThisWorld2 = args[2].trim().toLowerCase();
                                this.addSpawnTime(p2, args[1].trim(), ThisWorld2, add);
                            } else {
                                p2.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("gettime") || args[0].equalsIgnoreCase("gt")) {
                        if (args.length == 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                this.getSpawnTime(p2, ThisWorld);
                            } else {
                                p2.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            }
                        } else {
                            this.dragonSpawnHelp(p2);
                        }
                    } else if (args[0].equalsIgnoreCase("getnexttime") || args[0].equalsIgnoreCase("gn")) {
                        String ThisWorld = "";
                        if (args.length >= 2) {
                            if (!this.isWorld(args[1])) {
                                p2.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                return false;
                            }
                            ThisWorld = args[1].trim().toLowerCase();
                        }
                        final String NextTime = this.plugin.getNextSpawnTime(ThisWorld);
                        if (!NextTime.isEmpty()) {
                            p2.sendMessage(ChatColor.YELLOW + "Next spawn in world " + ThisWorld + " is at " + NextTime + " !");
                        } else {
                            p2.sendMessage(ChatColor.RED + "No spawntime or wrong world :" + ThisWorld);
                        }
                    } else if (Arrays.asList(IntVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (args.length == 2 && this.isWorld(args[1].trim())) {
                            final String ThisWorld = args[1].trim().toLowerCase();
                            this.getConfigVar(p2, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        if (args.length != 3) {
                            p2.sendMessage(ChatColor.RED + "Wrong syntax.");
                            return false;
                        }
                        if (!this.isWorld(args[2].trim())) {
                            p2.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            return false;
                        }
                        final String ThisWorld = args[2].trim().toLowerCase();
                        final String Arg1 = args[1].trim();
                        if (!DragonCommands.isInteger(Arg1)) {
                            p2.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final int value = Integer.parseInt(Arg1);
                        this.setConfigVar(p2, args[0].toLowerCase().trim(), value, ThisWorld);
                    } else if (Arrays.asList(BoolVarList.split(",")).contains(args[0].toLowerCase().trim())) {
                        if (args.length == 2 && this.isWorld(args[1].trim())) {
                            final String ThisWorld = args[1].trim().toLowerCase();
                            this.getConfigBoolean(p2, args[0].toLowerCase().trim(), ThisWorld);
                            return true;
                        }
                        if (args.length != 3) {
                            p2.sendMessage(ChatColor.RED + "Wrong syntax.");
                            return false;
                        }
                        if (!this.isWorld(args[2].trim())) {
                            p2.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                            return false;
                        }
                        final String ThisWorld = args[2].trim().toLowerCase();
                        final String Arg1 = args[1].trim().toLowerCase();
                        if (!Arg1.equals("true") && !Arg1.equals("false")) {
                            p2.sendMessage(ChatColor.RED + "Wrong format!");
                            return false;
                        }
                        final boolean value2 = Boolean.parseBoolean(Arg1);
                        this.setConfigBoolean(p2, args[0].toLowerCase().trim(), value2, ThisWorld);
                    } else if (args[0].equalsIgnoreCase("forceallrespawn") || args[0].equalsIgnoreCase("forceall")) {
                        this.plugin.SpawnForceAllDragons();
                        p2.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                    } else {
                        this.dragonSpawnHelp(p2);
                    }
                }
            }
        }
        return false;
    }

    private void getDragonSpawn(final CommandSender p, final String Worldname) {
        final World W = this.plugin.getDragonWorldFromString(Worldname);
        if (W.getEnvironment() != World.Environment.THE_END) {
            p.sendMessage(ChatColor.RED + "World " + Worldname + " is not an End-World !");
            return;
        }
        if (!this.plugin.checkWorld(Worldname)) {
            p.sendMessage(ChatColor.RED + "World " + Worldname + " is not used by DragonTimer !");
            return;
        }
        final double x = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".x");
        final double y = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".y");
        final double z = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".z");
        p.sendMessage(ChatColor.GREEN + "Dragon spawn is set at: " + (int) x + " " + (int) y + " " + (int) z + " in " + Worldname);
    }

    private void setDragonSpawn(final CommandSender p, final double x, final double y, final double z, final String w) {
        final World W = this.plugin.getDragonWorldFromString(w);
        if (W.getEnvironment() != World.Environment.THE_END) {
            p.sendMessage(ChatColor.RED + "World " + w + " is not an End-World !");
            return;
        }
        this.plugin.getConfig().set("spawnpoint." + w + ".x", x);
        this.plugin.getConfig().set("spawnpoint." + w + ".y", y);
        this.plugin.getConfig().set("spawnpoint." + w + ".z", z);
        this.plugin.saveConfig();
        p.sendMessage(ChatColor.GREEN + "Dragon spawn set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + w);
    }

    private void remDragonSpawn(final CommandSender p, final String w) {
        this.plugin.getConfig().set("spawnpoint." + w, null);
        this.plugin.getConfig().set("dragon." + w, null);
        this.plugin.saveConfig();
        p.sendMessage(ChatColor.GREEN + "Dragon spawn removed from world: " + w);
    }

    private void addSpawnTime(final CommandSender p, final String Time, final String ThisWorld, final Boolean add) {
        if (this.plugin.checkWorld(ThisWorld)) {
            if (!Time.matches("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")) {
                p.sendMessage(ChatColor.RED + "Incorrect time format. It is HH:MM in 24h time.");
                return;
            }
            this.plugin.getSpawnTimerList(ThisWorld);
            final String spawnTimes = this.plugin.getConfig().getString("dragon." + ThisWorld + ".spawntimes");
            boolean found = false;
            if (spawnTimes.contains(Time)) {
                found = true;
            }
            if (add) {
                if (!found) {
                    if (spawnTimes.isEmpty()) {
                        this.plugin.getConfig().set("dragon." + ThisWorld + ".spawntimes", Time);
                    } else {
                        this.plugin.getConfig().set("dragon." + ThisWorld + ".spawntimes", String.valueOf(spawnTimes) + "," + Time);
                    }
                    this.plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Time set for that world.");
                    this.plugin.SpawnTimerMap.remove(ThisWorld);
                    return;
                }
                p.sendMessage(ChatColor.RED + "This time is already set for that world.");
            } else {
                if (found) {
                    final String NewTimeString = spawnTimes.replace("," + Time, "").replace(String.valueOf(Time) + ",", "").replace(Time,
                            "");
                    this.plugin.getConfig().set("dragon." + ThisWorld + ".spawntimes", NewTimeString);
                    this.plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Time removed from that world.");
                    this.plugin.SpawnTimerMap.remove(ThisWorld);
                    return;
                }
                p.sendMessage(ChatColor.RED + "This time was not found for that world.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getSpawnTime(final CommandSender p, final String ThisWorld) {
        if (!this.plugin.checkWorld(ThisWorld)) {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
            return;
        }
        final String spawnTimes = this.plugin.getConfig().getString("dragon." + ThisWorld + ".spawntimes");
        if (!spawnTimes.isEmpty()) {
            p.sendMessage(ChatColor.YELLOW + "Spawntimes for world " + ThisWorld + ": " + spawnTimes);
            return;
        }
        p.sendMessage(ChatColor.RED + "There are no times set for that world.");
    }

    private void killDragons(final CommandSender p, final String ThisWorld) {
        if (this.plugin.removeAllDragons(ThisWorld)) {
            final String Message = this.plugin.replaceValues(this.plugin.getConfig().getString("messages.timelimit"), ThisWorld);
            if (Message != null && !Message.isEmpty()) {
                this.plugin.getServer().broadcastMessage(Message);
            }
        }
    }

    private void setConfigVar(final CommandSender p, final String var, final int value, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            this.plugin.getConfig().set("dragon." + ThisWorld + "." + var, value);
            this.plugin.saveConfig();
            p.sendMessage(ChatColor.GREEN + var + " set to " + value);
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getConfigVar(final CommandSender p, final String var, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            final int value = this.plugin.getConfigInt(ThisWorld, var);
            p.sendMessage(ChatColor.GREEN + var + " is " + value + " !");
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void setConfigBoolean(final CommandSender p, final String var, final boolean value, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            this.plugin.getConfig().set("dragon." + ThisWorld + "." + var, value);
            this.plugin.saveConfig();
            p.sendMessage(ChatColor.GREEN + var + " set to " + value);
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void getConfigBoolean(final CommandSender p, final String var, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            final boolean value = this.plugin.getConfigBoolean(ThisWorld, var);
            p.sendMessage(ChatColor.GREEN + var + " is " + value + " !");
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }
    }

    private void dragonSpawnHelp(final CommandSender p) {
        if (p.hasPermission("dragontimer.info")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer getspawn [world]");
        }
        if (p.hasPermission("dragontimer.admin")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer setspawn [x y z [world]]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer remspawn [world]");
        }
        if (p.hasPermission("dragontimer.info")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer gettime [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer getnexttime [world]");
        }
        if (p.hasPermission("dragontimer.admin")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer addtime hh:mm [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer remtime hh:mm [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer minplayers [value] [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer maxdragons [value] [world]");
            if (!this.plugin.getDSL()) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer health [value] [world]");
            }
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer removedragons true/false [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer noguarding true/false [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer timerdisplay true/false [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer onebyone true/false [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer removedelay [value] [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer timelimit [value] [world]");
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer killdragons [world]");
        }
        if (p.hasPermission("dragontimer.admin")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer reload");
        }
        if (p.hasPermission("dragontimer.forcerespawn")) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragontimer forceallrespawn");
        }
    }

    private boolean isWorld(final String string) { return Bukkit.getWorld(string) != null; }

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
