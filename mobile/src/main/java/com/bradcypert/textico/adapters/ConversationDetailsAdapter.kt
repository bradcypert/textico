package com.bradcypert.textico.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bradcypert.textico.holders.ConversationDetailsHolder
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.services.ContactsService
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by bradc on 3/11/2017.
 */

class ConversationDetailsAdapter(private val context: Context,
                                 private val itemResource: Int,
                                 private val messages: OrderedRealmCollection<Message>,
                                 private val contact: Contact) : RealmRecyclerViewAdapter<Message,ConversationDetailsHolder>(messages,true), SearchAndRemove {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationDetailsHolder {
        // 3. Inflate the view and return the new ViewHolder
        val view = LayoutInflater.from(parent.context)
                .inflate(this.itemResource, parent, false)
        return ConversationDetailsHolder(this.context, view, contact)
    }

    // 4. Override the onBindViewHolder method
    override fun onBindViewHolder(holder: ConversationDetailsHolder, position: Int) {
        val message = this.messages[position]
        var after: Message? = null
        var showImage = false

        try {
            after = this.messages[position + 1]
        } catch (e: Exception) {
            showImage = true
        }

        if (after == null) {
            showImage = true
        } else if (!message.isSentByMe && after.isSentByMe) {
            showImage = true
        }

        holder.bindMessage(message, showImage)
    }

    override fun getItemCount(): Int {
        return this.messages.size
    }

    override fun removeItem(position: Int) {
        messages.removeAt(position)
        this.notifyItemRemoved(position)
    }

    override fun search(query: String) {

    }
}
