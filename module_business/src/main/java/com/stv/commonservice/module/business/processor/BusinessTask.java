
package com.stv.commonservice.module.business.processor;

import android.annotation.SuppressLint;
import android.os.SystemClock;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.BusinessManager;
import com.stv.commonservice.module.business.R;
import com.stv.commonservice.module.business.callback.BusinessListener;
import com.stv.library.common.util.LogUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BusinessTask implements BusinessListener {
    private final String TAG = "BusinessTask";
    private BusinessProcessor businessProcessor;
    private Disposable disposable;
    private boolean headerProp, headerFiles, headerInstall, headerUninstall;
    private long startT;
    private boolean running;

    private static Consumer consumer = null;

    private TaskListener listener;

    public BusinessTask(Consumer con) {
        this.consumer = con;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    @SuppressLint("CheckResult")
    public void startWork() {
        businessProcessor = BusinessManager.getInstance().getProcessor();

        running = true;
        disposable = Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    LogUtils.d(TAG, "startWork() process...");
                    businessProcessor.setActivity(null);
                    businessProcessor.setListener(BusinessTask.this);
                    boolean success = businessProcessor.processDir();
                    emitter.onNext(success);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .delaySubscription(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    LogUtils.d(TAG, "startWork() process finish: " + aBoolean);
                    if (null != consumer) {
                        consumer.accept(aBoolean);
                    }
                    businessProcessor.setListener(null);
                    businessProcessor.setActivity(null);
                    running = false;
                    cancelDisposable();
                });
    }

    private void cancelDisposable() {
        if (null != disposable && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    private void updateText(String text) {
        if (null != listener) {
            listener.updateText(text);
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void onStartProcess() {
        LogUtils.d(TAG, "onStartProcess()");
        startT = SystemClock.elapsedRealtime();
        String text = BaseHelper.getContext().getString(R.string.activity_text_start);
        updateText(text);
        headerProp = false;
        headerFiles = false;
        headerInstall = false;
        headerUninstall = false;
    }

    @Override
    public void onWriteProps(int total, int current, boolean result, String prop) {
        if (null == prop) {
            return;
        }
        if (!headerProp) {
            headerProp = true;
            LogUtils.d(TAG, "onWriteProps() Business Props");
            updateText("Business Props");
        }
        LogUtils.d(TAG, "onWriteProps() " + current + "/" + total + " result: " + result + " prop: " + prop);
        String text = BaseHelper.getContext().getString(R.string.activity_text_props, current, total, result
                ? BaseHelper.getContext().getString(R.string.activity_text_success)
                : BaseHelper.getContext().getString(R.string.activity_text_failure),
                prop);
        updateText(text);
    }

    @Override
    public void onCopyFiles(int total, int current, String msg) {
        if (null == msg) {
            return;
        }
        if (!headerFiles) {
            headerFiles = true;
            LogUtils.d(TAG, "onCopyFiles() Business Files");
            updateText("Business Files");
        }
        LogUtils.d(TAG, "onCopyFiles() " + current + "/" + total + " msg: " + msg);
        String text = BaseHelper.getContext().getString(R.string.activity_text_files, current, total, msg);
        updateText(text);
    }

    @Override
    public void onInstallApks(int total, int current, boolean result, String path) {
        if (null == path) {
            return;
        }
        if (!headerInstall) {
            headerInstall = true;
            LogUtils.d(TAG, "onInstallApks() Business Install");
            updateText("Business Install");
        }
        LogUtils.d(TAG, "onInstallApks() " + current + "/" + total + " result: " + result + " path: " + path);
        String text = BaseHelper.getContext().getString(R.string.activity_text_install, current, total, result
                ? BaseHelper.getContext().getString(R.string.activity_text_success)
                : BaseHelper.getContext().getString(R.string.activity_text_failure),
                path);
        updateText(text);
    }

    @Override
    public void onUnInstallApks(int total, int current, boolean result, String pkg) {
        if (null == pkg) {
            return;
        }
        if (!headerUninstall) {
            headerUninstall = true;
            LogUtils.d(TAG, "onUnInstallApks() Business Uninstall");
            updateText("Business Uninstall");
        }
        LogUtils.d(TAG, "onUnInstallApks() " + current + "/" + total + " result: " + result + " pkg: " + pkg);
        String text = BaseHelper.getContext().getString(R.string.activity_text_uninstall, current, total, result
                ? BaseHelper.getContext().getString(R.string.activity_text_success)
                : BaseHelper.getContext().getString(R.string.activity_text_failure),
                pkg);
        updateText(text);
    }

    @Override
    public void onFinishProcess() {
        long useT = SystemClock.elapsedRealtime() - startT;
        LogUtils.d(TAG, "onFinishProcess() use time: " + useT / 1000 + "s");
        String text = BaseHelper.getContext().getString(R.string.activity_text_finish, useT / 1000);
        updateText(text);
    }

    public interface TaskListener {
        void updateText(String text);
    }
}
