package com.bradcypert.textico.migrations

import android.content.Context
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.models.SMS
import com.bradcypert.textico.repositories.SMSRepository
import io.reactivex.Observable
import io.realm.Realm

class SetupDB(val context: Context): Command {
    override fun run(): Observable<Boolean> {
        return Observable.fromCallable<Boolean> {
            try {
                val sms = SMSRepository.getConversations(context.contentResolver)
                val rSMS = sms.map(this::mapSMS)
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                realm.copyToRealm(rSMS)
                realm.commitTransaction()
                realm.close()
                 true
            } catch(exception: Exception) {
                exception.printStackTrace()
                false
            }
        }
    }

    fun mapSMS(m: SMS): Message {
        return Message(number = m.number,
                body = m.body,
                timestamp = m.timestamp,
                read = m.read,
                id = m.id,
                threadId = m.threadId,
                person = m.person,
                sentByMe = m.sentByMe,
                isArchived = false,
                messageType = 0)
    }

}