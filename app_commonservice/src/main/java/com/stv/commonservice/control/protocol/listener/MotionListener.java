
package com.stv.commonservice.control.protocol.listener;

import com.stv.commonservice.control.protocol.bean.MotionEventBean;

public interface MotionListener {
    boolean onTouch(MotionEventBean event);
}
