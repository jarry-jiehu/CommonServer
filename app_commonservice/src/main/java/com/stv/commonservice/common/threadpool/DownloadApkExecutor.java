
package com.stv.commonservice.common.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadApkExecutor {
    private static final int CORE_SIZE = 1;
    private static final int MAX_SIZE = 1;
    private static final long KEEP_TIME = 3;

    private static DownloadApkExecutor sInstance;
    private ThreadPoolExecutor pool;

    private DownloadApkExecutor() {
        pool = new ThreadPoolExecutor(CORE_SIZE, MAX_SIZE, KEEP_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public static DownloadApkExecutor getInstance() {
        if (null == sInstance) {
            sInstance = new DownloadApkExecutor();
        }
        return sInstance;
    }

    public void execute(Runnable runnable) {
        pool.execute(runnable);
    }
}
