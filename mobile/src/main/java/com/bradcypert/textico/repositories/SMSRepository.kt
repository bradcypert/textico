package com.bradcypert.textico.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony

import com.bradcypert.textico.models.SMS

import java.util.ArrayList
import java.util.Date

object SMSRepository {
    private const val UNREAD = "read = 0"
    private const val SORT_DATE_DESC = "${Telephony.TextBasedSmsColumns.DATE} desc"
    private const val SORT_DATE_ASC = "${Telephony.TextBasedSmsColumns.DATE} asc"

    enum class MessageStatus {
        UNREAD
    }

    private fun getAllCursor(contentResolver: ContentResolver): Cursor {
        val uri = Telephony.Sms.CONTENT_URI
        return contentResolver.query(uri, null, null, null, SORT_DATE_DESC)
    }

    private fun getConversationDetailsCursor(contentResolver: ContentResolver, number: String): Cursor {
        return contentResolver.query(Telephony.Sms.CONTENT_URI, null, "address=$number", null, SORT_DATE_ASC)
    }

    fun getSmsMessages(contentResolver: ContentResolver): ArrayList<SMS> {
        val messages = ArrayList<SMS>()
        val inboxCursor = getAllCursor(contentResolver)
        inboxCursor.moveToFirst()

        while (!inboxCursor.isAfterLast) {
            try {
                val message = buildSmsFromCursor(inboxCursor)
                messages.add(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            inboxCursor.moveToNext()
        }

        return messages
    }

    fun getConversationDetails(contentResolver: ContentResolver, number: String): ArrayList<SMS> {
        val messages = ArrayList<SMS>()
        val inboxCursor = getConversationDetailsCursor(contentResolver, number)
        inboxCursor.moveToFirst()

        while (!inboxCursor.isAfterLast) {
            try {
                messages.add(buildSmsFromCursor(inboxCursor))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            inboxCursor.moveToNext()
        }

        return messages
    }

    fun flagMessageAsRead(contentResolver: ContentResolver, number: String) {
        val values = ContentValues()
        values.put("read", "1")
        val uri = Uri.parse("content://sms/inbox")
        contentResolver.update(uri, values, "address=$number", null)
    }

    @Throws(Exception::class)
    private fun buildSmsFromCursor(c: Cursor): SMS {
        var date = java.lang.Long.parseLong(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.DATE)))
        if (date == 0L) {
            date = java.lang.Long.parseLong(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.DATE_SENT)))
        }

        return SMS(number = c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS)),
                body = c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.BODY)),
                timestamp = Date(date),
                id = c.getInt(c.getColumnIndex(Telephony.BaseMmsColumns._ID)),
                threadId = c.getString(c.getColumnIndex(Telephony.BaseMmsColumns.THREAD_ID)),
                read = c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.READ)) == "1",
                sentByMe = c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.DATE_SENT)) == "0",
                sender = c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.PERSON)))
    }

    fun deleteThreadById(contentResolver: ContentResolver, threadId: String) {
        contentResolver.delete(Uri.parse(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI.toString() + "/" + threadId), null, null)
    }
}
