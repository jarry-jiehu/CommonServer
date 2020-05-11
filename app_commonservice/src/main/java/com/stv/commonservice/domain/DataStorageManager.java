
package com.stv.commonservice.domain;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.domain.util.DomainUtil;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

import java.util.Map;

/**
 * 该类是以单例模式存在的一个全局对象 1.将一些数据存在内存当中，避免多次读文件造成CPU压力 2.存储一些标记
 */
public class DataStorageManager {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_DOMAIN, DataStorageManager.class.getSimpleName());
    private static DataStorageManager sInstance;
    // 存储所有的域名，lable 和 域名 一一对应
    private Map<String, String> mDomains;
    // 存储 播控认证码
    private String mAttestRequestCode;
    // 存储本次启动是否已经请求过域名下发
    public boolean isRequestedForDomain = false;
    // 存储本次启动是否正在请求域名下发
    public boolean isRequestingForDomain = false;
    // 存储本次启动是否已经请求过播控认证
    public boolean isRequestedForAttestation = false;
    // 存储本次启动是否正在请求播控认证
    public boolean isRequestingForAttestation = false;
    // 存储本次启动激活是否已经请求过
    public boolean isRequestedForActivation = false;
    // 存储本次启动激活是否正在请求
    public boolean isRequestingForActivation = false;
    private DataPref mPref;
    private Context mContext;

    public DataStorageManager(Context context) {
        this.mContext = context.getApplicationContext();
        mPref = DataPref.getInstance(mContext);
    }

    public Map<String, String> getDomains() {
        if (null == mDomains || mDomains.size() < 1) {
            mDomains = DomainUtil.parseDomainList(DomainUtil.readDomainJson());
        }
        return mDomains;
    }

    public void setDomains(Map<String, String> domains) {
        this.mDomains = domains;
    }

    public String getAttestRequestCode() {
        if (TextUtils.isEmpty(mAttestRequestCode)) {
            mAttestRequestCode = readAttestRequestCode();
        }
        return mAttestRequestCode;
    }

    public void setAttestRequestCode(String attestRequestCode) {
        this.mAttestRequestCode = attestRequestCode;
    }

    public synchronized static DataStorageManager getIntance(Context context) {
        if (null == sInstance) {
            sInstance = new DataStorageManager(context);
        }
        return sInstance;
    }

    /**
     * 读取缓存中播控认证的返回码
     * @return
     */
    private String readAttestRequestCode() {
        return mPref.getStvAttestResuleCode();
    }

    /**
     * 是否可以进行播控认证
     * @param context
     * @return
     */
    public boolean isCanAttest(Context context) {
        mLog.i("isCanAttest : " + "[ isNetWork : " + SystemUtils.isNetworkAvailable(context)
                + " ] [ isRequestedForAttestation : "
                + isRequestedForAttestation
                + " ] [isRequestingForAttestation : " + isRequestingForAttestation + " ]");
        return SystemUtils.isCibn() && SystemUtils.isNetworkAvailable(context)
                && !isRequestedForAttestation
                && !isRequestingForAttestation;
    }

    /**
     * 是否可以拉取域名下发
     * @param context
     * @return
     */
    public boolean isCanRequestDomain(Context context) {
        mLog.i(" isCanRequestDomain : [ isRequestedForDomain : "
                + isRequestedForDomain + " ] [ isRequesting: "
                + isRequestingForDomain + " ]");
        return SystemUtils.isNetworkAvailable(context) && !isRequestedForDomain && !isRequestingForDomain;
    }

}
