package com.bradcypert.textico.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.bradcypert.textico.R
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.models.MMS
import com.bradcypert.textico.models.Message
import com.bradcypert.textico.repositories.MMSRepository
import com.squareup.picasso.Picasso

class ConversationDetailsHolder(private val context: Context, itemView: View, var contact: Contact) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val messageBodyContainer: LinearLayout = itemView.findViewById(R.id.message_body_container)
    private val messageBody: TextView = itemView.findViewById(R.id.message_body)
    private val contactImage: ImageView = itemView.findViewById(R.id.contact_details_image)
    private val mmsImage: ImageView = itemView.findViewById(R.id.mms_image)
    private var message: Message? = null

    init {

        itemView.setOnClickListener(this)
    }

    fun bindMessage(message: Message, showImage: Boolean) {
        this.message = message

        this.messageBody.text = message.body
        this.contactImage.setImageDrawable(null)
        this.contactImage.setImageURI(null)
        this.contactImage.visibility = View.GONE
        this.mmsImage.visibility = View.GONE
        this.messageBody.visibility = View.VISIBLE

        if (message.body == null || message.body == "") {
            this.messageBody.visibility = View.GONE
        }

        // this bad boi is an SMS
        if (message.messageType == 1) {
            this.mmsImage.visibility = View.VISIBLE
            val m = message as MMS
            Thread(Runnable {
                val bmp = MMSRepository.getMmsImage(context, m.id)
                mmsImage.setImageBitmap(bmp)
            }).run()
        }

        val params = this.messageBodyContainer.layoutParams as RelativeLayout.LayoutParams
        if (message.isSentByMe) {
            this.messageBodyContainer.setBackgroundResource(R.drawable.chat_bubble_mine)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            this.messageBodyContainer.setBackgroundResource(R.drawable.chat_bubble)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

            if (showImage) {
                if (this.contact.picUri != null && this.contact.picUri != "") {
                    Picasso.get().load(this.contact.picUri).into(this.contactImage)
                } else {
                    Picasso.get().load(R.mipmap.empty_portait).into(this.contactImage)
                }
                this.contactImage.visibility = View.VISIBLE
            }
        }
        this.messageBodyContainer.layoutParams = params
        this.messageBody.text = message.body
    }

    override fun onClick(v: View) {}
}
