package com.adamgaltrey.islesarena.game;

import com.adamgaltrey.islesarena.IslesArena;
import com.adamgaltrey.islesarena.configuration.IslesConfig;
import com.adamgaltrey.islesarena.queues.ChallengeQueue;
import com.adamgaltrey.islesarena.utilities.IslesMessenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

/**
 * Created by Adam on 26/04/2017.
 */
public class DuelInstance implements Listener {

    private final Player p1, p2;
    private final Location oldLoc1, oldLoc2;

    private boolean begun = false, ended = false;
    private BukkitTask task;

    public DuelInstance(final Player p1, final Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.oldLoc1 = p1.getLocation();
        this.oldLoc2 = p2.getLocation();

        Bukkit.getPluginManager().registerEvents(this, IslesArena.getPlugin());

        p1.teleport(IslesConfig.getSpawn1().getLocation());
        p2.teleport(IslesConfig.getSpawn2().getLocation());

        p1.setHealth(20);
        p2.setHealth(20);

        task = Bukkit.getScheduler().runTaskTimer(IslesArena.getPlugin(), new Runnable() {

            int clock = 5;

            public void run() {

                if (ended) {
                    return;
                }

                if (clock <= 0) {
                    begun = true;
                    task.cancel();
                    String msg =  "&aThe duel has begun!";
                    IslesMessenger.message(p1, msg);
                    IslesMessenger.message(p2, msg);
                    return;
                }

                String msg =  "&aThe duel will begin in " + clock + (clock > 1 ? " seconds." : " second.");
                IslesMessenger.message(p1, msg);
                IslesMessenger.message(p2, msg);

                clock--;

            }

        }, 0, 20L);
    }

    //Is this player relevant to our duel?
    private boolean isMember(Player p) {
        return p.equals(p1) || p.equals(p2);
    }

    @EventHandler
    private void move(PlayerMoveEvent evt) {
        //Boolean check first as this is fastest
        if (!begun && isMember(evt.getPlayer())) {
            evt.setTo(evt.getFrom());
        }
    }

    @EventHandler
    private void drop(PlayerDropItemEvent evt){
        if (isMember(evt.getPlayer())) {
            //No item dropping while in a duel.
            evt.setCancelled(true);
        }
    }

    /*
        I know there's an event or method to let the player die and then respawn them so they dont see the death screen.
        That way damage calculations after armour are applied which doesn't happen if we use this event.

        But I cant remember it or it's in some old code so it's not overly important now.
     */
    @EventHandler
    private void dmg(EntityDamageEvent evt) {
        if (evt.getEntity() instanceof Player) {
            Player p = (Player) evt.getEntity();

            if (isMember(p)) {
                if (p.getHealth() - evt.getDamage() <= 0) {
                    endDuel(p.equals(p1) ? p2 : p1);
                    evt.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void quit(PlayerQuitEvent evt) {
        if (isMember(evt.getPlayer())) {
            endDuel(evt.getPlayer().equals(p1) ? p2 : p1);
        }
    }

    private void endDuel(Player winner){
        ended = true;

        p1.teleport(oldLoc1);
        p2.teleport(oldLoc2);

        p1.setHealth(20);
        p2.setHealth(20);

        IslesMessenger.message(winner, "&aYou won the duel!");
        IslesMessenger.message(winner.equals(p1) ? p2 : p1, "&cYou lost the duel!");

        //Deregister this player from the duelling list.
        ChallengeQueue.duelEnded(p1.getName().toLowerCase(), p2.getName().toLowerCase());

        //Deregister events, garbage collector will eat this class up soon.
        HandlerList.unregisterAll(this);
    }


}
