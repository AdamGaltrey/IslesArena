package com.adamgaltrey.islesarena.commands;

import com.adamgaltrey.islesarena.configuration.IslesConfig;
import com.adamgaltrey.islesarena.data.DuelRequest;
import com.adamgaltrey.islesarena.queues.ChallengeQueue;
import com.adamgaltrey.islesarena.utilities.IslesMessenger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Adam on 26/04/2017.
 */
public class CoreCommands implements CommandExecutor {

    private final String[] help = {
            ChatColor.translateAlternateColorCodes('&', "&a/ia ss1 &7to set spawn point 1 for the duel arena."),
            ChatColor.translateAlternateColorCodes('&', "&a/ia ss2 &7to set spawn point 2 for the duel arena."),
            ChatColor.translateAlternateColorCodes('&', "&a/ia challenge <player> &7to challenge named player to a duel."),
            ChatColor.translateAlternateColorCodes('&', "&a/ia duel &7to accept a duel request.")
    };

    private final ChallengeQueue queue = new ChallengeQueue();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("islesarena")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "IslesArena commands must be executed in game.");
                return true;
            }

            Player p = (Player) sender;

            boolean ss1 = false;
            if (args.length == 1 && ( (ss1 = args[0].equalsIgnoreCase("ss1")) || args[0].equalsIgnoreCase("ss2"))) {
                //only let operators set spawns, haven't bothered with permissions in case test server isnt running them.
                if (!p.isOp()) {
                    IslesMessenger.message(p, "&cOnly operators may set the duel arena spawn points.");
                    return true;
                } else {
                    if(IslesConfig.setSpawn(ss1, p.getLocation())){
                        IslesMessenger.message(p, "&aSpawn point " + (ss1 ? 1 : 2) + " set successfully.");
                    } else {
                        IslesMessenger.message(p, "&cAn unknown error occured while attempting to set a spawn point.");
                    }
                }

                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("duel")) {
                //Accept a duel
                queue.acceptDuel(p);

                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("challenge")) {
                String targetName = args[1];

                if (targetName.equalsIgnoreCase(p.getName())) {
                    IslesMessenger.message(p, "&cYou cannot challenge yourself to a duel.");
                    return true;
                }

                if (!IslesConfig.getSpawn1().isSet() || !IslesConfig.getSpawn2().isSet()) {
                    IslesMessenger.message(p, "&cThe duel arena spawn points have not been configured yet.");
                    return true;
                }
                //Check their queue and process
                queue.sendRequest(p, new DuelRequest(p.getName(), targetName));

                return true;
            }

            fallbackHelp(p);

            return true;
        }

        return false;
    }

    private void fallbackHelp(Player p) {
        //send help messages if they did a command wrong
        p.sendMessage(help);
    }
}
