package com.bradcypert.textico

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast

import com.bradcypert.textico.adapters.ConversationDetailsAdapter
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.models.MMS
import com.bradcypert.textico.models.SMS
import com.bradcypert.textico.services.MMSService
import com.bradcypert.textico.services.MessageService
import com.bradcypert.textico.services.ThemeService
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction

import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

class ConversationDetails : AppCompatActivity() {
    private var smsMessages: ArrayList<SMS>? = null
    private var mmsMessages: ArrayList<MMS>? = null
    private val messagesForAdapter = ArrayList<com.bradcypert.textico.models.Message>()
    private var adapter: ConversationDetailsAdapter? = null
    private var timer: Timer? = null
    private var externalImage: Bitmap? = null
    private var currentContact: Contact? = null
    private var messageList: RecyclerView? = null
    private var sendText: EditText? = null
    private var sendButton: ImageButton? = null

    private val threadIdFromIntent: String?
        get() {
            val intent = intent
            return intent.getStringExtra(THREAD_ID)
        }

    private val contactPictureFromIntent: String?
        get() {
            val intent = intent
            return intent.getStringExtra(CONTACT_PICTURE)
        }

    private val contactNameFromIntent: String?
        get() {
            val intent = intent
            return intent.getStringExtra(CONTACT_NAME)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, true))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_details)

        this.currentContact = Contact.ContactBuilder()
                .setName(contactNameFromIntent)
                .setPicUri(contactPictureFromIntent)
                .build()
        this.messageList = findViewById(R.id.listView)
        this.sendText = findViewById(R.id.send_text)
        this.sendButton = findViewById(R.id.image_button)

        Thread(Runnable {
            val key = getKeyFromIntent(true)
            if (key != null) {
                MessageService.flagMessageAsRead(contentResolver, key)
            }
            setupMessageList()
            setupSendButton()
            setupPictureButton()
            setupWatcher()
        }).run()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer!!.cancel()
        timer!!.purge()
        timer = null
    }

    private fun setupWatcher() {
        if (timer == null) {
            timer = Timer()
        }
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newMMS = MMSService.getAllMmsMessages(baseContext, threadIdFromIntent!!)
                val newMessages = MessageService.getConversationDetails(contentResolver, getKeyFromIntent(true)!!)
                if (smsMessages!!.size != newMessages.size || newMMS.size != mmsMessages!!.size) {
                    smsMessages!!.clear()
                    mmsMessages!!.clear()
                    mmsMessages!!.addAll(newMMS)
                    smsMessages!!.addAll(newMessages)
                    messagesForAdapter.clear()
                    messagesForAdapter.addAll(newMessages)
                    messagesForAdapter.addAll(newMMS)
                    messagesForAdapter.sort()
                    runOnUiThread { adapter!!.notifyDataSetChanged() }
                }
            }
        }, 250, 3000)
    }

    private fun setupPictureButton() {
        sendButton!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (data.data != null) {
                try {
                    externalImage = BitmapFactory.decodeStream(applicationContext.contentResolver.openInputStream(data.data!!))
                    val imgv = findViewById<ImageView>(R.id.mmsImage)
                    imgv.setImageBitmap(externalImage)
                    imgv.visibility = View.VISIBLE

                    val btn = findViewById<ImageButton>(R.id.remove_image_button)
                    btn.visibility = View.VISIBLE
                    btn.setOnClickListener {
                        externalImage = null
                        imgv.visibility = View.GONE
                        imgv.setImageBitmap(null)
                        btn.visibility = View.GONE
                    }
                } catch (e: FileNotFoundException) {
                    Toast.makeText(baseContext, R.string.unable_to_pick_file, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }

            }
        }
    }

    private fun sendSMS(text: String) {
        val sendText = findViewById<EditText>(R.id.send_text)
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(baseContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            AsyncTask.execute {
                val sendSettings = Settings()
                sendSettings.sendLongAsMms = true
                val sendTransaction = Transaction(baseContext, sendSettings)
                val mMessage = Message(text, getKeyFromIntent(true)!!)
                sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID)
                runOnUiThread { sendText.setText("") }
            }

            try {
                timer!!.cancel()
                timer!!.purge()
                timer = null
                setupWatcher()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            ActivityCompat.requestPermissions(
                    this@ConversationDetails,
                    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CHANGE_NETWORK_STATE),
                    REQUEST_CODE_ASK_PERMISSIONS
            )
            setupSendButton()
        }
    }

    private fun sendMMS(text: String) {
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(baseContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            AsyncTask.execute {
                val sendSettings = Settings()
                sendSettings.sendLongAsMms = true
                sendSettings.useSystemSending = true
                val sendTransaction = Transaction(baseContext, sendSettings)
                val mMessage = Message(text, getKeyFromIntent(true)!!)
                mMessage.setImage(externalImage)
                sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID)
                val imgv = findViewById<ImageView>(R.id.mmsImage)
                runOnUiThread {
                    sendText!!.setText("")
                    imgv.setImageBitmap(null)
                    imgv.visibility = View.INVISIBLE
                    externalImage = null
                    val btn = findViewById<ImageButton>(R.id.remove_image_button)
                    btn.visibility = View.INVISIBLE
                }
                try {
                    timer!!.cancel()
                    timer!!.purge()
                    timer = null
                    setupWatcher()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                    this@ConversationDetails,
                    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CHANGE_NETWORK_STATE),
                    REQUEST_CODE_ASK_PERMISSIONS
            )
            setupSendButton()
        }
    }

    private fun setupSendButton() {
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        val sendText = findViewById<EditText>(R.id.send_text)
        sendButton.setOnClickListener {
            val value = sendText.text.toString()
            if (externalImage != null) {
                sendMMS(value)
            } else if (value != "") {
                sendSMS(value)
            }
        }
    }

    private fun setupMessageList() {
        val activity = this
        AsyncTask.execute {
            smsMessages = MessageService.getConversationDetails(contentResolver, getKeyFromIntent(true)!!)
            mmsMessages = MMSService.getAllMmsMessages(baseContext, threadIdFromIntent!!)
            messagesForAdapter.addAll(smsMessages!!)
            messagesForAdapter.addAll(mmsMessages!!)
            messagesForAdapter.sort()
            adapter = ConversationDetailsAdapter(activity, R.layout.conversation_details_list_adapter, messagesForAdapter, currentContact!!)

            runOnUiThread {
                if (currentContact!!.name != null) {
                    title = currentContact!!.name
                } else {
                    title = getKeyFromIntent(false)
                }
                messageList!!.adapter = adapter
                messageList!!.layoutManager = LinearLayoutManager(activity)
                messageList!!.scrollToPosition(messagesForAdapter.size - 1)
            }
        }
    }

    private fun getKeyFromIntent(filter: Boolean): String? {
        val intent = intent
        return if (filter)
            intent.getStringExtra(KEY).replace("[\\D]".toRegex(), "")
        else
            intent.getStringExtra(KEY)
    }

    companion object {
        const val KEY = "id"
        const val THREAD_ID = "thread_id"
        const val CONTACT_PICTURE = "contact_picture"
        const val CONTACT_NAME = "contact_name"
        private const val REQUEST_CODE_ASK_PERMISSIONS = 123
        private const val IMAGE_REQUEST_CODE = 555
    }
}
