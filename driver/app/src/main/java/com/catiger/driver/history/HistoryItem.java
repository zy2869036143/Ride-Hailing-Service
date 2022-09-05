package com.catiger.driver.history;

public class HistoryItem {
    private long orderID;
    private String time;
    private String begin;
    private String end;
    private String state;
    private boolean finished;

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    public boolean getFinished() {
        return finished;
    }

    public HistoryItem(long id) {
        orderID = id;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public long getOrderID() {
        return orderID;
    }

    public String getState() {
        return state;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setOrderID(long orderID) {
        this.orderID = orderID;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}
