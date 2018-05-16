package com.bradcypert.textico.migrations

import android.content.Context
import com.bradcypert.textico.models.MMS
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.models.SMS
import com.bradcypert.textico.repositories.MMSRepository
import com.bradcypert.textico.repositories.SMSRepository
import io.reactivex.Observable
import io.realm.Realm

class SetupDB(val context: Context): Command {
    override fun run(): Observable<Boolean> {
        return Observable.fromCallable<Boolean> {
            var succeeded = true
            try {
                val sms = SMSRepository.getSmsMessages(context.contentResolver)
                val rSMS = sms.map(this::mapSMS)
                rSMS.forEach { println(it) }
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                realm.insert(rSMS)
                realm.commitTransaction()
                realm.close()
                 succeeded = true
            } catch(exception: Exception) {
                exception.printStackTrace()
                succeeded = false
            }

             try {
                 val mms = MMSRepository.getAllMmsMessages(context)
                 val rMMS = mms.map(this::mapMMS)
                 rMMS.forEach { println(it) }
                 val realm = Realm.getDefaultInstance()
                 realm.beginTransaction()
                 realm.insert(rMMS)
                 realm.commitTransaction()
                 realm.close()
                 succeeded = true
                 succeeded
             } catch (exception: Exception) {
                 exception.printStackTrace()
                 succeeded = false
                 succeeded
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

    fun mapMMS(m: MMS): Message {
        return Message(number = m.number,
                body = m.body,
                timestamp = m.timestamp,
                read = m.read,
                id = m.id,
                threadId = m.threadId,
                person = m.person,
                sentByMe = m.sentByMe,
                isArchived = false,
                messageType = 1,
                part = m.part,
                type = m.type)
    }

}