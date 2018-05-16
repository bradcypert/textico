package com.bradcypert.textico.models

import android.telephony.PhoneNumberUtils
import io.realm.RealmObject
import java.util.Date

/**
 * Anything the user sees will be powered by a realm model. We'll use realm because supporting different db types on the fly is a pain in the ass.
 * So we can coerce both MMS and SMS into a Message Model and work with that from there.
 */
open class Message(var number: String? = null,
              var body: String? = null,
              var timestamp: Date? = null,
              var read: Boolean = false,
              var id: Int = 0,
              var threadId: String? = null,
              var person: String? = null,
              var sentByMe: Boolean = false,
              var isArchived: Boolean = false,
              var messageType: Int = 0, //0 = SMS, 1 = MMS
              var type: String? = null,
              var part: String? = null) : RealmObject(),  Comparable<Message> {

    var rootNumber: String? = number?.replace("\\D+".toRegex(), "")

    val senderId: String?
        get() = this.person

    val isSentByMe: Boolean
        get() = this.sentByMe


    fun hasBeenRead(): Boolean {
        return this.read
    }

    override fun toString(): String {
        return super.toString() + ">>$number>>$rootNumber"
    }

    override fun compareTo(other: Message): Int {
        return if (this.timestamp == null || other.timestamp == null) {
            0
        } else this.timestamp!!.compareTo(other.timestamp)
    }

    init {
        if (number != null && number!!.matches(DIGIT_REGEX.toRegex())) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO)
        }

        rootNumber = if (rootNumber?.length == 11) {
            rootNumber?.substring(1)
        } else {
            rootNumber
        }
    }

    companion object {
        private const val DIGIT_REGEX = "\\d+"
        private const val US_ISO = "US"
    }
}
