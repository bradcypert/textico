package com.bradcypert.textico.services;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.Telephony;

import com.bradcypert.textico.models.SMS;

import java.util.ArrayList;
import java.util.Date;

public class SmsService {
    private static final String NONEMPTY = "body IS NOT NULL AND body != ''";
    private static final String UNREAD = "read = 0";
    private static final String SORT_DATE_DESC = "date desc";
    private static final String SORT_DATE_ASC = "date asc";

    public enum MessageStatus {
        UNREAD
    }

    private static Cursor getAllCursor(ContentResolver contentResolver) {
        return contentResolver.query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, null, NONEMPTY,
                null, SORT_DATE_DESC);
    }

    private static Cursor getUnreadCursor(ContentResolver contentResolver) {
        return contentResolver.query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, null, NONEMPTY+" AND "+UNREAD,
                null, SORT_DATE_DESC);
    }

    private static Cursor getConversationDetailsCursor(ContentResolver contentResolver, String number) {
        return contentResolver.query(Telephony.Sms.CONTENT_URI, null, NONEMPTY + " AND address="+number, null, SORT_DATE_ASC);
    }

    public static ArrayList<SMS> getConversations(ContentResolver contentResolver) {
        ArrayList<SMS> messages = new ArrayList<>();
        Cursor inboxCursor = getAllCursor(contentResolver);
        inboxCursor.moveToFirst();

        while(!inboxCursor.isAfterLast()) {
            try {
                messages.add(buildSmsFromCursor(inboxCursor));
            } catch (Exception e) {}

            inboxCursor.moveToNext();
        }

        return messages;
    }

    public static ArrayList<SMS> getConversations(ContentResolver contentResolver, MessageStatus filter) {
        ArrayList<SMS> messages = new ArrayList<>();
        Cursor inboxCursor = null;
        if (filter == MessageStatus.UNREAD) {
             inboxCursor = getUnreadCursor(contentResolver);
        }

        inboxCursor.moveToFirst();

        while(!inboxCursor.isAfterLast()) {
            try {
                messages.add(buildSmsFromCursor(inboxCursor));
            } catch (Exception e) {}

            inboxCursor.moveToNext();
        }

        return messages;
    }

    public static ArrayList<SMS> getConversationDetails(ContentResolver contentResolver, String number) {
        ArrayList<SMS> messages = new ArrayList<>();
        Cursor inboxCursor = getConversationDetailsCursor(contentResolver, number);
        inboxCursor.moveToFirst();

        while(!inboxCursor.isAfterLast()) {
            try {
                messages.add(buildSmsFromCursor(inboxCursor));
            } catch (Exception e) {}

            inboxCursor.moveToNext();
        }

        return messages;
    }

    private static SMS buildSmsFromCursor(Cursor c) throws Exception {
        long date = Long.parseLong(c.getString(c.getColumnIndex("date")));
        if (date == 0) {
            date = Long.parseLong(c.getString(c.getColumnIndex("date_sent")));
        }

        return new SMS.SMSBuilder()
                .setNumber(c.getString(c.getColumnIndex("address")))
                .setBody(c.getString(c.getColumnIndex("body")))
                .setTimestamp(new Date(date))
                .setId(c.getInt(c.getColumnIndex(Telephony.BaseMmsColumns._ID)))
                .setThreadId(c.getInt(c.getColumnIndex("thread_id")))
                .setRead(c.getString(c.getColumnIndex("read")))
                .setPerson(c.getString(c.getColumnIndex("person")))
                .setSentByMe(c.getString(c.getColumnIndex("date_sent")).equals("0"))
                .build();
    }
}
