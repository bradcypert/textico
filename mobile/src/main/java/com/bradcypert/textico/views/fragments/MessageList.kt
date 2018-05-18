package com.bradcypert.textico.views.fragments

import android.app.ActivityOptions
import android.app.Fragment
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import butterknife.BindView
import butterknife.ButterKnife
import com.bradcypert.textico.R
import com.bradcypert.textico.adapters.MessageListAdapter
import com.bradcypert.textico.adapters.SearchAndRemove
import com.bradcypert.textico.itemtouch.callbacks.SwipeToDeleteCallback
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.recycler.item.decorators.VerticalSpaceItemDecorator
import com.bradcypert.textico.repositories.SMSRepository
import com.bradcypert.textico.views.ComposeActivity
import com.bradcypert.textico.views.MainView
import com.bradcypert.textico.views.SettingsActivity
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class MessageList: Fragment() {

    private enum class Filter {
        all, unread
    }

    private var filter: Filter = Filter.all

    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.message_list) lateinit var messageList: RecyclerView
    @BindView(R.id.filter_spinner) lateinit var filterSpinner: Spinner
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    private var adapter: MessageListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_conversation_list, container, false)
        ButterKnife.bind(this, view)
        setupMessageList()
        setupFab()
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setupMessageFilters()
        setHasOptionsMenu(true)
        return view
    }

    private fun setupFab() {
        fab.setOnClickListener {
            val intent = Intent(activity, ComposeActivity::class.java)
            activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
        }
    }

    private fun setupMessageList() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val realm = Realm.getDefaultInstance()
                val messages = realm.where<Message>().sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll()
                refreshMessageList(messages)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS),
                    com.bradcypert.textico.views.fragments.MessageList.REQUEST_CODE_ASK_PERMISSIONS
            )
            setupMessageList()
        }
    }

    private fun refreshMessageList(messages: OrderedRealmCollection<Message>) {
        adapter = MessageListAdapter(activity, R.layout.message_list_adapter_view, messages, activity)
        val itemDecorator = VerticalSpaceItemDecorator(5)
        val swipe = object : SwipeToDeleteCallback(messageList) {
            //TODO: Better way to do this?
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Row is swiped from recycler view
                // remove it from adapter
                val pos = viewHolder.adapterPosition
                if (pos > -1) {
                    try {
                        //TODO: Persist changes to realm as well
                        SMSRepository.deleteThreadById(activity.contentResolver, messages[viewHolder.adapterPosition]!!.threadId!!)
                        (messageList.adapter as SearchAndRemove).removeItem(pos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }

        messageList.layoutManager = LinearLayoutManager(activity)
        messageList.adapter = adapter
        messageList.addItemDecoration(itemDecorator)
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(messageList)
    }

    private fun setupMessageFilters() {
        val filters = ArrayAdapter.createFromResource(activity,
                R.array.filter_options, R.layout.filter_option)

        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = filters
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filterValue = if (position > 0) parent.getItemAtPosition(position) as String else ""
                val messages: RealmResults<Message>

                val realm = Realm.getDefaultInstance()

                if (filterValue == "Unread") {
                    messages = (realm.where<Message>().equalTo("read",false).sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
                    filter = Filter.unread
                } else {
                    messages = (realm.where<Message>().sort("timestamp", Sort.DESCENDING).distinct("threadId").findAll())
                    filter = Filter.all
                }

                refreshMessageList(messages)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_message_list, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
//                val realm = Realm.getDefaultInstance()
//                refreshMessageList()
                // TODO: Get filtering working with realm.
                return true
            }
        })

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_CODE_ASK_PERMISSIONS = 123
    }


}