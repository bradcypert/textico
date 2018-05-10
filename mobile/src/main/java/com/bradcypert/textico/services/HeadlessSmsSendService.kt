package com.bradcypert.textico.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by bradc on 3/13/2017.
 */

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
