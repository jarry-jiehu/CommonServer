
package com.stv.commonservice.smarthome.manager;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.stv.commonservice.smarthome.model.HouseInfoResponse;
import com.stv.commonservice.smarthome.model.LoginResponse;
import com.stv.commonservice.smarthome.model.RegisterResponse;
import com.stv.commonservice.smarthome.util.Constant;
import com.stv.commonservice.smarthome.util.Sha256;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import eui.tv.TvManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 设备管理类
 */
public class DeviceManager {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, DeviceManager.class.getSimpleName());
    private static volatile DeviceManager sInstance;
    private Context mContext;
    private static OkHttpClient mOkHttpClient;

    private DeviceManager(Context context) {
        mContext = context;
        mOkHttpClient = new OkHttpClient();
    }

    public static DeviceManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (DeviceManager.class) {
                if (null == sInstance) {
                    sInstance = new DeviceManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 登陆账户（通过乐视账号）
     *
     * @param url
     */
    public void login(String url) {
        mLog.d("*****login EIOT******" + url);
        if (TextUtils.isEmpty(url))
            return;
        String uid = AccountInfoManager.getInstance().getAccountInfo(mContext,
                Constant.COLUMN_UID);
        String token = AccountInfoManager.getInstance().getAccountInfo(mContext,
                Constant.COLUMN_TOKEN);
        mLog.d("isLoginAccount uid=" + uid + " token=" + token);
        if (TextUtils.isEmpty(token))
            return;
        try {
            // 请求体--------->
            JSONObject obj = new JSONObject();
            obj.putOpt("deviceSN", TvManager.getDeviceId());
            obj.putOpt("terminaterType", "android"); //
            mLog.d("Request Body: " + obj.toString());
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder().url(url).headers(getHeaders(Constant.ROUTE_LOGIN, obj.toString(), token)).post(formBody).build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    mLog.e("login onFailure:" + e.getMessage());
                }

                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    mLog.d("onResponse code:" + response.code() + ";message:" + response.message());
                    if (response.code() >= HttpURLConnection.HTTP_OK && response.code() < HttpURLConnection.HTTP_MULT_CHOICE) {
                        String message = response.body().string();
                        mLog.d("onResponse body:" + message);
                        LoginResponse registerBean = new Gson().fromJson(message, LoginResponse.class);
                        if (registerBean.getStatus() == 1) {
                            DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_LE_TOKEN, registerBean.getResult().getToken());
                            register(Constant.getRegisterUrl(mContext));

                        }
                    } else if (response.code() > HttpURLConnection.HTTP_MULT_CHOICE && response.code() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        String location = response.headers().get("Location"); // 需要重定向http <---> https
                        mLog.w("location body:" + location);
                        login(location);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册ELINK服务
     *
     * @param url
     */
    public void register(String url) {
        mLog.d("****register ELINK*****" + url);
        if (TextUtils.isEmpty(url))
            return;
        String leToken = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_LE_TOKEN, "");
        mLog.d(" leToken: " + leToken);
        if (TextUtils.isEmpty(leToken))
            return;
        try {
            JSONObject obj = new JSONObject();
            obj.putOpt("productKey", Constant.PRODUCT_KEY);
            obj.putOpt("deviceName", TvManager.getDeviceName(mContext).replaceAll(" ", ""));
            obj.putOpt("deviceId", TvManager.getDeviceId());
            obj.putOpt("manufacture", "letv");
            mLog.d("RequestBody: " + obj.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .headers(getHeaders(Constant.ROUTE_REGISTER_DEVICE, obj.toString(), leToken))
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString()))
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mLog.e("register onFailure:" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    mLog.d("onResponse  response code :" + response.code() + " ;message :" + response.message());
                    int code = response.code();
                    mLog.d("reporter code:" + code);
                    if (code == HttpURLConnection.HTTP_OK) {
                        String result = response.body().string();
                        mLog.d("update reporter response data :" + result);
                        RegisterResponse registerBean = new Gson().fromJson(result, RegisterResponse.class);
                        if (registerBean.getStatus() == 1) {
                            DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_ELINK_ID, registerBean.getResult().getElinkId());
                            DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_DEVICE_SECRET, registerBean.getResult().getDeviceSecret());
                            queryHouseInfo(Constant.getHouseQueryUrl(mContext), true);
                        }
                    } else if (response.code() > HttpURLConnection.HTTP_MULT_CHOICE && response.code() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        // 需要重定向http <---> https
                        String location = response.headers().get("Location");
                        mLog.w("location body:" + location);
                        register(location);
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过leToken查询房屋信息
     *
     * @param url
     * @param isBind 绑定还是解绑设备
     */
    public void queryHouseInfo(String url, boolean isBind) {
        mLog.d("*****Query House Info******" + url);
        if (TextUtils.isEmpty(url))
            return;
        String leToken = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_LE_TOKEN, "");
        mLog.d(" leToken: " + leToken);
        if (TextUtils.isEmpty(leToken))
            return;
        try {
            Request request = new Request.Builder().url(url).headers(getHeaders(Constant.ROUTE_QUERY_HOUSE, null, leToken))
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    mLog.e("==onFailure==" + e.getMessage());
                }

                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    mLog.d("onResponse code:" + response.code() + ";message:" + response.message());
                    if (response.code() >= HttpURLConnection.HTTP_OK && response.code() < HttpURLConnection.HTTP_MULT_CHOICE) {
                        String message = response.body().string();
                        mLog.d("onResponse body:" + message);
                        HouseInfoResponse houseInfo = new Gson().fromJson(message, HouseInfoResponse.class);
                        if (houseInfo.getStatus() == 1) {
                            DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_HOUSE_ID, houseInfo.getResult().get(0).getHouseId());
                            if (isBind)
                                bindDevice(Constant.getDeviceBindUrl(mContext));
                            else
                                unbindDevice(Constant.getDeviceUnbindUrl(mContext));
                        }
                    } else if (response.code() > HttpURLConnection.HTTP_MULT_CHOICE && response.code() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        String location = response.headers().get("Location"); // 需要重定向http <---> https
                        mLog.w("location body:" + location);
                        queryHouseInfo(location, isBind);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绑定设备
     *
     * @param url
     */
    private void bindDevice(String url) {
        mLog.d("*****bind Device******" + url);
        if (TextUtils.isEmpty(url))
            return;
        String leToken = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_LE_TOKEN, "");
        String elinkId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_ELINK_ID, "");
        String houseId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_HOUSE_ID, "");
        mLog.d(" leToken: " + leToken + " ;elinkId: " + elinkId + " ;houseId: " + houseId);
        if (TextUtils.isEmpty(leToken) || TextUtils.isEmpty(elinkId) || TextUtils.isEmpty(houseId))
            return;
        try {
            JSONObject obj = new JSONObject();
            obj.putOpt("elinkId", elinkId);
            obj.putOpt("typeId", Constant.PRODUCT_KEY);
            obj.putOpt("manufacturer", "letv");
            obj.putOpt("houseId", houseId);
            mLog.d("Param: " + obj.toString());
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder().url(url).headers(getHeaders(Constant.ROUTE_BIND_DEVICE, obj.toString(), leToken)).post(formBody).build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    mLog.w("==onFailure==" + e.getMessage());
                }

                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    mLog.d("onResponse code:" + response.code() + ";message:" + response.message());
                    if (response.code() >= HttpURLConnection.HTTP_OK && response.code() < HttpURLConnection.HTTP_MULT_CHOICE) {
                        String message = response.body().string();
                        mLog.d("onResponse body:" + message);
                        try {
                            JSONObject json = new JSONObject(message);
                            if (json.getInt("status") == 1 && mDeviceCallback != null) {
                                mDeviceCallback.deviceBind();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.code() > HttpURLConnection.HTTP_MULT_CHOICE && response.code() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        // 需要重定向http <---> https
                        String location = response.headers().get("Location");
                        mLog.w("location body:" + location);
                        bindDevice(location);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设备解绑
     *
     * @param url
     */
    private void unbindDevice(String url) {
        mLog.d("*****Unbind Device******" + url);
        if (TextUtils.isEmpty(url))
            return;
        String leToken = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_LE_TOKEN, "");
        String elinkId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_ELINK_ID, "");
        String houseId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_HOUSE_ID, "");
        mLog.d(" leToken: " + leToken + " ;elinkId: " + elinkId + " ;houseId: " + houseId);
        if (TextUtils.isEmpty(leToken) || TextUtils.isEmpty(elinkId) || TextUtils.isEmpty(houseId))
            return;
        try {
            JSONObject obj = new JSONObject();
            obj.putOpt("elinkId", elinkId);
            obj.putOpt("houseId", houseId);
            mLog.d("Param: " + obj.toString());
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder().url(url).headers(getHeaders(Constant.ROUTE_UNBIND_DEVICE, obj.toString(), leToken)).post(formBody).build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    mLog.w("==onFailure==" + e.getMessage());
                }

                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    mLog.d("onResponse code:" + response.code() + ";message:" + response.message());
                    if (response.code() >= HttpURLConnection.HTTP_OK && response.code() < HttpURLConnection.HTTP_MULT_CHOICE) {
                        String message = response.body().string();
                        mLog.d("onResponse body:" + message);
                        try {
                            JSONObject json = new JSONObject(message);
                            if (json.getInt("status") == 1 && mDeviceCallback != null) {
                                mDeviceCallback.deviceUnbind();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() > HttpURLConnection.HTTP_MULT_CHOICE && response.code() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        // 需要重定向http <---> https
                        String location = response.headers().get("Location");
                        mLog.w("location body:" + location);
                        unbindDevice(location);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装请求头
     *
     * @param location 请求的位置
     * @param body     请求体
     * @param token    请求的token
     * @return
     */
    private Headers getHeaders(String location, String body, String token) {
        long totalSeconds = System.currentTimeMillis() / 1000;
        StringBuilder builder = new StringBuilder();
        builder.append(location);
        if (!TextUtils.isEmpty(body))
            builder.append(body);
        builder.append(Constant.APP_ID);
        builder.append(Constant.APP_KEY);
        builder.append(totalSeconds);
        HashMap<String, String> head = new HashMap<>();
        head.put("token", token);
        head.put("appId", Constant.APP_ID);
        head.put("sign", Sha256.getSHA256(builder.toString()));
        head.put("timestamp", String.valueOf(totalSeconds));
        return Headers.of(head);
    }

    private DeviceCallback mDeviceCallback;

    public void setDeviceCallback(DeviceCallback callback) {
        mDeviceCallback = callback;
    }

    public interface DeviceCallback {
        void deviceBind();

        void deviceUnbind();
    }
}
