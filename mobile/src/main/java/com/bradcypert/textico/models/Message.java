package com.bradcypert.textico.models;

import android.support.annotation.NonNull;

import java.util.Date;

public abstract class Message extends MessageProperties implements Comparable<Message> {
    public String getNumber() {
        return this.number;
    }

    public String getBody() {
        return this.body;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public int getId() {
        return this.id;
    }

    public String getThreadId() {
        return this.threadId;
    }

    public boolean hasBeenRead() {
        return this.read;
    }

    public String getSenderId() {
        return this.person;
    }

    public boolean isSentByMe() {
        return this.sentByMe;
    }

    public int getMessageType() {
        return this.messageType;
    }

    @Override
    public int compareTo(@NonNull Message o) {
        if(this.timestamp == null || o.getTimestamp() == null) {
            return 0;
        }

        return this.timestamp.compareTo(o.getTimestamp());
    }
}
