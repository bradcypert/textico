package com.bradcypert.textico.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bradcypert.textico.R;
import com.bradcypert.textico.models.SMS;

import java.util.ArrayList;

public class ConversationDetailsAdapter extends ArrayAdapter {
    public ConversationDetailsAdapter(Context context, ArrayList messageList) {
        super(context, 0, messageList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SMS message = (SMS) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.conversation_details_list_adapter, parent, false);
        }

        TextView bodyView = (TextView) convertView.findViewById(R.id.message_body);
        LinearLayout messageContainer = (LinearLayout) convertView.findViewById(R.id.message_container);
        if (message != null) {
            if (message.isSentByMe()) {
                bodyView.setBackgroundResource(R.drawable.chat_bubble_mine);
                messageContainer.setHorizontalGravity(Gravity.END);
                messageContainer.setGravity(Gravity.END);
            } else {
                bodyView.setBackgroundResource(R.drawable.chat_bubble);
                messageContainer.setHorizontalGravity(Gravity.START);
                messageContainer.setGravity(Gravity.START);
            }

            bodyView.setText(message.getBody());
        }
        return convertView;
    }
}
