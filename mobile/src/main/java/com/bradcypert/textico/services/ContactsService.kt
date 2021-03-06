package com.bradcypert.textico.services

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager

import com.bradcypert.textico.models.Contact

object ContactsService {
    private fun getCursorForPhoneNumber(contentResolver: ContentResolver, number: String): Cursor? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return contentResolver.query(uri, null, null, null, null)
    }

    private fun buildContactFromCursor(c: Cursor): Contact {
        return if (c.count != 0) {
            Contact.ContactBuilder()
                    .setName(c.getString(c.getColumnIndex("DISPLAY_NAME")))
                    .setPicUri(c.getString(c.getColumnIndex("PHOTO_URI")))
                    .setStarred(c.getString(c.getColumnIndex("STARRED")) == "1")
                    .build()
        } else {
            Contact.ContactBuilder()
                    .setName(null)
                    .setPicUri(null)
                    .setStarred(false)
                    .build()
        }
    }

    fun getContactForNumber(contentResolver: ContentResolver, number: String): Contact {
        val c = getCursorForPhoneNumber(contentResolver, number)
        c!!.moveToFirst()

        return buildContactFromCursor(c)
    }
}
