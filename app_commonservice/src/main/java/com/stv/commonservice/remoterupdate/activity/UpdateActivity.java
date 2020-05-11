
package com.stv.commonservice.remoterupdate.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;

import com.stv.commonservice.R;
import com.stv.commonservice.remoterupdate.manager.CheckUpdateStatusManager;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;

public class UpdateActivity extends Activity implements OnClickListener, OnFocusChangeListener {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, UpdateActivity.class.getSimpleName());
    private Button mBtnNow = null;
    private Button mBtnLater = null;
    private SharedPreferences mSp = null;
    private DataPref mDataPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 记录打开一个activity
        CheckUpdateStatusManager.getInstance(UpdateActivity.this).increaseActivitysNum();
        setContentView(R.layout.layout_update);
        init();
    }

    private void init() {
        mBtnNow = findViewById(R.id.btn_now);
        mBtnNow.setOnFocusChangeListener(this);
        mBtnNow.setOnClickListener(this);
        mBtnLater = findViewById(R.id.btn_later);
        mBtnLater.setOnFocusChangeListener(this);
        mBtnLater.setOnClickListener(this);
        // mSp = getSharedPreferences(Constants.Y_PREFERENCE_NAME,
        // MODE_PRIVATE);
        mDataPref = DataPref.getInstance(this);
        mBtnNow.setFocusable(true);
        mBtnNow.requestFocus();
    }

    @Override
    protected void onStop() {
        mLog.i("remoterupdate  onStop goto finish");
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        // 记录关闭一个Activity
        CheckUpdateStatusManager.getInstance(UpdateActivity.this).reduceActivitysNum();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_now:
                Intent intent = new Intent(this, UpdateProgressActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_later:
                mDataPref.setRemoterLater(true);
                // mSp.edit().putBoolean(Constants.Y_PREFERENCE_UPDATE_LATER, true).commit();
                finish();
                break;
            default:
                finish();
                break;
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        v.setSelected(hasFocus);
    }
}
