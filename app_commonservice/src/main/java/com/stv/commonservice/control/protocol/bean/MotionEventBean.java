
package com.stv.commonservice.control.protocol.bean;

/**
 * touch事件传输bean
 */
public class MotionEventBean {
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

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getMetaState() {
        return metaState;
    }

    public void setMetaState(int metaState) {
        this.metaState = metaState;
    }

    /**
     * The kind of action being performed
     */
    private int action;
    /**
     * The X coordinate of this event
     */
    private float x;
    /**
     * The Y coordinate of this event
     */
    private float y;
    /**
     * The state of any meta / modifier keys that were in effect when the event was generated
     */
    private int metaState;

    public MotionEventBean(long downTime, long eventTime, int action, float x, float y, int metaState) {
        this.downTime = downTime;
        this.eventTime = eventTime;
        this.action = action;
        this.x = x;
        this.y = y;
        this.metaState = metaState;
    }
}
