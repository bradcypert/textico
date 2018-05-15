package com.bradcypert.textico.models

import android.telephony.PhoneNumberUtils
import io.realm.RealmObject

import java.util.Date

class SMS constructor(var number: String?,
                      val body: String?,
                      val timestamp: Date?,
                      val id: Int,
                      val read: Boolean,
                      val sender: String?,
                      val sentByMe: Boolean,
                      val threadId: String?) {

    // TODO: Refactor this crap
    public val person: String?

    init {
        if (number?.matches(DIGIT_REGEX.toRegex())!!) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO)
        } else {
            this.number = number
        }
        this.person = sender
    }

    companion object {
        private const val DIGIT_REGEX = "\\d+"
        private const val US_ISO = "US"
    }
}
