package com.bradcypert.textico.models;

import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import java.util.Date;

public class MMS extends Message implements Comparable<MMS> {
    private final String DIGIT_REGEX = "\\d+";
    private final String US_ISO = "US";


    public static class MMSBuilder extends MessageProperties {
        private final String UNREAD = "0";
        private final String READ = "1";

        public MMSBuilder() {}

        public MMSBuilder setNumber(String number) {
            this.number = number;
            return this;
        }

        public MMSBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public MMSBuilder setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MMSBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public MMSBuilder setThreadId(String threadId) {
            this.threadId = threadId;
            return this;
        }

        public MMSBuilder setRead(String read) {
            this.read = read.equals(READ);
            return this;
        }

        public MMSBuilder setPerson(String person) {
            this.person = person;
            return this;
        }

        public MMSBuilder setSentByMe(boolean sentByMe) {
            this.sentByMe = sentByMe;
            return this;
        }

        public MMS build() throws Exception {
            if(this.number == null || this.body == null || this.timestamp == null) {
                throw new Exception("Required fields for builder not met: Number, Body, Timestamp");
            } else {
                return new MMS(number, body, timestamp, id, read, person, sentByMe, threadId);
            }
        }
    }

    private MMS(String number, String body, Date timestamp, int id, boolean read, String sender, boolean sentByMe, String threadId) {
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
    }

    @Override
    public int compareTo(@NonNull MMS o) {
        if(this.timestamp == null || o.getTimestamp() == null) {
            return 0;
        }

        return this.timestamp.compareTo(o.getTimestamp());
    }
}
