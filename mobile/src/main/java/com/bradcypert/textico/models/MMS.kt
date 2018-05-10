package com.bradcypert.textico.models

import android.graphics.Bitmap
import android.telephony.PhoneNumberUtils

import java.util.Date

class MMS private constructor(number: String?, body: String?, timestamp: Date?, id: Int, read: Boolean, sender: String?, sentByMe: Boolean, threadId: String?, val type: String?, val part: String?, val image: Bitmap?) : Message() {
    private val DIGIT_REGEX = "\\d+"
    private val US_ISO = "US"


    class MMSBuilder : MessageProperties() {
        private val UNREAD = "0"
        private val READ = "1"
        private var type: String? = null
        private var part: String? = null
        private var image: Bitmap? = null

        fun setNumber(number: String?): MMSBuilder {
            this.number = number
            return this
        }

        fun setBody(body: String?): MMSBuilder {
            this.body = body
            return this
        }

        fun setTimestamp(timestamp: Date?): MMSBuilder {
            this.timestamp = timestamp
            return this
        }

        fun setId(id: Int): MMSBuilder {
            this.id = id
            return this
        }

        fun setThreadId(threadId: String?): MMSBuilder {
            this.threadId = threadId
            return this
        }

        fun setRead(read: String?): MMSBuilder {
            this.read = read == READ
            return this
        }

        fun setPerson(person: String?): MMSBuilder {
            this.person = person
            return this
        }

        fun setSentByMe(sentByMe: Boolean): MMSBuilder {
            this.sentByMe = sentByMe
            return this
        }

        fun setType(type: String?): MMSBuilder {
            this.type = type
            return this
        }

        fun setPart(part: String?): MMSBuilder {
            this.part = part
            return this
        }

        fun setImage(image: Bitmap?): MMSBuilder {
            this.image = image
            return this
        }

        @Throws(Exception::class)
        fun build(): MMS {
            return if (this.number == null || this.timestamp == null) {
                throw Exception("Required fields for builder not met: Number, Body, Timestamp")
            } else {
                MMS(number, body, timestamp, id, read, person, sentByMe, threadId, type, part, image)
            }
        }
    }

    init {
        if (number?.matches(DIGIT_REGEX.toRegex())!!) {
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
        this.messageType = 1 // See MessageProperties.java
    }
}
