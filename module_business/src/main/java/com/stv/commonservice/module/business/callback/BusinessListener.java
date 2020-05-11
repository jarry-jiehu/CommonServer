
package com.stv.commonservice.module.business.callback;

public interface BusinessListener {
    void onStartProcess();

    void onWriteProps(int total, int current, boolean result, String prop);

    void onCopyFiles(int total, int current, String msg);

    void onInstallApks(int total, int current, boolean result, String path);

    void onUnInstallApks(int total, int current, boolean result, String pkg);

    void onFinishProcess();
}
