
package com.stv.commonservice.attestation.net;

import android.content.Context;

import com.stv.commonservice.attestation.model.Info;
import com.stv.commonservice.attestation.util.AttestationUtils;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.NetUtils;
import com.stv.commonservice.util.SystemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import eui.lighthttp.Helper;
import eui.lighthttp.Response;

public class AttestTask {
    private static final int CONNECT_TIME_OUT = 3000;
    private static final int READ_TIME_OUT = 3000;
    // 未录用
    private static final String NOT_REGISTER = "0";
    // 正常
    private static final String NORMAL_REGISTER = "1";
    // 被禁用
    private static final String Disable_REGISTER = "2";
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_ATTEST, AttestTask.class.getSimpleName());
    private Context mContext;
    private AtomicBoolean mIsSuccess = new AtomicBoolean(false);
    private String mRequestResult = "";
    private String mSubUrl = "http://cert.ott.cibntv.net/api/auth/?";
    private Info mInfo;
    private DataPref mDataPref;
    private Helper mHelper;

    public AttestTask(Context context) {
        this.mContext = context;
        mDataPref = DataPref.getInstance(context);
        mHelper = new Helper();
        mHelper.getClient().setConnectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        mHelper.getClient().setReadTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS);

    }

    public void execute() {
        // 标记播控认证正在请求
        DataStorageManager.getIntance(mContext).isRequestingForAttestation = true;
        onExecute();
        afterExecute();
        // 标记播控认证结束请求
        DataStorageManager.getIntance(mContext).isRequestingForAttestation = false;
    }

    protected void onExecute() {
        Map<String, String> param = getParam();
        for (int i = 0; i < 3; i++) {
            mLog.i("to request " + i + " time");
            if (toDoRequest(NetUtils.getUrl(mSubUrl, param))) {
                break;
            } else {
                try {
                    Thread.sleep((i + 1) * 5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void afterExecute() {
        // 1小时轮询
        SystemUtils.setAttestAmler(mContext, Constants.ALARM_ID_ATTEST, Constants.ATTEST_REQUEST_DELAY, Constants.ALARM_BROADCAST_FOR_ATTESTATION);
        mLog.i("requestResult : " + mRequestResult + "\n");
        if (mIsSuccess.get()) {
            if (null != mInfo && null != mInfo.resultCode && !"".equals(mInfo.resultCode)) {
                reportResult();// 上报
                String resultCode = mInfo.getResultCode();
                mLog.i("result code : " + resultCode);
                // 保存此次的requestCode
                mDataPref.setLastAttestCode(resultCode);
                // 标记本次请求成功
                DataStorageManager.getIntance(mContext).isRequestedForAttestation = true;
                if (NOT_REGISTER.equals(resultCode) || Disable_REGISTER.equals(resultCode)) {
                    saveRequestCode(Constants.CARRIER_STATE_DENIED);
                } else if (NORMAL_REGISTER.equals(resultCode)) {
                    // 取消定时器
                    SystemUtils.cancelAttestAmler(mContext, Constants.ALARM_ID_ATTEST, Constants.ALARM_BROADCAST_FOR_ATTESTATION);
                    saveRequestCode(Constants.CARRIER_STATE_ALLOWED);
                } else {
                    saveRequestCode(Constants.CARRIER_STATE_DEFAULT);
                }
                return;
            } else {
                mLog.i("mInfo is null");
                saveRequestCode(Constants.CARRIER_STATE_DEFAULT);
            }
        } else {
            // 三次请求失败
            mLog.i("Request fail");
            reportResult();// 上报
            saveRequestCode(Constants.CARRIER_STATE_DEFAULT);
        }

    }

    private void reportResult() {
        if (!DataStorageManager.getIntance(mContext).isRequestedForAttestation) {// 表示第一次请求
            mLog.i("first report " + mInfo.toString());
            AttestationUtils.report(mContext, mInfo.getResultCode(), mInfo.getDeviceId());
        } else {// 表示轮循
            // 获取上次保存的requestcode
            String getSaveRequestCode = mDataPref.getLastAttestCode();
            mLog.i("repeat report " + mInfo.toString() + " [ getSaveRequestCode ] : [ "
                    + getSaveRequestCode + " ]");
            if (null != getSaveRequestCode && !getSaveRequestCode.equals(mInfo.getResultCode())) {// 如果此次和上次请求的requestcode不同则上报
                AttestationUtils.report(mContext, mInfo.getResultCode(), mInfo.getDeviceId());
                mLog.i("repeat has reported ");
            } else {
                mLog.i("repeat hasn't reported ");
            }
        }
    }

    private boolean toDoRequest(String url) {
        String jsonString = "";
        try {
            Response response = mHelper.requestByGet(url);
            int code = response.getStatusCode();
            jsonString = response.getContent();
            mLog.i("new http tool ,response code:" + code);
            mLog.i("response jsonString:" + jsonString);

            if (200 == code) {
                AttestTask.this.mRequestResult = jsonString;
                mInfo = AttestationUtils.getInfo(mRequestResult);
                mIsSuccess.set(true);
            } else {
                mInfo = new Info();
                mInfo.setResultCode("-2");
            }
        } catch (Exception e) {
            mInfo = new Info();
            mInfo.setResultCode("-3");
            mLog.i("wrong result is [ " + jsonString + " ]");
            mLog.i("xml pase Exception : " + e);
        } catch (Error error) {
            mInfo = new Info();
            mInfo.setResultCode("-3");
            mLog.i("wrong result is [ " + jsonString + " ]");
            mLog.i("xml pase error : " + error);
        }
        return mIsSuccess.get();
    }

    private void saveRequestCode(String requestCode) {
        DataStorageManager.getIntance(mContext).setAttestRequestCode(requestCode);
        mDataPref.setStvAttestResultCode(requestCode);
    }

    public HashMap<String, String> getParam() {// type=1&recordmode=addr=
        HashMap<String, String> generalParam = new HashMap<String, String>();
        generalParam.put("type", "1");
        generalParam.put("recordmode", AttestationUtils.getRecordmode());
        generalParam.put("addr", AttestationUtils.getAddr());
        generalParam.put("partnerid", "stv");
        return generalParam;
    }
}
