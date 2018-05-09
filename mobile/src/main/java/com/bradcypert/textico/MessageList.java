package com.bradcypert.textico;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import com.bradcypert.textico.adapters.MessageListAdapter;
import com.bradcypert.textico.adapters.SearchAndRemove;
import com.bradcypert.textico.itemtouch.callbacks.SwipeToDeleteCallback;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.recycler.item.decorators.VerticalSpaceItemDecorator;
import com.bradcypert.textico.services.MessageService;

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
          messages.clear();
          if (filter == Filter.all) {
              messages.addAll(MessageService.getConversations(getContentResolver()));
          } else {
              messages.addAll(MessageService.getConversations(getContentResolver(), MessageService.MessageStatus.UNREAD));
          }
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  adapter.notifyDataSetChanged();
              }
          });
      }
    };

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setExitTransition(new Slide(Gravity.START));
        getWindow().setEnterTransition(new Slide(Gravity.START));
        getWindow().setSharedElementEnterTransition(new Slide(Gravity.START));
        getWindow().setSharedElementExitTransition(new Slide(Gravity.START));

        setupMessageFilters();
        setupMessageList();
        setupFab();
    }

    private void setupFab() {
        final Activity a = this;
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ComposeActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(a).toBundle());
            }
        });
    }

    private void setupWatcher() {
        IntentFilter filter = new IntentFilter("com.bradcypert.updateData");
        registerReceiver(listener, filter);
        isListenerRegistered = true;
    }

    private void setupMessageFilters() {
        Spinner filterSpinner = findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> filters = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.filter_option);

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filters);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterValue = (String) parent.getItemAtPosition(position);
                ArrayList<SMS> messages;

                if (filterValue.equals("Unread")) {
                    messages = MessageService.getConversations(getContentResolver(), MessageService.MessageStatus.UNREAD);
                    filter = Filter.unread;
                } else {
                    messages = MessageService.getConversations(getContentResolver());
                    filter = Filter.all;
                }

                refreshMessageList(messages);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshMessageList(final ArrayList<SMS> messages) {
        final RecyclerView messageList = findViewById(R.id.message_list);
        final Activity a = this;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                adapter = new MessageListAdapter(getBaseContext(), R.layout.message_list_adapter_view, messages, a);
                final VerticalSpaceItemDecorator itemDecorator = new VerticalSpaceItemDecorator(5);
                final SwipeToDeleteCallback swipe = new SwipeToDeleteCallback(messageList) {
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // Row is swiped from recycler view
                        // remove it from adapter
                        int pos = viewHolder.getAdapterPosition();
                        if (pos > -1) {
                            try {
                                MessageService.deleteThreadById(getContentResolver(), messages.get(viewHolder.getAdapterPosition()).getThreadId());
                                ((SearchAndRemove) messageList.getAdapter()).removeItem(pos);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        messageList.setAdapter(adapter);
                        messageList.addItemDecoration(itemDecorator);
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipe);
                        itemTouchHelper.attachToRecyclerView(messageList);
                    }
                });
            }
        });
    }

    private void setupMessageList() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            messages = MessageService.getConversations(this.getContentResolver());
            refreshMessageList(messages);
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
                adapter.search(query);
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
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
