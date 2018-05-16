package com.bradcypert.textico.adapters

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.bradcypert.textico.holders.ConversationHolder
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.services.ContactsService
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by bradc on 3/11/2017.
 */

class MessageListAdapter(private val context: Context, private val itemResource: Int, private val messages: OrderedRealmCollection<Message>, private val host: Activity) : RealmRecyclerViewAdapter<Message,ConversationHolder>(messages, true), SearchAndRemove {
    private val rootMessages: ArrayList<Message> = ArrayList(messages)
    private val contactsByNumber = HashMap<String?, Contact>()

    init {
        val rootMessages = this.rootMessages
        for (message in rootMessages) {
            contactsByNumber[message.number] = ContactsService.getContactForNumber(context.contentResolver, message.number!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationHolder {
        // 3. Inflate the view and return the new ViewHolder
        val view = LayoutInflater.from(parent.context)
                .inflate(this.itemResource, parent, false)
        return ConversationHolder(this.context, view)
    }

    // 4. Override the onBindViewHolder method
    override fun onBindViewHolder(holder: ConversationHolder, position: Int) {
        val message = this.messages[position]
        holder.bindMessage(message!!, this.host, contactsByNumber[message.number])
    }

    override fun getItemCount(): Int {
        return this.messages.size
    }

    override fun removeItem(position: Int) {
        messages.removeAt(position)
        this.notifyItemRemoved(position)
    }

    override fun search(query: String) {
        messages.clear()
        if (query == "") {
            messages.addAll(rootMessages)
        } else {
            val filtered = ArrayList<Message>()
            for (message in rootMessages) {
                val contact = contactsByNumber[message.number]
                if (message.number!!.contains(query) || contact?.name != null && contact.name.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(message)
                }
            }

            messages.addAll(filtered)
        }

        this.notifyDataSetChanged()
    }
}
