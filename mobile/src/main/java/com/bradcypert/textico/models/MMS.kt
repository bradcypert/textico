package com.bradcypert.textico.models

import android.telephony.PhoneNumberUtils
import java.util.*

data class MMS(var number: String,
               val body: String?,
               val timestamp: Date?,
               val id: Int,
               val read: Boolean,
               val sender: String?,
               val person: String?,
               val sentByMe: Boolean,
               val threadId: String?,
               val type: String?,
               val part: String?) {

    init {
        if (number.matches(DIGIT_REGEX.toRegex())) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO)
        }
    }

    override fun toString(): String {
        return "$number"
    }

    companion object {
        private const val DIGIT_REGEX = "\\d+"
        private const val US_ISO = "US"
    }
}
