package com.example.lapitchat.data.model;

public class Messages {
    private String message;
    private boolean seen;
    private String type;
    private long time;
    private String from;

    public Messages(String message) {
        this.message = message;
    }

    public Messages() {

    }

    public Messages(String message, boolean seen, long time, String type) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public boolean isSeen() {
        return seen;
    }


    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


}
