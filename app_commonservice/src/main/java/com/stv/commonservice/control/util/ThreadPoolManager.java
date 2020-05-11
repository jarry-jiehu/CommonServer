
package com.stv.commonservice.control.util;

public class ThreadPoolManager {
    private static ThreadPoolManager instance;
    /**
     * 响应界面操作需要检查账户,手机号,开通状态等检查的响应,避免重复操作,单线程
     */
    private final BaseThreadPool mUIResponseThreadPool;
    /**
     * 负责云同步联系人等,不需要实时响应的刷新操作
     */
    private final BaseThreadPool mSyncDataThreadPool;

    /**
     * 远程控制线程,操作比较频繁
     */
    private final BaseThreadPool mRemoteControlThreadPool;

    private ThreadPoolManager() {
        mUIResponseThreadPool = new BaseThreadPool();
        mSyncDataThreadPool = new BaseThreadPool(3);
        mRemoteControlThreadPool = new BaseThreadPool(5);
    }

    public static ThreadPoolManager getInstance() {
        if (null == instance) {
            synchronized (ThreadPoolManager.class) {
                if (null == instance) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public BaseThreadPool getUIThreadPool() {
        return mUIResponseThreadPool;
    }

    public BaseThreadPool getSyncDataThreadPool() {
        return mSyncDataThreadPool;
    }

    public BaseThreadPool getRemoteControlThreadPool() {
        return mRemoteControlThreadPool;
    }
}
