package com.bradcypert.textico.adapters;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bodyView.getLayoutParams();
        if (message != null) {
            if (message.isSentByMe()) {
                bodyView.setBackgroundResource(R.drawable.chat_bubble_mine);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.setMarginEnd(0);
                params.setMarginStart(120);
            } else {
                bodyView.setBackgroundResource(R.drawable.chat_bubble);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.setMarginStart(0);
                params.setMarginEnd(120);
            }
            bodyView.setLayoutParams(params);
            bodyView.setText(message.getBody());
        }
        return convertView;
    }
}
