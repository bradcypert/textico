package com.bradcypert.textico.views

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.bradcypert.textico.R
import com.bradcypert.textico.adapters.ConversationDetailsAdapter
import com.bradcypert.textico.migrations.RefreshMMS
import com.bradcypert.textico.models.Contact
import com.bradcypert.textico.repositories.SMSRepository
import com.bradcypert.textico.services.ThemeService
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.io.FileNotFoundException
import java.util.*

class ConversationDetails : AppCompatActivity() {
    private var adapter: ConversationDetailsAdapter? = null
    private var externalImage: Bitmap? = null
    private var currentContact: Contact? = null
    private var imageUri: Uri? = null
    @BindView(R.id.listView) lateinit var messageList: RecyclerView
    @BindView(R.id.send_text) lateinit var sendText: EditText
    @BindView(R.id.send_button) lateinit var sendButton: ImageButton
    @BindView(R.id.image_button) lateinit var imageButton: ImageButton
    @BindView(R.id.mmsImage) lateinit var imgv: ImageView
    @BindView(R.id.remove_image_button) lateinit var removeImageButton: ImageButton
    private val threadIdFromIntent: String
        get() {
            val intent = intent
            return intent.getStringExtra(THREAD_ID) ?: ""
        }

    private val contactPictureFromIntent: String
        get() {
            val intent = intent
            return intent.getStringExtra(CONTACT_PICTURE) ?: ""
        }

    private val contactNameFromIntent: String
        get() {
            val intent = intent
            return intent.getStringExtra(CONTACT_NAME) ?: ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeService.getSelectedTheme(this, true))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_details)
        ButterKnife.bind(this)

        this.currentContact = Contact(name = contactNameFromIntent, picUri = contactPictureFromIntent)

        setupMessageList()

        Thread(Runnable {
            val key = getKeyFromIntent(true)
            if (key != null) {
                SMSRepository.flagMessageAsRead(contentResolver, key)
            }
            setupSendButton()
            setupPictureButton()
        }).run()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_conversation_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId


        if (id == R.id.action_call) {
            if (ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:${getKeyFromIntent(true)}")
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CALL_PHONE),
                        REQUEST_CODE_ASK_PERMISSIONS
                )
                Toast.makeText(applicationContext, "If you granted permission, you can tap the phone icon to try again!", Toast.LENGTH_LONG).show()
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupPictureButton() {
        imageButton.setOnClickListener {
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
                    imageUri = data.data
                    externalImage = BitmapFactory.decodeStream(applicationContext.contentResolver.openInputStream(data.data!!))
                    val imgv = findViewById<ImageView>(R.id.mmsImage)
                    imgv.setImageBitmap(externalImage)
                    imgv.visibility = View.VISIBLE

                    val btn = findViewById<ImageButton>(R.id.remove_image_button)
                    btn.visibility = View.VISIBLE
                    btn.setOnClickListener {
                        imageUri = null
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

            val sendSettings = Settings()
            sendSettings.sendLongAsMms = true
            val sendTransaction = Transaction(baseContext, sendSettings)
            val mMessage = Message(text, getKeyFromIntent(false)!!)
            sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID)
            val m = com.bradcypert.textico.models.Message(number=getKeyFromIntent(false),
                    body=text,
                    timestamp = Date(),
                    read = true,
                    id = 0,
                    threadId = threadIdFromIntent,
                    person = contactNameFromIntent,
                    sentByMe = true,
                    isArchived = false,
                    messageType = 0,
                    type=null,
                    part=null)
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.insert(m)
            realm.commitTransaction()
            sendText.setText("")

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

            val sendSettings = Settings()
            sendSettings.sendLongAsMms = true
            sendSettings.useSystemSending = true
            val sendTransaction = Transaction(baseContext, sendSettings)
            val mMessage = Message(text, getKeyFromIntent(true)!!)
            mMessage.setImage(externalImage)
            Toast.makeText(this.applicationContext, "Sending MMS...", Toast.LENGTH_SHORT).show()
            sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID)
            RefreshMMS(this.applicationContext).run()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        Toast.makeText(this.applicationContext, "I know. That's bad. I'm working on it :)", Toast.LENGTH_SHORT).show()
                    }
            sendText.setText("")
            imgv.visibility = View.GONE
            removeImageButton.visibility = View.GONE
            imgv.setImageBitmap(null)
            externalImage = null
            imageUri = null
        } else {
            ActivityCompat.requestPermissions(
                    this@ConversationDetails,
                    arrayOf(Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.RECEIVE_MMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CHANGE_NETWORK_STATE),
                    REQUEST_CODE_ASK_PERMISSIONS
            )
            setupSendButton()
        }
    }

    private fun setupSendButton() {
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
        val realm = Realm.getDefaultInstance()
        val messages = getMessages(realm)
        adapter = ConversationDetailsAdapter(this, R.layout.conversation_details_list_adapter, messages!!, currentContact!!)

        title = if (currentContact!!.name != null && !currentContact!!.name.equals("")) {
            currentContact!!.name
        } else {
            getKeyFromIntent(false)
        }
        messageList.adapter = adapter
        messageList.layoutManager = LinearLayoutManager(this)
        messageList.scrollToPosition(messages.size - 1)
    }

    private fun getMessages(realm: Realm): RealmResults<com.bradcypert.textico.models.Message>? {
        return realm.where<com.bradcypert.textico.models.Message>()
                .equalTo("rootNumber", getKeyFromIntent(true))
                .sort("timestamp", Sort.ASCENDING)
                .findAll()
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
