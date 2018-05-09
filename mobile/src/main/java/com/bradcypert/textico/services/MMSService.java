package com.bradcypert.textico.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Telephony;

import com.bradcypert.textico.models.MMS;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class MMSService {
    private static final String SORT_DATE_ASC = "date asc";

    private static Cursor getConversationMMSDetailsCursor(ContentResolver contentResolver, String threadId) {
        return contentResolver.query(Telephony.Mms.CONTENT_URI, null, "thread_id="+threadId, null, SORT_DATE_ASC);
    }

    public static ArrayList<MMS> getAllMmsMessages(final Context context, final String threadId) {
        final ArrayList<MMS> mms = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = MMSService.getConversationMMSDetailsCursor(context.getContentResolver(), threadId);
                try {
                    cursor.moveToFirst();

                    while(!cursor.isAfterLast()) {
                        try {
                            MMS message = buildMMSFromCursor(cursor, context);
                            mms.add(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        cursor.moveToNext();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }).run();

        return mms;
    }

    private static String getPartOfMMS(Context context, int mmsID) {
        String selectionPart = "mid=" + mmsID;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
                    if (path != null) {
                        return path;
                    }
                } while (cursor.moveToNext());
            }
            return null;
        } finally {
            cursor.close();
        }

    }

    private static String getAddressOfMMS(Context context, int mmsID) {
        String address = null;
        Uri uri = Uri.parse("content://mms/"+mmsID+"/addr");
        Cursor cursor = context.getContentResolver().query(uri, new String[] { "address" },
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    address = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS));
                    if (address != null) {
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return address;

    }


    private static String getMmsText(Context context, int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if ("text/plain".equals(type)) {
                        path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return path;
    }

    private static String getMmsType(Context context, int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if (!type.equals("application/smil")) {
                        return type;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private static MMS buildMMSFromCursor(Cursor cursor, Context context) throws Exception {
        long date = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE));
        if (date == 0) {
            date = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE_SENT));
        }

        int id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID));
//        String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
        int threadId = cursor.getInt(cursor.getColumnIndex(Telephony.Mms.THREAD_ID));
        String address = getAddressOfMMS(context, id);
        String part = getPartOfMMS(context, id);
        String message = getMmsText(context, id);
        String type = getMmsType(context, id);
        return new MMS.MMSBuilder()
                .setBody(message)
                .setId(id)
                .setThreadId(String.valueOf(threadId))
                .setTimestamp(new Date(date * 1000L))
                .setType(type)
                .setNumber(address)
                .setRead(cursor.getString(cursor.getColumnIndex(Telephony.Mms.READ)))
                .setSentByMe(cursor.getString(cursor.getColumnIndex(Telephony.Mms.DATE_SENT)).equals("0"))
                .setPart(part)
//                .setImage(getMmsImage(context, id))
                .build();
    }

    public static Bitmap getMmsImage(Context context, int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Bitmap bitmap = null;
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String partId = cursor.getString(cursor.getColumnIndex("_id"));
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                            "image/gif".equals(type) || "image/jpg".equals(type) ||
                            "image/png".equals(type)) {
                        bitmap = getMmsImageDecoder(context, partId);
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return bitmap;
    }

    private static Bitmap getMmsImageDecoder(Context context, String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bitmap;
    }

}
