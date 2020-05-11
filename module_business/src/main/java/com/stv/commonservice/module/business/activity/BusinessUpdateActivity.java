
package com.stv.commonservice.module.business.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.BusinessManager;
import com.stv.commonservice.module.business.R;
import com.stv.commonservice.module.business.processor.BusinessTask;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.ToastUtils;

public class BusinessUpdateActivity extends AppCompatActivity {
    private final String TAG = "BusinessUpdateActivity";
    private ScrollView scrollView;
    private TextView tvMsg;
    private StringBuilder msgBuilder;
    private BusinessTask businessTask = null;

    public static void startActivity() {
        Intent intent = new Intent();
        intent.setClass(BaseHelper.getContext(), BusinessUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseHelper.getContext().startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        scrollView = findViewById(R.id.scroll_view);
        tvMsg = findViewById(R.id.tv_msg);

        businessTask = BusinessManager.getInstance().getProcessor().getBusinessTask();
        businessTask.setListener(text -> BusinessUpdateActivity.this.updateText(text));

        startWork();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startWork();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (businessTask.isRunning() && KeyEvent.KEYCODE_BACK == keyCode) {
            ToastUtils.showToast(this, R.string.activity_toast_back_key_disable);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("CheckResult")
    private void startWork() {
        tvMsg.setText(R.string.activity_text_ready);

        msgBuilder = new StringBuilder();
        businessTask.startWork();
    }

    private void updateText(String text) {
        tvMsg.post(() -> {
            msgBuilder.append(text).append("\n");
            tvMsg.setText(msgBuilder.toString());
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

}
