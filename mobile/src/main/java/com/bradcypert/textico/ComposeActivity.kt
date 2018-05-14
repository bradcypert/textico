package com.bradcypert.textico

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.bradcypert.textico.services.ThemeService

/**
 * Created by bradc on 3/13/2017.
 */

class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, true))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

}
