package com.bradcypert.textico.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.Gravity
import com.bradcypert.textico.R
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.services.ThemeService
import com.bradcypert.textico.views.fragments.TourContainer
import io.realm.Realm
import io.realm.kotlin.where

class MainView : AppCompatActivity(), TourContainer.OnTourCompleteListener {
    override fun onTourComplete() {
        val fm = fragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.content_message_list_root, com.bradcypert.textico.views.fragments.MessageList())
        ft.commit()
    }

    private var initialTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, false))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view_activity)
        this.initialTheme = ThemeService.getThemeName(this)

        window.exitTransition = Slide(Gravity.START)
        window.enterTransition = Slide(Gravity.START)
        window.sharedElementEnterTransition = Slide(Gravity.START)
        window.sharedElementExitTransition = Slide(Gravity.START)


        val realm = Realm.getDefaultInstance()
        if (realm.where<Message>().count() < 1) {
            val fm = fragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.content_message_list_root, com.bradcypert.textico.views.fragments.TourContainer())
            ft.commit()
        } else {
            val fm = fragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.content_message_list_root, com.bradcypert.textico.views.fragments.MessageList())
            ft.commit()
        }

    }

    public override fun onResume() {
        if (this.initialTheme != ThemeService.getThemeName(this)) {
            val intent = Intent(this, this.javaClass)
            startActivity(intent)
            finish()
        }
        super.onResume()
    }
}
