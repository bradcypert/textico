package com.bradcypert.textico.models;

import java.util.Date;

public abstract class Message extends MessageProperties {
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
}
