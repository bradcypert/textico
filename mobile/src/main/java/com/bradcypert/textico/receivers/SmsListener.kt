package com.bradcypert.textico.receivers

import android.app.Notification
import android.app.NotificationChannel
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
import android.os.Build
import android.provider.MediaStore
import android.provider.Telephony
import android.telephony.SmsMessage
import com.bradcypert.textico.ConversationDetails
import com.bradcypert.textico.R
import com.bradcypert.textico.services.ContactsService
import com.bradcypert.textico.services.MessageService

class SmsListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {

            for (smsMessage: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val messageBody = smsMessage.messageBody
                val phoneNumber = smsMessage.displayOriginatingAddress
                val contact = ContactsService.getContactForNumber(context.contentResolver, phoneNumber)
                val style = Notification.MessagingStyle(context.resources.getString(R.string.you))
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                var bitmap: Bitmap
                bitmap = try {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(contact.picUri))
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.empty_portait)
                }

                val dateTime = smsMessage.timestampMillis

                val mBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("textico_notification", "Incoming Text Messages", NotificationManager.IMPORTANCE_DEFAULT)
                    nm.createNotificationChannel(channel)
                    Notification.Builder(context, "textico_notification")
                } else {
                    Notification.Builder(context)
                }

                mBuilder.setSmallIcon(R.mipmap.empty_portait)
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
                openIntent.putExtra(ConversationDetails.CONTACT_NAME, contact.name)
                openIntent.putExtra(ConversationDetails.CONTACT_PICTURE, contact.picUri)
                openIntent.putExtra(ConversationDetails.THREAD_ID, MessageService.getConversationDetails(context.contentResolver, phoneNumber)[0].threadId)
                val pendingIntent = PendingIntent.getActivity(context, 12345, openIntent, PendingIntent.FLAG_ONE_SHOT)
                mBuilder.setContentIntent(pendingIntent)

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
