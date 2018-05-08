package com.bradcypert.textico.models;

import java.util.Date;

public abstract class MessageProperties {
    protected String number;
    protected String body;
    protected Date timestamp;
    protected boolean read;
    protected int id;
    protected String threadId;
    protected String person;
    protected boolean sentByMe;
    protected boolean isArchived = false;
    // This is shitty but it is what it is
    // 0 = SMS; 1 = MMS
    protected int messageType;
}
