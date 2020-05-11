
package com.stv.commonservice.module.business.processor;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.R;
import com.stv.commonservice.module.business.activity.BusinessUpdateActivity;
import com.stv.commonservice.module.business.callback.BusinessListener;
import com.stv.commonservice.module.business.parsor.BusinessApkParsor;
import com.stv.commonservice.module.business.parsor.BusinessFilesParsor;
import com.stv.commonservice.module.business.parsor.BusinessPropsParsor;
import com.stv.commonservice.module.business.utils.UsbUtils;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.ThreadUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BusinessProcessor {
    private final String TAG = "BusinessProcessor";
    private final String DIR_NAME = "/Letv_config";
    private String[] paths;
    private String uDiskPath;
    private BusinessListener listener;
    private AlertDialog alertDialog;
    private BusinessUpdateActivity activity;
    private BusinessTask businessTask;
    private boolean skipDialog = false;
    private boolean skipActivity = false;
    private boolean registe = false;
    private boolean running = false;

    public BusinessProcessor() {
        paths = UsbUtils.getPaths();
    }

    public BusinessProcessor(String... paths) {
        this.paths = paths;
    }

    private boolean containsLetvConfig() {
        if (null == paths || 0 == paths.length) {
            LogUtils.d(TAG, "paths is null or length is 0.");
            return false;
        }
        for (String path : paths) {
            LogUtils.d(TAG, "U Disk path: " + path);
            if (null == path) {
                continue;
            }
            String configDirPath = path + DIR_NAME;
            LogUtils.d(TAG, "config: " + configDirPath);
            File dir = new File(configDirPath);
            if (dir.exists() && dir.isDirectory()) {
                LogUtils.d(TAG, DIR_NAME + " exists and is a directory.");
                uDiskPath = path;
                return true;
            }
        }
        return false;
    }

    public boolean isRunning() {
        return running;
    }

    public void setSkipDialog(boolean skip) {
        skipDialog = skip;
    }

    public void setSkipActivity(boolean skip) {
        skipActivity = skip;
    }

    public void setDiskPath(String path) {
        if (null == path) {
            paths = UsbUtils.getPaths();
        } else {
            paths = new String[] {
                    path
            };
        }
    }

    public void setListener(BusinessListener listener) {
        this.listener = listener;
    }

    public void setActivity(BusinessUpdateActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("CheckResult")
    public void processReceive() {
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    ThreadUtils.sleep(1000);
                    if (containsLetvConfig()) {
                        BaseHelper.getContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        emitter.onNext(true);
                    } else {
                        emitter.onNext(false);
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (s) {
                        showTipDialog();
                    }
                });
    }

    private void showTipDialog() {
        LogUtils.d(TAG, "showTipDialog() path=" + uDiskPath);
        dismiss();

        if (skipDialog) {
            startProcessActivity();
            return;
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dismiss();
                if (registe) {
                    BaseHelper.getContext().unregisterReceiver(this);
                    registe = false;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        BaseHelper.getContext().registerReceiver(receiver, filter);
        registe = true;

        DialogInterface.OnDismissListener onDismissListener = dialog -> {
            if (registe) {
                BaseHelper.getContext().unregisterReceiver(receiver);
                registe = false;
            }
        };

        initDialog(onDismissListener);
        alertDialog.show();
    }

    public void dismiss() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        if (null != activity) {
            activity.finish();
            activity = null;
        }
    }

    private void initDialog(DialogInterface.OnDismissListener onDismissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseHelper.getContext(), R.style.AlertDialog);
        builder.setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_msg)
                .setNegativeButton(R.string.dialog_n, (dialog, which) -> {
                    dismiss();
                    LogUtils.d(TAG, "Cancel.");
                })
                .setPositiveButton(R.string.dialog_y, (dialog, which) -> {
                    LogUtils.d(TAG, "Start.");
                    dismiss();
                    startProcessActivity();
                })
                .setOnDismissListener(onDismissListener)
                .setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            int type;
            if (Build.VERSION.SDK_INT >= 25) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            // type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            alertDialog.getWindow().setType(type);
        }
    }

    private void startProcessActivity() {
        Consumer consumer = (Consumer<Boolean>) result -> LogUtils.d(TAG,
                "Process finished. Result: " + result);
        initBusinessTask(consumer);
        if (skipActivity) {
            startProcessTask();
        } else {
            BusinessUpdateActivity.startActivity();
        }
    }

    private void startProcessTask() {
        businessTask.startWork();
    }

    private void initBusinessTask(Consumer consumer) {
        if (null == businessTask) {
            businessTask = new BusinessTask(consumer);
        }
    }

    public BusinessTask getBusinessTask() {
        return businessTask;
    }

    public boolean processDir() {
        if (null == listener) {
            return false;
        }

        if (!containsLetvConfig()) {
            listener.onFinishProcess();
            return false;
        }
        running = true;
        listener.onStartProcess();

        LogUtils.d(TAG, "processDir() path: " + uDiskPath);
        String configPath = uDiskPath + DIR_NAME;
        /**
         * 1. 处理属性
         */
        BusinessPropsParsor.readProps(configPath, listener);
        /**
         * 2. 处理文件
         */
        BusinessFilesParsor.readFiles(configPath, listener);
        /**
         * 3. 批量卸载
         */
        BusinessApkParsor.uninstallApks(configPath, listener);
        /**
         * 4. 批量安装
         */
        BusinessApkParsor.installApks(configPath, listener);

        running = false;
        listener.onFinishProcess();
        return true;
    }
}
