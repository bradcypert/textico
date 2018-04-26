package com.bradcypert.textico;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bradc on 3/13/2017.
 */

public class ComposeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_compose);
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
}
