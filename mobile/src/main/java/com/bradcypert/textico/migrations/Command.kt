package com.bradcypert.textico.migrations

import io.reactivex.Observable

interface Command {
    fun run(): Observable<Boolean>
}