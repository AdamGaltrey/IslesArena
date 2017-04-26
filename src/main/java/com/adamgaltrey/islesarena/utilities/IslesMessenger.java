package com.adamgaltrey.islesarena.utilities;

import com.adamgaltrey.islesarena.configuration.IslesConfig;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Adam on 26/04/2017.
 */
public class IslesMessenger {

    private static String prefix = null;

    /**
     * Send a message to a player.
     * @param p Player to receive the message.
     * @param msg The message to send.
     */
    public static void message(Player p, String msg) {
        if (prefix == null) {
            //Lazy initialise the prefix from the config.
            prefix = IslesConfig.getPrefix();
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }

}
