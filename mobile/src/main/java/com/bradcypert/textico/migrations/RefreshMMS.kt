package com.bradcypert.textico.migrations

import android.content.Context
import com.bradcypert.textico.models.MMS
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.repositories.MMSRepository
import com.crashlytics.android.Crashlytics
import io.reactivex.Observable
import io.realm.Realm
import io.realm.kotlin.where

class RefreshMMS(val context: Context): Command {
    override fun run(): Observable<Boolean> {
        return Observable.fromCallable<Boolean> {
            var succeeded = true
            try {
                val mms = MMSRepository.getAllMmsMessages(context)
                val rMMS = mms.map(this::mapMMS)
                val realm = Realm.getDefaultInstance()
                val existing = realm.where<Message>().equalTo("messageType", 1 as Int).findAll()
                realm.beginTransaction()
                existing.deleteAllFromRealm()
                realm.insert(rMMS)
                realm.commitTransaction()
                realm.close()
                succeeded = true
                succeeded
            } catch (exception: Exception) {
                Crashlytics.log("Failed to refresh MMS")
                exception.printStackTrace()
                succeeded = false
                succeeded
            }
        }
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