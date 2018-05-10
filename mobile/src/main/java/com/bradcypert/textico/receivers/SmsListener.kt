package com.bradcypert.textico.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.provider.MediaStore
import android.provider.Telephony
import android.telephony.SmsMessage

import com.bradcypert.textico.ConversationDetails
import com.bradcypert.textico.R
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.services.ContactsService

class SmsListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {

            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val messageBody = smsMessage.messageBody
                val phoneNumber = smsMessage.displayOriginatingAddress
                val contact = ContactsService.getContactForNumber(context.contentResolver, phoneNumber)
                val style = Notification.MessagingStyle(context.resources.getString(R.string.you))
                var bitmap: Bitmap
                bitmap = try {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(contact.picUri))
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.empty_portait)
                }

                val dateTime = smsMessage.timestampMillis

                val mBuilder = Notification.Builder(context)
                        .setSmallIcon(R.mipmap.empty_portait)
                        .setLargeIcon(bitmap)
                        .setContentTitle(phoneNumber)
                        .setContentText(messageBody)
                        .setAutoCancel(true)

                val message1 = Notification.MessagingStyle.Message(smsMessage.messageBody,
                        smsMessage.timestampMillis,
                        if (contact.name != null) contact.name else phoneNumber)

                style.addMessage(message1)


                if (contact.picUri != null) {
                    val icon = Icon.createWithContentUri(Uri.parse(contact.picUri))
                    mBuilder.setSmallIcon(icon)
                }

                if (contact.name != null) {
                    mBuilder.setContentTitle(contact.name)
                }

                mBuilder.setStyle(style)

                val openIntent = Intent(context, ConversationDetails::class.java)
                openIntent.putExtra(ConversationDetails.KEY, phoneNumber)
                val pendingIntent = PendingIntent.getActivity(context, 12345, openIntent, PendingIntent.FLAG_ONE_SHOT)
                mBuilder.setContentIntent(pendingIntent)

                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(0, mBuilder.build())

                val cv = ContentValues()
                cv.put("address", phoneNumber)
                cv.put("body", messageBody)
                cv.put("read", 0)
                cv.put("date", dateTime)
                cv.put("date_sent", "1")

                context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, cv)

                val bcast = Intent()
                bcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                bcast.action = "com.bradcypert.updateData"
                context.sendBroadcast(bcast)
            }
        }
    }
}
