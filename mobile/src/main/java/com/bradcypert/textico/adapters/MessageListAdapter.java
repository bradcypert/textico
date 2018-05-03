package com.bradcypert.textico.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradcypert.textico.R;
import com.bradcypert.textico.holders.ConversationHolder;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by bradc on 3/11/2017.
 */

public class MessageListAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private final ArrayList<SMS> messages;
    private Context context;
    private int itemResource;

    public MessageListAdapter(Context context, int itemResource, ArrayList<SMS> messageList) {
        super();
        this.messages = messageList;
        this.context = context;
        this.itemResource = itemResource;
    }

    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 3. Inflate the view and return the new ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(this.itemResource, parent, false);
        return new ConversationHolder(this.context, view);
    }

    // 4. Override the onBindViewHolder method
    @Override
    public void onBindViewHolder(ConversationHolder holder, int position) {
        SMS message = this.messages.get(position);
        holder.bindSMS(message);
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }
}
