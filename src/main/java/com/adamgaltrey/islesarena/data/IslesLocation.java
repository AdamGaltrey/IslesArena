package com.adamgaltrey.islesarena.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.ref.SoftReference;

/**
 * Created by Adam on 26/04/2017.
 */
public class IslesLocation {

    //A class to handle saving/loading Bukkit location classes.
    private String world = null;
    private double x, y, z;
    float yaw, pitch;

    private SoftReference<Location> location = null;

    //This will be called if loading from config.
    public IslesLocation(String in) {
        String[] s = in.split(",");

        if (s.length != 6) {
            return;
        }

        world = s[0];
        x = toDouble(s[1]);
        y = toDouble(s[2]);
        z = toDouble(s[3]);
        yaw = toFloat(s[4]);
        pitch = toFloat(s[5]);
    }

    //This will be called when the spawn is set or changed in game.
    public IslesLocation(Location l) {
        world = l.getWorld().getName();
        x = l.getX();
        y = l.getY();
        z = l.getZ();
        yaw = l.getYaw();
        pitch = l.getPitch();

        location = new SoftReference<Location>(l);
    }

    private static float toFloat(String s) {
        return Float.parseFloat(s);
    }

    private static double toDouble(String s) {
        return Double.parseDouble(s);
    }

    /**
     * Has the class been constructed properly or is the data not set?
     * @return True if the location is useable, otherwise false.
     */
    public boolean isSet(){
        return world != null;
    }

    public Location getLocation(){
        if (location == null || location.get() == null) {
            //create again
            Location l = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            location = new SoftReference<Location>(l);
            return l;
        } else {
            //we still have the reference
            return location.get();
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(world).append(",").append(x).append(",").append(y).append(",").append(z).append(",").append(yaw).append(",").append(pitch);
        return b.toString();
    }
}
