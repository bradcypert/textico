package com.bradcypert.textico.views

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.transition.Slide
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import com.bradcypert.textico.R
import com.bradcypert.textico.adapters.MessageListAdapter
import com.bradcypert.textico.adapters.SearchAndRemove
import com.bradcypert.textico.itemtouch.callbacks.SwipeToDeleteCallback
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.models.SMS
import com.bradcypert.textico.recycler.item.decorators.VerticalSpaceItemDecorator
import com.bradcypert.textico.repositories.SMSRepository
import com.bradcypert.textico.services.ThemeService
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*
import kotlin.collections.ArrayList

class MessageList : AppCompatActivity() {
    private var timer: Timer? = null
    private var messages: ArrayList<Message> = ArrayList()
    private var adapter: MessageListAdapter? = null
    private var filter: Filter? = null
    private var initialTheme: String? = null
    private var isListenerRegistered = false
    private val listener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshMessages()
        }
    }

    fun refreshMessages() {
        val realm = Realm.getDefaultInstance()
        messages.clear()
        if (filter == Filter.all) {
            messages.addAll(realm.where<Message>().sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
        } else {
            messages.addAll(realm.where<Message>().equalTo("read",false).sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
        }

        refreshMessageList(messages)
    }

    private lateinit var fab: FloatingActionButton

    private enum class Filter {
        all, unread
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, false))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)
        this.initialTheme = ThemeService.getThemeName(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        window.exitTransition = Slide(Gravity.START)
        window.enterTransition = Slide(Gravity.START)
        window.sharedElementEnterTransition = Slide(Gravity.START)
        window.sharedElementExitTransition = Slide(Gravity.START)

        setupMessageFilters()
        setupMessageList()
        setupFab()
        setupWatcher()
    }

    private fun setupFab() {
        val a = this
        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(baseContext, ComposeActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(a).toBundle())
        }
    }

    private fun setupWatcher() {
        val filter = IntentFilter("com.bradcypert.updateData")
        registerReceiver(listener, filter)
        isListenerRegistered = true
    }

    private fun setupMessageFilters() {
        val filterSpinner = findViewById<Spinner>(R.id.filter_spinner)
        val filters = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.filter_option)

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filters
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filterValue = if (position > 0) parent.getItemAtPosition(position) as String else ""
                val messages: ArrayList<Message> = ArrayList()

                val realm = Realm.getDefaultInstance()

                if (filterValue == "Unread") {
                    messages.addAll(realm.where<Message>().equalTo("read",false).sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
                    filter = Filter.unread
                } else {
                    messages.addAll(realm.where<Message>().sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
                    filter = Filter.all
                }

                refreshMessageList(messages)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun refreshMessageList(messages: ArrayList<Message>) {
        val messageList = findViewById<RecyclerView>(R.id.message_list)
        val a = this

        adapter = MessageListAdapter(baseContext, R.layout.message_list_adapter_view, messages, a)
        val itemDecorator = VerticalSpaceItemDecorator(5)
        val swipe = object : SwipeToDeleteCallback(messageList) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Row is swiped from recycler view
                // remove it from adapter
                val pos = viewHolder.adapterPosition
                if (pos > -1) {
                    try {
                        SMSRepository.deleteThreadById(contentResolver, messages[viewHolder.adapterPosition].threadId!!)
                        (messageList.adapter as SearchAndRemove).removeItem(pos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }

        runOnUiThread {
            messageList.layoutManager = LinearLayoutManager(baseContext)
            messageList.adapter = adapter
            messageList.addItemDecoration(itemDecorator)
            val itemTouchHelper = ItemTouchHelper(swipe)
            itemTouchHelper.attachToRecyclerView(messageList)
        }
    }

    private fun setupMessageList() {
        if (ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val realm = Realm.getDefaultInstance()
                messages.clear()
                messages.addAll(realm.where<Message>().equalTo("read",false).sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
                refreshMessageList(messages)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS),
                    REQUEST_CODE_ASK_PERMISSIONS
            )
            setupMessageList()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
    }

    public override fun onResume() {
        if (this.initialTheme != ThemeService.getThemeName(this)) {
            val intent = Intent(this, this.javaClass)
            startActivity(intent)
            finish()
        } else {
            setupWatcher()
        }
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        if (isListenerRegistered) {
            unregisterReceiver(listener)
            isListenerRegistered = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_message_list, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                adapter!!.search(query)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            val intent = Intent(baseContext, SettingsActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_CODE_ASK_PERMISSIONS = 123
    }
}
