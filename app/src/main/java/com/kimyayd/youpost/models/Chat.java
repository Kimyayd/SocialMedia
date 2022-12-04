package com.kimyayd.youpost.models;

public class Chat {
    private String id;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String date;
    private String hour;
    private boolean delete;




    public Chat(String id, String sender, String receiver, String message, boolean isseen, String date, String hour, boolean delete) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.date = date;
        this.hour = hour;
        this.delete = delete;
    }

    public String getHour() {
        return hour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
