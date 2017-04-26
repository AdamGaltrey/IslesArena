package com.adamgaltrey.islesarena.threading;

import com.adamgaltrey.islesarena.data.DuelRequest;

import java.util.Random;

/**
 * Created by Adam on 26/04/2017.
 */
public class LatencySimulator {

    private static Random r = new Random(System.currentTimeMillis());
    //Mean of 1 second
    private static final double mean = 1000;

    /**
     * Simulate a latency delay in milliseconds.
     * @return
     */
    public static int simulateDelay(){
        //Set the upper limit
        double L = Math.exp(-mean);
        double p = r.nextDouble();
        int k = 0;

        do {
            k++;
            p *= r.nextDouble();
        } while (p > L);

        return k - 1;
    }
}
