package com.bradcypert.textico.services

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Telephony

import com.bradcypert.textico.models.MMS

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Date

object MMSService {
    private val SORT_DATE_ASC = "date asc"

    private fun getConversationMMSDetailsCursor(contentResolver: ContentResolver, threadId: String): Cursor? {
        return contentResolver.query(Telephony.Mms.CONTENT_URI, null, "thread_id=$threadId", null, SORT_DATE_ASC)
    }

    fun getAllMmsMessages(context: Context, threadId: String): ArrayList<MMS> {
        val mms = ArrayList<MMS>()

        Thread(Runnable {
            val cursor = MMSService.getConversationMMSDetailsCursor(context.contentResolver, threadId)
            try {
                cursor!!.moveToFirst()

                while (!cursor.isAfterLast) {
                    try {
                        val message = buildMMSFromCursor(cursor, context)
                        mms.add(message)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    cursor.moveToNext()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }).run()

        return mms
    }

    private fun getPartOfMMS(context: Context, mmsID: Int): String? {
        val selectionPart = "mid=$mmsID"
        val uri = Uri.parse("content://mms/part")
        val cursor = context.contentResolver.query(uri, null,
                selectionPart, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    val path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._DATA))
                    if (path != null) {
                        cursor.close()
                        return path
                    }
                } while (cursor.moveToNext())
            }
            return null
        } finally {
            cursor!!.close()
        }

    }

    private fun getAddressOfMMS(context: Context, mmsID: Int): String? {
        var address: String? = null
        val uri = Uri.parse("content://mms/$mmsID/addr")
        val cursor = context.contentResolver.query(uri, arrayOf("address"), null, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    address = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS))
                    if (address != null) {
                        break
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor!!.close()
        }

        return address

    }


    private fun getMmsText(context: Context, id: Int): String? {
        val selectionPart = "mid=$id"
        val uri = Uri.parse("content://mms/part")
        var path: String? = null
        val cursor = context.contentResolver.query(uri, null,
                selectionPart, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    val type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
                    if ("text/plain" == type) {
                        path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT))
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor!!.close()
        }
        return path
    }

    private fun getMmsType(context: Context, id: Int): String? {
        val selectionPart = "mid=$id"
        val uri = Uri.parse("content://mms/part")
        val cursor = context.contentResolver.query(uri, null,
                selectionPart, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    val type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
                    if (type != "application/smil") {
                        return type
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor!!.close()
        }
        return null
    }

    @Throws(Exception::class)
    private fun buildMMSFromCursor(cursor: Cursor, context: Context): MMS {
        var date = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE))
        if (date == 0L) {
            date = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE_SENT))
        }

        val id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID))
        //        String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
        val threadId = cursor.getInt(cursor.getColumnIndex(Telephony.Mms.THREAD_ID))
        val address = getAddressOfMMS(context, id)
        val part = getPartOfMMS(context, id)
        val message = getMmsText(context, id)
        val type = getMmsType(context, id)
        return MMS.MMSBuilder()
                .setBody(message)
                .setId(id)
                .setThreadId(threadId.toString())
                .setTimestamp(Date(date * 1000L))
                .setType(type)
                .setNumber(address)
                .setRead(cursor.getString(cursor.getColumnIndex(Telephony.Mms.READ)))
                .setSentByMe(cursor.getString(cursor.getColumnIndex(Telephony.Mms.DATE_SENT)) == "0")
                .setPart(part)
                //                .setImage(getMmsImage(context, id))
                .build()
    }

    fun getMmsImage(context: Context, id: Int): Bitmap? {
        val selectionPart = "mid=$id"
        val uri = Uri.parse("content://mms/part")
        var bitmap: Bitmap? = null
        val cursor = context.contentResolver.query(uri, null,
                selectionPart, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    val partId = cursor.getString(cursor.getColumnIndex("_id"))
                    val type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE))
                    if ("image/jpeg" == type || "image/bmp" == type ||
                            "image/gif" == type || "image/jpg" == type ||
                            "image/png" == type) {
                        bitmap = getMmsImageDecoder(context, partId)
                        break
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor!!.close()
        }
        return bitmap
    }

    private fun getMmsImageDecoder(context: Context, _id: String): Bitmap? {
        val partURI = Uri.parse("content://mms/part/$_id")
        var `is`: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            `is` = context.contentResolver.openInputStream(partURI)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                }

            }
        }
        return bitmap
    }

}
