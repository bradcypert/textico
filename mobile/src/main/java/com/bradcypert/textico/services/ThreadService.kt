package com.bradcypert.textico.services

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.TelephonyManager

import com.bradcypert.textico.models.Contact

object ThreadService {

    private fun getCursorForId(contentResolver: ContentResolver, id: String): Cursor? {
        return contentResolver.query(Telephony.Threads.CONTENT_URI, null, "_id = $id AND archived=0", null, null)
    }

    fun isThreadActive(contentResolver: ContentResolver, id: String): Boolean {
        val c = getCursorForId(contentResolver, id)
        c!!.moveToFirst()

        return c.getString(c.getColumnIndex(Telephony.Threads.ARCHIVED)) == "-1"
    }
}
