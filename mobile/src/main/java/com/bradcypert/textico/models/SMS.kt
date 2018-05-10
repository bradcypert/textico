package com.bradcypert.textico.models

import android.telephony.PhoneNumberUtils

import java.util.Date

class SMS private constructor(number: String, body: String, timestamp: Date, id: Int, read: Boolean, sender: String, sentByMe: Boolean, threadId: String) : Message() {
    private val DIGIT_REGEX = "\\d+"
    private val US_ISO = "US"

    class SMSBuilder : MessageProperties() {
        private val UNREAD = "0"
        private val READ = "1"

        fun setNumber(number: String): SMSBuilder {
            this.number = number
            return this
        }

        fun setBody(body: String): SMSBuilder {
            this.body = body
            return this
        }

        fun setTimestamp(timestamp: Date): SMSBuilder {
            this.timestamp = timestamp
            return this
        }

        fun setId(id: Int): SMSBuilder {
            this.id = id
            return this
        }

        fun setThreadId(threadId: String): SMSBuilder {
            this.threadId = threadId
            return this
        }

        fun setRead(read: String): SMSBuilder {
            this.read = read == READ
            return this
        }

        fun setPerson(person: String): SMSBuilder {
            this.person = person
            return this
        }

        fun setSentByMe(sentByMe: Boolean): SMSBuilder {
            this.sentByMe = sentByMe
            return this
        }

        @Throws(Exception::class)
        fun build(): SMS {
            return if (this.number == null || this.body == null || this.timestamp == null) {
                throw Exception("Required fields for builder not met: Number, Body, Timestamp")
            } else {
                SMS(number, body, timestamp, id, read, person, sentByMe, threadId)
            }
        }
    }

    init {
        if (number.matches(DIGIT_REGEX.toRegex())) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO)
        } else {
            this.number = number
        }
        this.body = body
        this.timestamp = timestamp
        this.id = id
        this.read = read
        this.person = sender
        this.sentByMe = sentByMe
        this.threadId = threadId
        this.messageType = 0 // See MessageProperties.java
    }
}
