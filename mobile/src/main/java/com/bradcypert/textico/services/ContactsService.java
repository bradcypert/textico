package com.bradcypert.textico.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.bradcypert.textico.models.Contact;

public class ContactsService {
    private static Cursor getAllCursor(ContentResolver contentResolver, String id) {
        return contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null ,
                null, null);
    }

    private static Cursor getCursorForPhoneNumber(ContentResolver contentResolver, String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        return contentResolver.query(uri, null, null, null, null);
    }

    private static Contact buildContactFromCursor(Cursor c) {
        if (c.getCount() != 0) {
            return new Contact.ContactBuilder()
                    .setName(c.getString(c.getColumnIndex("DISPLAY_NAME")))
                    .setPicUri(c.getString(c.getColumnIndex("PHOTO_URI")))
                    .setStarred(c.getString(c.getColumnIndex("STARRED")).equals("1"))
                    .build();
        } else {
            return new Contact.ContactBuilder()
                    .setName(null)
                    .setPicUri(null)
                    .setStarred(false)
                    .build();
        }
    }

    public static Contact getContactForId(ContentResolver contentResolver, String id) {
        Cursor c = getAllCursor(contentResolver, id);
        c.moveToFirst();

        return buildContactFromCursor(c);
    }

    public static Contact getContactForNumber(ContentResolver contentResolver, String number) {
        Cursor c = getCursorForPhoneNumber(contentResolver, number);
        c.moveToFirst();

        return buildContactFromCursor(c);
    }

    public static String getUsersPhoneNumber(Context context) {
        TelephonyManager manager =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getLine1Number();
    }
}
