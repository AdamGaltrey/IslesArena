package com.adamgaltrey.islesarena.configuration;

import com.adamgaltrey.islesarena.data.IslesLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by Adam on 26/04/2017.
 */
public class IslesConfig {

    private static File cfg;

    //Config variables.
    private static String prefix;
    private static IslesLocation spawn1, spawn2;
    private static int requestTimeout;

    /**
     * Initialse the configuration, and create it if needed.
     *
     * @param root The root working directory, should be "/plugins/IslesArena".
     */
    public static boolean init(File root) {
        root.mkdir();

        cfg = new File(root + File.separator + "config.yml");

        FileConfiguration io = YamlConfiguration.loadConfiguration(cfg);

        //Create config if it does not exist.
        if (!cfg.exists()) {
            try {
                cfg.createNewFile();

                io.set("messages.prefix", "&7[&aIslesArena&7]&r ");
                io.set("arena.spawn1", "");
                io.set("arena.spawn2", "");
                // Duel invitations expire 120 seconds after being sent.
                io.set("requests.timeout.seconds", 120);
                io.save(cfg);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        //Load config.
        prefix = ChatColor.translateAlternateColorCodes('&', io.getString("messages.prefix", "&7[&aIslesArena&7]&r "));
        spawn1 = new IslesLocation(io.getString("arena.spawn1"));
        spawn2 = new IslesLocation(io.getString("arena.spawn2"));
        requestTimeout = io.getInt("requests.timeout.seconds", 120);

        return true;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static IslesLocation getSpawn1() {
        return spawn1;
    }

    public static IslesLocation getSpawn2() {
        return spawn2;
    }

    public static int getRequestTimeout() {
        return requestTimeout;
    }

    public static boolean setSpawn(boolean s1, Location l) {
        IslesLocation il = new IslesLocation(l);

        FileConfiguration io = YamlConfiguration.loadConfiguration(cfg);
        io.set(s1 ? "arena.spawn1" : "arena.spawn2", il.toString());
        try {
            io.save(cfg);
            if (s1) {
                spawn1 = il;
            } else {
                spawn2 = il;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
