package me.tingri.j8.streaming;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by sandeep on 4/23/16.
 */
class TimeIt {

    static <T> T time(Callable<T> task, List<Double> timings) {
        T call = null;
        try {
            long startTime = System.currentTimeMillis();
            call = task.call();
            timings.add((System.currentTimeMillis() - startTime) / 1000d);
        } catch (Exception e) {
            //...
        }
        return call;
    }
}
