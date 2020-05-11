
package com.stv.commonservice.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stv.commonservice.activation.ActivationTask;
import com.stv.commonservice.appupdate.manager.UpdateManager;
import com.stv.commonservice.attestation.net.AttestTask;
import com.stv.commonservice.domain.DomainRequestTask;
import com.stv.commonservice.util.LogUtils;

/**
 * for test ,Not open to the outside world
 */
public class TestReceiver extends BroadcastReceiver {
    private final String TAG = TestReceiver.class.getSimpleName();
    private final String TEST_ACTION = "com.stv.activation.action.test";
    private final String FLG = "flg";
    private final String FlG_FOR_DOMAIN = "FlG_FOR_DOMAIN";
    private final String FlG_FOR_ATTEST = "FlG_FOR_ATTEST";
    private final String FlG_FOR_ACTIVATION = "FlG_FOR_ACTIVATION";
    private final String FlG_FOR_APPUPDATE = "FLG_FOR_APPUPDATE";
    private LogUtils mLog = LogUtils.getInstance("Domain receiver", TAG);

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action) {
            return;
        }
        mLog.i("TestReceiver action: " + action);
        if (TEST_ACTION.equals(action)) {
            String flg = intent.getStringExtra(FLG);
            mLog.i("TestReceiver flg " + flg);
            if (null != flg && FlG_FOR_DOMAIN.equals(flg)) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        new DomainRequestTask(context).execute();
                    }
                }).start();

            } else if (null != flg && FlG_FOR_ATTEST.equals(flg)) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        new AttestTask(context).execute();
                    }
                }).start();

            } else if (null != flg && FlG_FOR_ACTIVATION.equals(flg)) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        new ActivationTask(context, true).execute();
                    }
                }).start();
            } else if (null != flg && FlG_FOR_APPUPDATE.equals(flg)) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        UpdateManager.getInstance(context).checkUpdate();
                    }
                }).start();
            }
        }
    }
}
