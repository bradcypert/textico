package com.bradcypert.textico.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bradcypert.textico.holders.ConversationDetailsHolder;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.Message;
import com.bradcypert.textico.services.ContactsService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bradc on 3/11/2017.
 */

public class ConversationDetailsAdapter extends RecyclerView.Adapter<ConversationDetailsHolder> implements SearchAndRemove {
    private final ArrayList<Message> messages;
    private Context context;
    private int itemResource;
    private final HashMap<String, Contact> contactsByNumber = new HashMap<>();

    public ConversationDetailsAdapter(Context context, int itemResource, ArrayList<Message> messageList) {
        super();
        this.messages = messageList;
        this.context = context;
        this.itemResource = itemResource;

        for (Message message: this.messages) {
            contactsByNumber.put(message.getNumber(), ContactsService.getContactForNumber(context.getContentResolver(), message.getNumber()));
        }
    }

    @Override
    public ConversationDetailsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 3. Inflate the view and return the new ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(this.itemResource, parent, false);
        return new ConversationDetailsHolder(this.context, view);
    }

    // 4. Override the onBindViewHolder method
    @Override
    public void onBindViewHolder(ConversationDetailsHolder holder, int position) {
        Message message = this.messages.get(position);
        Message after = null;
        boolean showImage = false;

        try {
            after = this.messages.get(position + 1);
        } catch (Exception e) {
            showImage = true;
        }

        if (after == null) {
            showImage = true;
        } else if (!message.isSentByMe() && message.getSenderId() != null && !message.getSenderId().equals(after.getSenderId())) {
            showImage = true;
        }

        holder.bindMessage(message, showImage, contactsByNumber.get(message.getNumber()));
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    @Override
    public void removeItem(int position) {
        messages.remove(position);
        this.notifyItemRemoved(position);
    }

    @Override
    public void search(String query) {

    }
}
