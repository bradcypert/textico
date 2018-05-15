package com.bradcypert.textico.models

import android.graphics.Bitmap
import android.telephony.PhoneNumberUtils
import io.realm.RealmObject

import java.util.Date

data class MMS(var number: String?,
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
        if (number?.matches(DIGIT_REGEX.toRegex())!!) {
            this.number = PhoneNumberUtils.formatNumber(number, US_ISO)
        }
    }

    companion object {
        private const val DIGIT_REGEX = "\\d+"
        private const val US_ISO = "US"
    }
}
