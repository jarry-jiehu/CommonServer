
package com.stv.commonservice.common;

import android.content.Intent;

public abstract class EventDistribute implements Runnable {
    public abstract void onHandleIntent(Intent intent);

    private Intent mIntent;

    public EventDistribute() {
    }

    public EventDistribute setIntent(Intent intent) {
        mIntent = intent;
        return this;
    }

    @Override
    public void run() {
        onHandleIntent(mIntent);
    }
}
