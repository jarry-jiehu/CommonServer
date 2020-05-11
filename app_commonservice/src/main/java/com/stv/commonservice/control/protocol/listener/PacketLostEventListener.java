
package com.stv.commonservice.control.protocol.listener;


import com.stv.videochatsdk.api.event.NetWorkStatusEvent;

public interface PacketLostEventListener {
    void onPacketLostEvent(NetWorkStatusEvent event);
}
