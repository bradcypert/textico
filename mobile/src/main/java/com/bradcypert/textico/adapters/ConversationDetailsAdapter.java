package com.bradcypert.textico.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bradcypert.textico.holders.ConversationDetailsHolder;
import com.bradcypert.textico.models.SMS;

import java.util.ArrayList;

/**
 * Created by bradc on 3/11/2017.
 */

public class ConversationDetailsAdapter extends RecyclerView.Adapter<ConversationDetailsHolder> implements SearchAndRemove {
    private final ArrayList<SMS> messages;
    private Context context;
    private int itemResource;

    public ConversationDetailsAdapter(Context context, int itemResource, ArrayList<SMS> messageList) {
        super();
        this.messages = messageList;
        this.context = context;
        this.itemResource = itemResource;
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
        SMS message = this.messages.get(position);
        holder.bindSMS(message);
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
