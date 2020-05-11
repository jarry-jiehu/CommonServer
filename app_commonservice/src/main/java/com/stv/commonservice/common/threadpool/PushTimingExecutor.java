
package com.stv.commonservice.common.threadpool;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PushTimingExecutor {
    private static final int CORE_SIZE = 1;

    private static PushTimingExecutor sInstance;
    private ScheduledThreadPoolExecutor mPoolExecutor;

    private PushTimingExecutor() {
        mPoolExecutor = new ScheduledThreadPoolExecutor(CORE_SIZE);
    }

    public static PushTimingExecutor getInstance() {
        if (sInstance == null) {
            sInstance = new PushTimingExecutor();
        }
        return sInstance;
    }

    public void execute(Runnable runnable, long delay) {
        mPoolExecutor.schedule(runnable, delay, TimeUnit.MINUTES);
    }
}
