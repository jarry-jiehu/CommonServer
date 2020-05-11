
package com.stv.commonservice.control.protocol.bean;

/**
 * 浮层切换位置事件传输bean
 */
public class SwitchEventBean {
    private int action;
    /**
     * The time (in ms) when the user originally pressed down to start a stream of position events.
     */
    private long downTime;
    /**
     * The the time (in ms) when this specific event was generated. This must be obtained from.
     */
    private long eventTime;

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getMetaState() {
        return metaState;
    }

    public void setMetaState(int metaState) {
        this.metaState = metaState;
    }

    private int metaState;

    public SwitchEventBean(long downTime, long eventTime, int action, float x, float y, int metaState) {
        this.downTime = downTime;
        this.eventTime = eventTime;
        this.action = action;
        this.metaState = metaState;
    }
}
