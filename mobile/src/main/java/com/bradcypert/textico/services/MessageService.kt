package com.bradcypert.textico.services

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.android.internal.telephony.SmsConstants

import com.bradcypert.textico.models.SMS

import java.util.ArrayList
import java.util.Date

object MessageService {
    private const val UNREAD = "read = 0"
    private const val SORT_DATE_DESC = "${Telephony.TextBasedSmsColumns.DATE} desc"
    private const val SORT_DATE_ASC = "${Telephony.TextBasedSmsColumns.DATE} asc"

    enum class MessageStatus {
        UNREAD
    }

    private fun getAllCursor(contentResolver: ContentResolver): Cursor {
        val uri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI.buildUpon().appendQueryParameter("simple", "false").build()
        val projection = arrayOf("_id",
                                 Telephony.TextBasedSmsColumns.ADDRESS,
                                 Telephony.TextBasedSmsColumns.BODY,
                                 Telephony.TextBasedSmsColumns.DATE,
                                 Telephony.TextBasedSmsColumns.DATE_SENT,
                                 Telephony.TextBasedSmsColumns.THREAD_ID,
                                 Telephony.TextBasedSmsColumns.READ,
                                 Telephony.TextBasedSmsColumns.PERSON)
        return contentResolver.query(uri, projection, null, null, SORT_DATE_DESC)
    }

    private fun getUnreadCursor(contentResolver: ContentResolver): Cursor {
        val uri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI.buildUpon().appendQueryParameter("simple", "false").build()
        val projection = arrayOf("_id",
                Telephony.TextBasedSmsColumns.ADDRESS,
                Telephony.TextBasedSmsColumns.BODY,
                Telephony.TextBasedSmsColumns.DATE,
                Telephony.TextBasedSmsColumns.DATE_SENT,
                Telephony.TextBasedSmsColumns.THREAD_ID,
                Telephony.TextBasedSmsColumns.READ,
                Telephony.TextBasedSmsColumns.PERSON)
        return contentResolver.query(uri, projection, UNREAD, null, SORT_DATE_DESC)
    }

    private fun getConversationDetailsCursor(contentResolver: ContentResolver, number: String): Cursor {
        return contentResolver.query(Telephony.Sms.CONTENT_URI, null, "address=$number", null, SORT_DATE_ASC)
    }

    fun getConversations(contentResolver: ContentResolver): ArrayList<SMS> {
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

    fun getConversations(contentResolver: ContentResolver, filter: MessageStatus): ArrayList<SMS> {
        val messages = ArrayList<SMS>()
        val inboxCursor: Cursor = if (filter == MessageStatus.UNREAD) getUnreadCursor(contentResolver) else getAllCursor(contentResolver)

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

        return SMS.SMSBuilder()
                .setNumber(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS)))
                .setBody(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.BODY)))
                .setTimestamp(Date(date))
                .setId(c.getInt(c.getColumnIndex(Telephony.BaseMmsColumns._ID)))
                .setThreadId(c.getString(c.getColumnIndex(Telephony.BaseMmsColumns.THREAD_ID)))
                .setRead(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.READ)))
                .setPerson(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.PERSON)))
                .setSentByMe(c.getString(c.getColumnIndex(Telephony.TextBasedSmsColumns.DATE_SENT)) == "0")
                .build()
    }

    fun deleteThreadById(contentResolver: ContentResolver, threadId: String) {
        contentResolver.delete(Uri.parse(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI.toString() + "/" + threadId), null, null)

    }
}
