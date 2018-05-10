package com.bradcypert.textico;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bradcypert.textico.adapters.ConversationDetailsAdapter;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.MMS;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.MMSService;
import com.bradcypert.textico.services.MessageService;
import com.bradcypert.textico.services.ThemeService;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationDetails extends AppCompatActivity {
    public static final String KEY="id";
    public static final String THREAD_ID="thread_id";
    public static final String CONTACT_PICTURE="contact_picture";
    public static final String CONTACT_NAME="contact_name";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final static int IMAGE_REQUEST_CODE = 555;
    private ArrayList<SMS> smsMessages;
    private ArrayList<MMS> mmsMessages;
    private ArrayList<com.bradcypert.textico.models.Message> messagesForAdapter = new ArrayList<>();
    private ConversationDetailsAdapter adapter;
    private Timer timer;
    private Bitmap externalImage;
    private Contact currentContact;
    private RecyclerView messageList;
    private EditText sendText;
    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeService.getSelectedTheme(this, true));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_details);

        this.currentContact = new Contact.ContactBuilder().setName(getContactNameFromIntent()).setPicUri(getContactPictureFromIntent()).build();
        this.messageList = findViewById(R.id.listView);
        this.sendText = findViewById(R.id.send_text);
        this.sendButton = findViewById(R.id.image_button);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = getKeyFromIntent(true);
                if (key != null) {
                    MessageService.flagMessageAsRead(getContentResolver(), key);
                }
                setupMessageList();
                setupSendButton();
                setupPictureButton();
                setupWatcher();
            }
        }).run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
        timer = null;
    }

    private void setupWatcher() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ArrayList<MMS> newMMS = MMSService.getAllMmsMessages(getBaseContext(), getThreadIdFromIntent());
                ArrayList<SMS> newMessages = MessageService.getConversationDetails(getContentResolver(), getKeyFromIntent(true));
                if(smsMessages.size() != newMessages.size() || newMMS.size() != mmsMessages.size()) {
                    smsMessages.clear();
                    mmsMessages.clear();
                    mmsMessages.addAll(newMMS);
                    smsMessages.addAll(newMessages);
                    messagesForAdapter.clear();
                    messagesForAdapter.addAll(newMessages);
                    messagesForAdapter.addAll(newMMS);
                    Collections.sort(messagesForAdapter);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }, 250, 3000);
    }

    private void setupPictureButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (data.getData() != null) {
                try {
                    externalImage = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(data.getData()));
                    final ImageView imgv = findViewById(R.id.mmsImage);
                    imgv.setImageBitmap(externalImage);
                    imgv.setVisibility(View.VISIBLE);

                    final ImageButton btn = findViewById(R.id.remove_image_button);
                    btn.setVisibility(View.VISIBLE);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            externalImage = null;
                            imgv.setVisibility(View.GONE);
                            imgv.setImageBitmap(null);
                            btn.setVisibility(View.GONE);
                        }
                    });
                } catch (FileNotFoundException e) {
                    Toast.makeText(getBaseContext(), R.string.unable_to_pick_file, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendSMS(final String text) {
        final EditText sendText = findViewById(R.id.send_text);
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Settings sendSettings = new Settings();
                    sendSettings.setSendLongAsMms(true);
                    Transaction sendTransaction = new Transaction(getBaseContext(), sendSettings);
                    Message mMessage = new Message(text, getKeyFromIntent(true));
                    sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendText.setText("");
                        }
                    });
                }
            });

            try {
                timer.cancel();
                timer.purge();
                timer = null;
                setupWatcher();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(
                    ConversationDetails.this,
                    new String[]{Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.RECEIVE_MMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CHANGE_NETWORK_STATE},
                    REQUEST_CODE_ASK_PERMISSIONS
            );
            setupSendButton();
        }
    }

    private void sendMMS(final String text) {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Settings sendSettings = new Settings();
                    sendSettings.setSendLongAsMms(true);
                    sendSettings.setUseSystemSending(true);
                    Transaction sendTransaction = new Transaction(getBaseContext(), sendSettings);
                    Message mMessage = new Message(text, getKeyFromIntent(true));
                    mMessage.setImage(externalImage);
                    sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID);
                    final ImageView imgv = findViewById(R.id.mmsImage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendText.setText("");
                            imgv.setImageBitmap(null);
                            imgv.setVisibility(View.INVISIBLE);
                            externalImage = null;
                            final ImageButton btn = findViewById(R.id.remove_image_button);
                            btn.setVisibility(View.INVISIBLE);
                        }
                    });
                    try {
                        timer.cancel();
                        timer.purge();
                        timer = null;
                        setupWatcher();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(
                    ConversationDetails.this,
                    new String[]{Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.RECEIVE_MMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CHANGE_NETWORK_STATE},
                    REQUEST_CODE_ASK_PERMISSIONS
            );
            setupSendButton();
        }
    }

    private void setupSendButton() {
        ImageButton sendButton = findViewById(R.id.send_button);
        final EditText sendText = findViewById(R.id.send_text);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = sendText.getText().toString();
                if (externalImage != null) {
                    sendMMS(value);
                } else if (!value.equals("")) {
                    sendSMS(value);
                }
            }
        });
    }

    private void setupMessageList() {
        final Activity activity = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                smsMessages = MessageService.getConversationDetails(getContentResolver(), getKeyFromIntent(true));
                mmsMessages = MMSService.getAllMmsMessages(getBaseContext(), getThreadIdFromIntent());
                messagesForAdapter.addAll(smsMessages);
                messagesForAdapter.addAll(mmsMessages);
                Collections.sort(messagesForAdapter);
                adapter = new ConversationDetailsAdapter(activity, R.layout.conversation_details_list_adapter, messagesForAdapter, currentContact);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentContact.getName() != null) {
                            setTitle(currentContact.getName());
                        } else {
                            setTitle(getKeyFromIntent(false));
                        }
                        messageList.setAdapter(adapter);
                        messageList.setLayoutManager(new LinearLayoutManager(activity));
                        messageList.scrollToPosition(messagesForAdapter.size() - 1);
                    }
                });
            }
        });
    }

    private String getThreadIdFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(THREAD_ID);
    }

    private String getContactPictureFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(CONTACT_PICTURE);
    }

    private String getContactNameFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(CONTACT_NAME);
    }

    private String getKeyFromIntent(boolean filter) {
        Intent intent = getIntent();
        if (filter) return intent.getStringExtra(KEY).replaceAll("[\\D]","");
        else return intent.getStringExtra(KEY);
    }
}
