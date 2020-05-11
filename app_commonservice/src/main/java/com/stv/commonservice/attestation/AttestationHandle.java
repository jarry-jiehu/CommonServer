
package com.stv.commonservice.attestation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.stv.commonservice.attestation.net.AttestTask;
import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

/**
 * CIBN 播控平台终端认证接口
 */
public class AttestationHandle extends EventDistribute {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_ATTEST, AttestationHandle.class.getSimpleName());
    private static AttestationHandle sInstance;
    private Context mContext;

    private AttestationHandle(Context context) {
        mContext = context;
    }

    public static AttestationHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new AttestationHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.v("******Attestation is start.*******" + "Thread id" + Thread.currentThread().getId());
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)
                || Constants.ALARM_BROADCAST_FOR_ATTESTATION.equals(action)) {
            initAttestation(mContext);// CIBN 播控平台终端认证接口
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Constants.TV_ACTION_ON.equals(action)) {
            // 开机后初始化标记状态，主要用于str模式，因str模式应用不会死，只处于休眠状态，标记依然保留，故开机后重新初始化
            DataStorageManager.getIntance(mContext).isRequestedForAttestation = false;
            initAttestation(mContext);// CIBN 播控平台终端认证接口
        } else {
            mLog.i("unknow action");
        }
    }

    private void initAttestation(Context context) {
        if (!SystemUtils.isUserUnlocked(mContext) || !SystemUtils.isNetworkAvailable(mContext))
            return;
        if (DataStorageManager.getIntance(mContext).isCanAttest(context)) {
            new AttestTask(context).execute();
        }
    }
}
