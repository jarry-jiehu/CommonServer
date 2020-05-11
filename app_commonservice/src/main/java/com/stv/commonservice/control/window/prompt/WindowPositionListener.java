
package com.stv.commonservice.control.window.prompt;

import android.view.View;
import android.view.WindowManager;

public interface WindowPositionListener {
    void onWindowUpdateView(View view, WindowManager.LayoutParams params);
}
