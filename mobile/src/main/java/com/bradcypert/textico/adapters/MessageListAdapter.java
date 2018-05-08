package com.bradcypert.textico.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bradcypert.textico.holders.ConversationHolder;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bradc on 3/11/2017.
 */

public class MessageListAdapter extends RecyclerView.Adapter<ConversationHolder> implements SearchAndRemove {
    private final ArrayList<SMS> messages;
    private final ArrayList<SMS> rootMessages;
    private final HashMap<String, Contact> contactsByNumber = new HashMap<>();
    private Context context;
    private Activity host;
    private int itemResource;

    public MessageListAdapter(final Context context, int itemResource, ArrayList<SMS> messageList, Activity activity) {
        super();
        this.messages = messageList;
        this.rootMessages = new ArrayList<>(messageList);
        final ArrayList<SMS> rootMessages = this.rootMessages;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SMS message: rootMessages) {
                    contactsByNumber.put(message.getNumber(), ContactsService.getContactForNumber(context.getContentResolver(), message.getNumber()));
                }
            }
        }).run();

        this.context = context;
        this.itemResource = itemResource;
        this.host = activity;
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
        holder.bindSMS(message, this.host);
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
        messages.clear();
        if (query.equals("")) {
            messages.addAll(rootMessages);
        } else {
            ArrayList<SMS> filtered = new ArrayList<>();
            for (SMS message: rootMessages) {
                Contact contact = contactsByNumber.get(message.getNumber());
                if (message.getNumber().contains(query)
                        || (contact.getName() != null && contact.getName().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(message);
                }
            }

            messages.addAll(filtered);
        }

        this.notifyDataSetChanged();
    }
}
