package com.adamgaltrey.islesarena.data;

import com.adamgaltrey.islesarena.configuration.IslesConfig;

/**
 * Created by Adam on 26/04/2017.
 */
public class DuelRequest {

    //Name of the player who sent the request and their intended target
    private final String origin, target;

    //To keep track of how long this request has been 'alive'.
    private final long t = System.currentTimeMillis();

    public DuelRequest(String origin, String target) {
        this.origin = origin;
        this.target = target;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTarget() {
        return target;
    }

    /**
     * How long has this duel request been 'alive'.
     * @return Number of seconds since this request was created.
     */
    private int getLife(){
        double ms = (double) (System.currentTimeMillis() - t);
        return (int) Math.floor(ms / 1000D);
    }

    /**
     * Check whether or not this duel request should've expired.
     * @return True if the request has expired.
     */
    public boolean shouldExpire(){
        return getLife() >= IslesConfig.getRequestTimeout();
    }
}
