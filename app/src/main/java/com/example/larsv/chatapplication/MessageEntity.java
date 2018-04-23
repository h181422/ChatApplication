package com.example.larsv.chatapplication;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "sender")
    private String sender;

    @ColumnInfo(name = "receiver")
    private String receiver;

    @ColumnInfo(name = "content")
    private String content;

    public MessageEntity(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;

    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUid() {
        return uid;
    }
    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }


}