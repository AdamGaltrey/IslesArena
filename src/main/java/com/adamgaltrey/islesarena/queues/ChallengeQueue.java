package com.adamgaltrey.islesarena.queues;

import com.adamgaltrey.islesarena.IslesArena;
import com.adamgaltrey.islesarena.data.DuelRequest;
import com.adamgaltrey.islesarena.game.DuelInstance;
import com.adamgaltrey.islesarena.threading.LatencySimulator;
import com.adamgaltrey.islesarena.utilities.IslesMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Adam on 26/04/2017.
 */
public class ChallengeQueue {

    /*
        This Map links a <Receiving Player's Name> to a Queue of duel requests they have received.
     */
    private static final Map<String, LinkedList<DuelRequest>> map = new HashMap<String, LinkedList<DuelRequest>>();
    private static final Set<String> duelling = new HashSet<String>();

    /*


        If checking the map when accepting a duel, or adding a new duel request we need to synchronise on the map.
        For any further operations pertaining directly to the player we synchronise on their specific queue.
     */

    public static void duelEnded(String p1, String p2) {
        synchronized (duelling) {
            duelling.remove(p1.toLowerCase());
            duelling.remove(p2.toLowerCase());
        }
    }

    public void sendRequest(final Player sender, final DuelRequest req) {
        /*
            Steps:
            1) Check if the target player is online.
                   Typically lookup player name, if only locally. But if functioning cross server just use string as a
                   reference.
            2) If player is online send this request to them, on whichever server they reside.
            3) Store the request (synchronously) in the above mapped queue.
         */

        final String target = req.getTarget().toLowerCase();

        // For simplicity's sake I am assuming all players on the same server.
        // Otherwise things become much more complicated.
        final Player targP = Bukkit.getPlayer(req.getTarget());

        if (targP == null || !targP.isOnline()) {
            IslesMessenger.message(sender, "&cPlayer '" + req.getTarget() + "' is not online.");
        } else {
            //proceed

            synchronized (duelling) {
                if (duelling.contains(target)) {
                    //don't allow duel requests if they are in a duel
                    IslesMessenger.message(sender, "&cPlayer '" + req.getTarget() + "' is currently in a duel.");
                    return;
                }
            }

            //have i already sent this guy a req?

            /*
                We need this synchronized in the instance that a player receives 2 requests at the same time, while
                not having any already. Would result in two insertions to the map and the request sent last would
                overwrite the previous one if this method is called by 2 threads at the same time.
             */
            final int delayMS = LatencySimulator.simulateDelay();


            IslesMessenger.message(sender, "&aA duel request has been sent to '" + req.getTarget() + "'.");
            Bukkit.getScheduler().runTaskAsynchronously(IslesArena.getPlugin(), new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(delayMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {

                        synchronized (map) {
                            if (!map.containsKey(target)) {
                                //Add this request to the players queue if he doesn't have one already.
                                LinkedList<DuelRequest> q = new LinkedList<DuelRequest>();
                                q.add(req);
                                map.put(target, q);
                            } else {
                                LinkedList<DuelRequest> reqs = map.get(target);
                                //check they dont alrady have an existing request

                                Iterator<DuelRequest> it = reqs.iterator();
                                while (it.hasNext()) {
                                    DuelRequest r = it.next();
                                    if (r.getOrigin().equalsIgnoreCase(sender.getName())) {

                                        if (r.shouldExpire()) {
                                            //add a new one
                                            it.remove();
                                            break;
                                        } else {
                                            IslesMessenger.message(sender, "&cYou already have a pending invitation to this player.");
                                            return;
                                        }
                                    }
                                }

                                reqs.add(req);
                            }

                            IslesMessenger.message(targP, "&a'" + sender.getName() + "' has challenged you to a duel!");
                            IslesMessenger.message(targP, "&a/ia duel &7to accept.");
                        }
                    }
                }
            });

        }
    }

    /**
     * Attempt to accept any pending duels (synchronously).
     *
     * @param p The player who is attempting to accept a duel.
     */

    public void acceptDuel(Player p) {
        //Need to synchronise on acceptance and on receiving as a request may come in at the same time.
        final String target = p.getName().toLowerCase();

        p.sendMessage("working...");

        synchronized (duelling) {
            if (duelling.contains(target)) {
                IslesMessenger.message(p, "&cYou are already in a duel.");
                return;
            }
        }

        DuelRequest finalReq = null;
        Player targetPlayer = null;

        synchronized (map) {
            //check if the player has any pending invitations
            if (!map.containsKey(target)) {
                //They don't, so break out
                IslesMessenger.message(p, "&cNobody has challenged you to a duel.");
                return;
            } else {
                //accept the most recent invitation
                LinkedList<DuelRequest> reqs = map.get(target);
                DuelRequest req = null;

                //while there is stuff in the queue do work...
                while_loop:
                while ((req = reqs.peek()) != null) {
                    System.out.println("Process req from " + req.getOrigin());

                    //if the request has expired remove it from the queue, or if the player is offline
                    targetPlayer = Bukkit.getPlayer(req.getOrigin());
                    if (req.shouldExpire() || targetPlayer == null || !targetPlayer.isOnline()) {
                        req = null;
                        reqs.poll();
                        System.out.println("Removed expired req.");
                        continue while_loop;
                    }

                    //if the sender is in the queue, but in a duel invalidate their request
                    synchronized (duelling) {
                        if (duelling.contains(req.getOrigin().toLowerCase())) {
                            req = null;
                            reqs.poll();
                            System.out.println("Removed req or challenger is in a duel.");
                            continue while_loop;
                        }
                    }

                    break;
                }

                if (req != null) {
                    //we found a valid request
                    finalReq = reqs.poll(); //remove it
                    System.out.println("Valid duel found from " + finalReq.getOrigin());
                } else {
                    IslesMessenger.message(p, "&cYou do not have any valid pending duel requests.");
                    //remove them from this map
                    map.remove(target);
                    return;
                }

            }
        }

        //begin the duel
        synchronized (duelling) {
            duelling.add(target);
            duelling.add(finalReq.getOrigin().toLowerCase());
        }

        IslesMessenger.message(p, "&aYou have accepted '" + finalReq.getOrigin() + "'s' duel request!");
        IslesMessenger.message(targetPlayer, "&a'" + p.getName() + "' has accepted your duel request!");

        //begin the listener..., don't reference it anyway so it can be GC'ed after the duel
        new DuelInstance(p, targetPlayer);
    }


}
