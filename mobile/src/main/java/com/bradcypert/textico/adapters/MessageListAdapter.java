package com.bradcypert.textico.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradcypert.textico.R;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by bradc on 3/11/2017.
 */

public class MessageListAdapter extends ArrayAdapter {
    public MessageListAdapter(Context context, ArrayList messageList) {
        super(context, 0, messageList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SMS message = (SMS) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_list_adapter_view, parent, false);
        }

        // Lookup view for data population
        TextView numberView = (TextView) convertView.findViewById(R.id.address);
        TextView bodyView = (TextView) convertView.findViewById(R.id.message_body);
        TextView timestampView = (TextView) convertView.findViewById(R.id.timestamp);
        ImageView contactImage = (ImageView) convertView.findViewById(R.id.contact_image);

        Contact contact = ContactsService.getContactForNumber(getContext().getContentResolver(), message.getNumber());
        if (contact.getName() != null) {
            numberView.setText(contact.getName());
        } else {
            numberView.setText(message.getNumber());
        }

        if (contact.getPicUri() != null) {
            contactImage.setImageURI(Uri.parse(contact.getPicUri()));
        } else {
            contactImage.setImageResource(R.mipmap.empty_portait);
        }

        bodyView.setText(message.getBody());

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mma", Locale.getDefault());
        String timestamp = message.getTimestamp() == null ? "" : df.format(message.getTimestamp());
        timestampView.setText(timestamp);

        return convertView;
    }
}
