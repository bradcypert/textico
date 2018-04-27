package com.bradcypert.textico.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.TelephonyManager;

import com.bradcypert.textico.models.Contact;

public class ThreadService {

    private static Cursor getCursorForId(ContentResolver contentResolver, String id) {
        return contentResolver.query(Telephony.Threads.CONTENT_URI, null, "_id = " + id, null, null);
    }

    public static boolean isThreadActive(ContentResolver contentResolver, String id) {
        Cursor c = getCursorForId(contentResolver, id);
        c.moveToFirst();

        return c.getString(c.getColumnIndex(Telephony.Threads.ARCHIVED)).equals("-1");
    }
}
