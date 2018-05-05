package com.bradcypert.textico;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bradcypert.textico.adapters.ConversationDetailsAdapter;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;
import com.bradcypert.textico.services.MessageService;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ConversationDetails extends AppCompatActivity {
    public static final String KEY="id";
    private String SMS_SENT = "SMS_SENT";
    private String SMS_DELIVERED = "SMS_DELIVERED";
    private final SmsManager smsManager = SmsManager.getDefault();
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private ArrayList<SMS> messages;
    private ConversationDetailsAdapter adapter;
    private Timer timer;
    private Bitmap externalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_details);

        String key = getKeyFromIntent(true);
        if (key != null) {
            MessageService.flagMessageAsRead(getContentResolver(), key);
        }
        setupMessageList();
        setupSendButton();
        setupPictureButton();
        setupWatcher();
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
                ArrayList<SMS> newMessages = MessageService.getConversationDetails(getContentResolver(), getKeyFromIntent(true));
                if(messages.size() != newMessages.size()) {
                    messages.clear();
                    messages.addAll(newMessages);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }, 250, 1000);
    }

    private void setupPictureButton() {
        ImageButton sendButton = (ImageButton) findViewById(R.id.image_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 555);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 555) {
            final Bundle extras = data.getExtras();

            if (data.getData() != null) {
                try {
                    externalImage = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(data.getData()));
                    final ImageView imgv = (ImageView) findViewById(R.id.mmsImage);
                    imgv.setImageBitmap(externalImage);
                    imgv.setVisibility(View.VISIBLE);

                    final ImageButton btn = (ImageButton) findViewById(R.id.remove_image_button);
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
        final EditText sendText = (EditText) findViewById(R.id.send_text);
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Settings sendSettings = new Settings();
                    sendSettings.setSendLongAsMms(true);
                    Transaction sendTransaction = new Transaction(getBaseContext(), sendSettings);
                    Message mMessage = new Message(text, getKeyFromIntent(true));
                    sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID);
                    sendText.setText("");
                }
            }).run();

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
        final EditText sendText = (EditText) findViewById(R.id.send_text);
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Settings sendSettings = new Settings();
                    sendSettings.setSendLongAsMms(true);
                    sendSettings.setUseSystemSending(true);
                    Transaction sendTransaction = new Transaction(getBaseContext(), sendSettings);
                    Message mMessage = new Message(text, getKeyFromIntent(true));
                    mMessage.setImage(externalImage);
                    sendTransaction.sendNewMessage(mMessage, Transaction.NO_THREAD_ID);
                    sendText.setText("");
                    final ImageView imgv = (ImageView) findViewById(R.id.mmsImage);
                    imgv.setImageBitmap(null);
                    imgv.setVisibility(View.INVISIBLE);
                    externalImage = null;
                    final ImageButton btn = (ImageButton) findViewById(R.id.remove_image_button);
                    btn.setVisibility(View.INVISIBLE);
                }
            }).run();


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

    private void setupSendButton() {
        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        final EditText sendText = (EditText) findViewById(R.id.send_text);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = sendText.getText().toString();
                if (externalImage != null) {
                    sendMMS(value);
                } else if (!value.equals("")) {
                    sendSMS(value);
                };
            }
        });
    }

    private void setupMessageList() {
        final Activity activity = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                messages = MessageService.getConversationDetails(getContentResolver(), getKeyFromIntent(true));
                Contact currentContact = ContactsService.getContactForNumber(getContentResolver(), getKeyFromIntent(false));
                if (currentContact.getName() != null) {
                    setTitle(currentContact.getName());
                } else {
                    setTitle(getKeyFromIntent(false));
                }
                RecyclerView messageList = (RecyclerView) findViewById(R.id.listView);
                adapter = new ConversationDetailsAdapter(activity, R.layout.conversation_details_list_adapter, messages);
                messageList.setAdapter(adapter);
                messageList.setLayoutManager(new LinearLayoutManager(activity));
                messageList.scrollToPosition(messages.size() - 1);
            }
        }).run();
    }

    private String getKeyFromIntent(boolean filter) {
        Intent intent = getIntent();
        if (filter) return intent.getStringExtra(KEY).replaceAll("[\\D]","");
        else return intent.getStringExtra(KEY);
    }
}
