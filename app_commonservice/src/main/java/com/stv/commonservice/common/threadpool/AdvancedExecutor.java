
package com.stv.commonservice.common.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池
 */

public class AdvancedExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_SIZE = CPU_COUNT * 2 + 1;
    private static final int MAX_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_TIME = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ExecutorManager #" + mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    private static AdvancedExecutor sInstance;
    private ThreadPoolExecutor pool;

    private AdvancedExecutor() {
        pool = new ThreadPoolExecutor(CORE_SIZE, MAX_SIZE, KEEP_TIME, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    public static AdvancedExecutor getInstance() {
        if (null == sInstance) {
            sInstance = new AdvancedExecutor();
        }
        return sInstance;
    }

    public void execute(Runnable runnable) {
        pool.execute(runnable);
    }
}
