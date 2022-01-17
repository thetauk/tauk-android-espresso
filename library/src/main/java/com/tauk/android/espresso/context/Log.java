package com.tauk.android.espresso.context;

public class Log {
    private long timestamp;
    private String level;
    private String type;
    private String message;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
