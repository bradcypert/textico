package com.bradcypert.textico.holders;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradcypert.textico.ConversationDetails;
import com.bradcypert.textico.R;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConversationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;

    public TextView numberView;
    public TextView bodyView;
    public TextView timestampView;
    public ImageView contactImage;

    private Activity hostActivity;
    public Contact contact;
    public SMS message;

    public ConversationHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;

        this.numberView = (TextView) itemView.findViewById(R.id.address);
        this.bodyView = (TextView) itemView.findViewById(R.id.message_body);
        this.timestampView = (TextView) itemView.findViewById(R.id.timestamp);
        this.contactImage = (ImageView) itemView.findViewById(R.id.contact_image);

        itemView.setOnClickListener(this);
    }

    public void bindSMS(SMS message, Activity host) {
        this.contact = ContactsService.getContactForNumber(this.context.getContentResolver(), message.getNumber());
        this.message = message;

        if (this.contact.getName() != null) {
            this.numberView.setText(contact.getName());
        } else {
            this.numberView.setText(message.getNumber());
        }

        if (this.contact.getPicUri() != null) {
            this.contactImage.setImageURI(Uri.parse(contact.getPicUri()));
        } else {
            this.contactImage.setImageResource(R.mipmap.empty_portait);
        }

        this.bodyView.setText(message.getBody());

        this.hostActivity = host;

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mma", Locale.getDefault());
        String timestamp = message.getTimestamp() == null ? "" : df.format(message.getTimestamp());
        this.timestampView.setText(timestamp);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.context, ConversationDetails.class);
        intent.putExtra(ConversationDetails.KEY, this.message.getNumber());
        intent.putExtra(ConversationDetails.THREAD_ID, this.message.getThreadId());
        this.hostActivity.getWindow().setSharedElementEnterTransition(new Slide());
        this.hostActivity.getWindow().setSharedElementExitTransition(new Slide());
//        this.hostActivity.startActivity(intent,
//                ActivityOptions.makeSceneTransitionAnimation(this.hostActivity,
//                        Pair.create((View)this.contactImage, "contact_portrait")).toBundle());
        this.hostActivity.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(this.hostActivity).toBundle());
    }
}
