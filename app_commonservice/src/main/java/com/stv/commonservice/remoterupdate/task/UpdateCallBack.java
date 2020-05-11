
package com.stv.commonservice.remoterupdate.task;

public interface UpdateCallBack {
    public void onUpdateStart(int progress);

    public void onUpdateProgressChanged(int progress);

    public void onUpdateFinished(boolean successed);

}
