package com.bradcypert.textico.migrations

import io.reactivex.Observable

class DestroyDB: Command {
    override fun run(): Observable<Boolean> {
        return Observable.fromCallable {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}