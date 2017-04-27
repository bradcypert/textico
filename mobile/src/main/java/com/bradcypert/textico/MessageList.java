package com.bradcypert.textico;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.bradcypert.textico.adapters.MessageListAdapter;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.receivers.SmsListener;
import com.bradcypert.textico.services.SmsService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Manifest;

public class MessageList extends AppCompatActivity {

    private enum Filter {
        all, unread
    }
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Timer timer;
    private ArrayList<SMS> messages;
    private MessageListAdapter adapter;
    private Filter filter;
    private boolean isListenerRegistered = false;
    private BroadcastReceiver listener = new BroadcastReceiver(){
      @Override
      public void onReceive(Context context, Intent intent) {
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  messages.clear();
                  if (filter == Filter.all) {
                      messages.addAll(SmsService.getConversations(getContentResolver()));
                  } else {
                      messages.addAll(SmsService.getConversations(getContentResolver(), SmsService.MessageStatus.UNREAD));
                  }
                  adapter.notifyDataSetChanged();
              }
          });
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupMessageFilters();
        setupMessageList();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupWatcher() {
        IntentFilter filter = new IntentFilter("com.bradcypert.updateData");
        registerReceiver(listener, filter);
        isListenerRegistered = true;
    }

    private void setupMessageFilters() {
        Spinner filterSpinner = (Spinner) findViewById(R.id.filter_spinner);
        ArrayAdapter filters = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.filter_option);

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filters);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterValue = (String) parent.getItemAtPosition(position);
                ListView messageList = (ListView) findViewById(R.id.message_list);
                ArrayList<SMS> messages;

                if (filterValue.equals("Unread")) {
                    messages = SmsService.getConversations(getContentResolver(), SmsService.MessageStatus.UNREAD);
                    filter = Filter.unread;
                } else {
                    messages = SmsService.getConversations(getContentResolver());
                    filter = Filter.all;
                }

                adapter = new MessageListAdapter(getBaseContext(), messages);
                messageList.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupMessageList() {
        if(ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ListView messageList = (ListView) findViewById(R.id.message_list);
            messages = SmsService.getConversations(this.getContentResolver());
            adapter = new MessageListAdapter(this, messages);
            messageList.setAdapter(adapter);
            messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getBaseContext(), ConversationDetails.class);
                    intent.putExtra(ConversationDetails.KEY, messages.get(position).getNumber());
                    startActivity(intent);
                }
            });
        } else {
            ActivityCompat.requestPermissions(
                    MessageList.this,
                    new String[]{android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS
            );
            setupMessageList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
        timer = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupMessageList();
        setupWatcher();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (listener != null && isListenerRegistered) {
            unregisterReceiver(listener);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
