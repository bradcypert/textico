package com.bradcypert.textico.repositories

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

object MMSRepository {
    private const val SORT_DATE_ASC = "date asc"

    fun getAllMmsMessages(context: Context): ArrayList<MMS> {
        val mms = ArrayList<MMS>()

        Thread(Runnable {
            val cursor = context.contentResolver.query(Telephony.Mms.CONTENT_URI, null, null, null, SORT_DATE_ASC)
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

    private fun getPartOfMMS(context: Context, mmsID: Int): String {
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
            return ""
        } finally {
            cursor!!.close()
        }

    }

    private fun getAddressOfMMS(context: Context, mmsID: Int): String {
        var address = ""
        val uri = Uri.parse("content://mms/$mmsID/addr")
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor!!.moveToFirst()) {
                do {
                    address = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS))
                    if (address != "") {
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
        val threadId = cursor.getInt(cursor.getColumnIndex(Telephony.Mms.THREAD_ID))
        val address = getAddressOfMMS(context, id)
        val part = getPartOfMMS(context, id)
        val message = getMmsText(context, id)
        val type = getMmsType(context, id)
        return MMS(body = message,
                    id = id,
                    threadId = threadId.toString(),
                    timestamp = Date(date*1000L),
                    type = type,
                    number = address,
                    read = cursor.getString(cursor.getColumnIndex(Telephony.Mms.READ)) == "1",
                    sentByMe = cursor.getString(cursor.getColumnIndex(Telephony.Mms.DATE_SENT)) == "0",
                    part = part,
                    person = null,
                    sender = null)
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
