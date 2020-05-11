
package com.stv.commonservice.control.util;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseThreadPool {
    private static final String TAG = BaseThreadPool.class.getSimpleName();
    private ExecutorService mExecutorService;
    private static int DEFAULE_THREAD_NUMBER = 1;// 默认线程数

    public BaseThreadPool() {
        if (null == mExecutorService || mExecutorService.isShutdown()) {
            mExecutorService = Executors.newFixedThreadPool(DEFAULE_THREAD_NUMBER);
        }
    }

    public BaseThreadPool(int threadNum) {
        if (null == mExecutorService || mExecutorService.isShutdown()) {
            mExecutorService = Executors.newFixedThreadPool(threadNum);
        }
    }

    /**
     * 执行线程
     */
    public void execute(Runnable runnable) {
        Log.d(TAG,"execute service=" + mExecutorService + " runnable=" + runnable);
        if (null != mExecutorService && null != runnable) {
            mExecutorService.execute(runnable);
        }
    }

    /**
     * 释放线程池
     */
    public void shutDownExecutorService() {
        if (null != mExecutorService && !mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
        }
        mExecutorService = null;
    }
}
