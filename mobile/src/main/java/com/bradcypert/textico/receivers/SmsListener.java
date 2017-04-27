package com.bradcypert.textico.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.bradcypert.textico.ConversationDetails;
import com.bradcypert.textico.R;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.services.ContactsService;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {

            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();
                String phoneNumber = smsMessage.getDisplayOriginatingAddress();
                Contact contact = ContactsService.getContactForNumber(context.getContentResolver(), phoneNumber);
                long dateTime = smsMessage.getTimestampMillis();

                Notification.Builder mBuilder = new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.empty_portait)
                        .setContentTitle(phoneNumber)
                        .setContentText(messageBody);

                if (contact.getPicUri() != null) {
                    Icon icon = Icon.createWithContentUri(Uri.parse(contact.getPicUri()));
                    mBuilder.setSmallIcon(icon);
                }

                if (contact.getName() != null) {
                    mBuilder.setContentTitle(contact.getName());
                }

                Intent openIntent = new Intent(context, ConversationDetails.class);
                openIntent.putExtra(ConversationDetails.KEY, phoneNumber);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 12345, openIntent, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setContentIntent(pendingIntent);

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(0, mBuilder.build());

                ContentValues cv = new ContentValues();
                cv.put("address", phoneNumber);
                cv.put("body", messageBody);
                cv.put("read", 0);
                cv.put("date", dateTime);

                context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, cv);

                Intent bcast = new Intent();
                bcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                bcast.setAction("com.bradcypert.updateData");
                context.sendBroadcast(bcast);
            }


        }
    }
}
