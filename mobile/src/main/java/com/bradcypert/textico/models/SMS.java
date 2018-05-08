package com.bradcypert.textico.models;

import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import java.util.Date;

public class SMS extends Message {
    private final String DIGIT_REGEX = "\\d+";
    private final String US_ISO = "US";

    public static class SMSBuilder extends MessageProperties {
        private final String UNREAD = "0";
        private final String READ = "1";

        public SMSBuilder() {}

        public SMSBuilder setNumber(String number) {
            this.number = number;
            return this;
        }

        public SMSBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public SMSBuilder setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SMSBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public SMSBuilder setThreadId(String threadId) {
            this.threadId = threadId;
            return this;
        }

        public SMSBuilder setRead(String read) {
            this.read = read.equals(READ);
            return this;
        }

        public SMSBuilder setPerson(String person) {
            this.person = person;
            return this;
        }

        public SMSBuilder setSentByMe(boolean sentByMe) {
            this.sentByMe = sentByMe;
            return this;
        }

        public SMS build() throws Exception {
            if(this.number == null || this.body == null || this.timestamp == null) {
                throw new Exception("Required fields for builder not met: Number, Body, Timestamp");
            } else {
                return new SMS(number, body, timestamp, id, read, person, sentByMe, threadId);
            }
        }
    }

    private SMS(String number, String body, Date timestamp, int id, boolean read, String sender, boolean sentByMe, String threadId) {
        if (number.matches(DIGIT_REGEX)) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO);
        } else {
            this.number = number;
        }
        this.body = body;
        this.timestamp = timestamp;
        this.id = id;
        this.read = read;
        this.person = sender;
        this.sentByMe = sentByMe;
        this.threadId = threadId;
        this.messageType = 0; // See MessageProperties.java
    }
}
