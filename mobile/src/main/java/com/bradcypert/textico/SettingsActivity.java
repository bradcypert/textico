package com.bradcypert.textico;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.MessageService;
import com.bradcypert.textico.services.ThemeService;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeService.getSelectedTheme(this, true));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupMessageFilters();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void setupMessageFilters() {
        final Activity a = this;
        final String currentName = ThemeService.getThemeName(this);
        final Spinner filterSpinner = findViewById(R.id.simple_spinner_dropdown_item_settings);
        ArrayAdapter<CharSequence> filters = ArrayAdapter.createFromResource(this,
                R.array.theme_list, R.layout.settings_spinner_item);

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filters);
        filterSpinner.setSelection(filters.getPosition(ThemeService.getThemeName(a)));
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterValue = (String) parent.getItemAtPosition(position);
                if (!currentName.equals(filterValue)) {
                    ThemeService.setThemeName(a, filterValue);
                    switch (filterValue) {
                        case "Modern":
                            ThemeService.setTheme(a, R.style.AppTheme);
                            ThemeService.setThemeNoActionBar(a, R.style.AppTheme_NoActionBar);
                            setTheme(R.style.AppTheme);
                            break;
                        case "Retro":
                            ThemeService.setTheme(a, R.style.Retro);
                            ThemeService.setThemeNoActionBar(a, R.style.Retro_NoActionBar);
                            setTheme(R.style.Retro);
                            break;
                        case "Pizza":
                            ThemeService.setTheme(a, R.style.Pizza);
                            ThemeService.setThemeNoActionBar(a, R.style.Pizza_NoActionBar);
                            setTheme(R.style.Pizza);
                            break;
                        case "Ocean":
                            ThemeService.setTheme(a, R.style.Ocean);
                            ThemeService.setThemeNoActionBar(a, R.style.Ocean_NoActionBar);
                            setTheme(R.style.Ocean);
                            break;
                        case "Sky":
                            ThemeService.setTheme(a, R.style.Sky);
                            ThemeService.setThemeNoActionBar(a, R.style.Sky_NoActionBar);
                            setTheme(R.style.Sky);
                            break;
                        case "Olive":
                            ThemeService.setTheme(a, R.style.Olive);
                            ThemeService.setThemeNoActionBar(a, R.style.Olive_NoActionBar);
                            setTheme(R.style.Olive);
                            break;
                    }
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
