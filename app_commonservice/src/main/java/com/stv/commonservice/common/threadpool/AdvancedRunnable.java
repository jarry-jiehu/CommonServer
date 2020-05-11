
package com.stv.commonservice.common.threadpool;

/**
 * 任务父类
 */

public abstract class AdvancedRunnable implements Runnable {

    protected abstract void onExecute();

    protected void afterExecute() {
    };

    @Override
    public void run() {
        onExecute();
        afterExecute();
    }

    public void execute() {
        AdvancedExecutor.getInstance().execute(this);
    }

}
