
package com.stv.commonservice.control.protocol.listener;

import com.stv.videochatsdk.api.event.DataChannelMessageEvent;

public interface DataChannelListener {
    void onDataChannelEvent(DataChannelMessageEvent event);

}
