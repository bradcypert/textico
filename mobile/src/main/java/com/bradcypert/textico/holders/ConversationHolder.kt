package com.bradcypert.textico.holders

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.util.Pair
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bradcypert.textico.ConversationDetails
import com.bradcypert.textico.R
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.models.SMS
import com.bradcypert.textico.services.ContactsService
import com.squareup.picasso.Picasso

import java.text.SimpleDateFormat
import java.util.Locale

class ConversationHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var numberView: TextView = itemView.findViewById(R.id.address)
    private var bodyView: TextView = itemView.findViewById(R.id.message_body)
    private var timestampView: TextView = itemView.findViewById(R.id.timestamp)
    private var contactImage: ImageView = itemView.findViewById(R.id.contact_image)

    private var hostActivity: Activity? = null
    lateinit var contact: Contact
    lateinit var message: SMS

    init {

        itemView.setOnClickListener(this)
    }

    fun bindSMS(message: SMS, host: Activity, contact: Contact) {
        this.contact = contact
        this.message = message

        if (this.contact.name != null) {
            this.numberView.text = contact.name
        } else {
            this.numberView.text = message.number
        }

        if (this.contact.picUri != null) {
            Picasso.get().load(contact.picUri).placeholder(R.mipmap.empty_portait).into(this.contactImage)
        } else {
            Picasso.get().load(R.mipmap.empty_portait).into(this.contactImage)
        }

        this.bodyView.text = message.body

        this.hostActivity = host

        val df = SimpleDateFormat("MM/dd/yyyy hh:mma", Locale.getDefault())
        val timestamp = if (message.timestamp == null) "" else df.format(message.timestamp)
        this.timestampView.text = timestamp
    }

    override fun onClick(v: View) {
        val intent = Intent(this.context, ConversationDetails::class.java)
        intent.putExtra(ConversationDetails.KEY, this.message.number)
        intent.putExtra(ConversationDetails.THREAD_ID, this.message.threadId)
        intent.putExtra(ConversationDetails.CONTACT_PICTURE, this.contact.picUri)
        intent.putExtra(ConversationDetails.CONTACT_NAME, this.contact.name)
        this.hostActivity!!.startActivity(intent)
    }
}
