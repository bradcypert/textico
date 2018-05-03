package com.bradcypert.textico;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.bradcypert.textico.adapters.MessageListAdapter;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.recycler.item.decorators.VerticalSpaceItemDecorator;
import com.bradcypert.textico.services.ContactsService;
import com.bradcypert.textico.services.SmsService;

import java.util.ArrayList;
import java.util.Timer;

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
                Intent intent = new Intent(getBaseContext(), ComposeActivity.class);
                startActivity(intent);
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
        ArrayAdapter<CharSequence> filters = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.filter_option);

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filters);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterValue = (String) parent.getItemAtPosition(position);
                RecyclerView messageList = (RecyclerView) findViewById(R.id.message_list);
                ArrayList<SMS> messages;

                if (filterValue.equals("Unread")) {
                    messages = SmsService.getConversations(getContentResolver(), SmsService.MessageStatus.UNREAD);
                    filter = Filter.unread;
                } else {
                    messages = SmsService.getConversations(getContentResolver());
                    filter = Filter.all;
                }

                adapter = new MessageListAdapter(getBaseContext(), R.layout.message_list_adapter_view, messages);
                messageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                messageList.setAdapter(adapter);
                int verticalSpacing = 5;
                VerticalSpaceItemDecorator itemDecorator = new VerticalSpaceItemDecorator(verticalSpacing);
                messageList.addItemDecoration(itemDecorator);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupMessageList() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            RecyclerView messageList = (RecyclerView) findViewById(R.id.message_list);
            messages = SmsService.getConversations(this.getContentResolver());
            adapter = new MessageListAdapter(this, R.layout.message_list_adapter_view, messages);
            messageList.setLayoutManager(new LinearLayoutManager(this));
            messageList.setAdapter(adapter);
            int verticalSpacing = 5;
            VerticalSpaceItemDecorator itemDecorator = new VerticalSpaceItemDecorator(verticalSpacing);
            messageList.addItemDecoration(itemDecorator);
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
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
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

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.equals("")) {
                    setupMessageList();
                } else {
                    System.out.println(query);
                    ArrayList<SMS> filtered = new ArrayList<>();
                    for (SMS message: messages) {
                        Contact contact = ContactsService.getContactForNumber(getContentResolver(), message.getNumber());
                        if (message.getNumber().contains(query)
                            || (contact.getName() != null && contact.getName().toLowerCase().contains(query.toLowerCase()))) {
                            filtered.add(message);
                        }
                    }
//                    adapter.clear();
//                    adapter.addAll(filtered);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });
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
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
