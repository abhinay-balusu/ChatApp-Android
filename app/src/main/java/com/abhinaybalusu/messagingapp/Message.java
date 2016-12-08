package com.abhinaybalusu.messagingapp;

import java.util.Date;

/**
 * Created by abhinaybalusu on 11/18/16.
 */
public class Message implements Comparable<Message>{

    String messageText, sender, receiver, dateAndTimePosted, readStatus, mId, mIcon;

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
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

    public String getDateAndTimePosted() {
        return dateAndTimePosted;
    }

    public void setDateAndTimePosted(String dateAndTimePosted) {
        this.dateAndTimePosted = dateAndTimePosted;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmIcon() {
        return mIcon;
    }

    public void setmIcon(String mIcon) {
        this.mIcon = mIcon;
    }

    @Override
    public int compareTo(Message another) {

        Date d1 = new Date(this.dateAndTimePosted);
        Date d2 = new Date(another.dateAndTimePosted);

        if (d1.compareTo(d2)>0)
        {
            return 1;
        }
        else if(d1.compareTo(d2)<0)
        {
            return -1;
        }
        return 0;
    }
}
