package com.bradcypert.textico
import android.app.Application
import butterknife.ButterKnife
import io.realm.Realm

class TexticoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this.applicationContext)
        ButterKnife.setDebug(BuildConfig.DEBUG)
    }
}