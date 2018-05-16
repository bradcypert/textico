package com.bradcypert.textico.migrations

import com.bradcypert.textico.repositories.SMSRepository
import io.reactivex.Observable
import io.realm.Realm

class DestroyDB: Command {
    override fun run(): Observable<Boolean> {
        return Observable.fromCallable {
            try {
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                realm.deleteAll()
                realm.commitTransaction()
                realm.close()
                true
            } catch(exception: Exception) {
                exception.printStackTrace()
                false
            }
        }
    }

}