package com.bradcypert.textico.views.fragments

import android.app.ActivityOptions
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class MessageList: Fragment() {

    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.message_list) lateinit var messageList: RecyclerView
    private var adapter: MessageListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_conversation_list, container, false)
        ButterKnife.bind(this, view)
        setupMessageList()
        setupFab()
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

    private fun refreshMessageList(messages: RealmResults<Message>) {
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

    companion object {
        private const val REQUEST_CODE_ASK_PERMISSIONS = 123
    }


}