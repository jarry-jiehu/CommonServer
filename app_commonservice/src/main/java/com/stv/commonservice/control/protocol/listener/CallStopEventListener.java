
package com.stv.commonservice.control.protocol.listener;

import com.stv.videochatsdk.api.event.CallStopEvent;

/**
 * 掛斷電話的Listener
 */
public interface CallStopEventListener {

    void onStopEvent(CallStopEvent cse2);

}
