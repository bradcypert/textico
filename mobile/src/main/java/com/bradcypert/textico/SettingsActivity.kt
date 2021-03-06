package com.bradcypert.textico

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.bradcypert.textico.services.ThemeService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, true))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupMessageFilters()
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

    private fun setupMessageFilters() {
        val a = this
        val currentName = ThemeService.getThemeName(this)
        val filterSpinner = findViewById<Spinner>(R.id.simple_spinner_dropdown_item_settings)
        val filters = ArrayAdapter.createFromResource(this,
                R.array.theme_list, R.layout.settings_spinner_item)

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filters
        filterSpinner.setSelection(filters.getPosition(ThemeService.getThemeName(a)))
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filterValue = parent.getItemAtPosition(position) as String
                if (currentName != filterValue) {
                    ThemeService.setThemeName(a, filterValue)
                    when (filterValue) {
                        "Modern" -> {
                            ThemeService.setTheme(a, R.style.AppTheme)
                            ThemeService.setThemeNoActionBar(a, R.style.AppTheme_NoActionBar)
                            setTheme(R.style.AppTheme)
                        }
                        "Retro" -> {
                            ThemeService.setTheme(a, R.style.Retro)
                            ThemeService.setThemeNoActionBar(a, R.style.Retro_NoActionBar)
                            setTheme(R.style.Retro)
                        }
                        "Pizza" -> {
                            ThemeService.setTheme(a, R.style.Pizza)
                            ThemeService.setThemeNoActionBar(a, R.style.Pizza_NoActionBar)
                            setTheme(R.style.Pizza)
                        }
                        "Ocean" -> {
                            ThemeService.setTheme(a, R.style.Ocean)
                            ThemeService.setThemeNoActionBar(a, R.style.Ocean_NoActionBar)
                            setTheme(R.style.Ocean)
                        }
                        "Sky" -> {
                            ThemeService.setTheme(a, R.style.Sky)
                            ThemeService.setThemeNoActionBar(a, R.style.Sky_NoActionBar)
                            setTheme(R.style.Sky)
                        }
                        "Olive" -> {
                            ThemeService.setTheme(a, R.style.Olive)
                            ThemeService.setThemeNoActionBar(a, R.style.Olive_NoActionBar)
                            setTheme(R.style.Olive)
                        }
                        "Midnight" -> {
                            ThemeService.setTheme(a, R.style.Midnight)
                            ThemeService.setThemeNoActionBar(a, R.style.Midnight_NoActionBar)
                            setTheme(R.style.Midnight)
                        }
                    }
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
