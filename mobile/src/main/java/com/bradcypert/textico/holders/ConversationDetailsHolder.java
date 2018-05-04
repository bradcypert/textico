package com.bradcypert.textico.holders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bradcypert.textico.ConversationDetails;
import com.bradcypert.textico.R;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConversationDetailsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;

    private RelativeLayout messageContainer;
    private TextView messageBody;
    private ImageView contactImage;

    public Contact contact;
    private SMS message;

    public ConversationDetailsHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;

        this.messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
        this.messageBody = (TextView) itemView.findViewById(R.id.message_body);
        this.contactImage = (ImageView) itemView.findViewById(R.id.contact_details_image);

        itemView.setOnClickListener(this);
    }

    public void bindSMS(SMS message, boolean showImage, Contact contact) {
        this.contact = contact;
        this.message = message;

        this.messageBody.setText(message.getBody());
        this.contactImage.setImageDrawable(null);
        this.contactImage.setImageURI(null);
        this.contactImage.setAlpha(0);
        this.contactImage.setImageAlpha(0);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.messageBody.getLayoutParams();
        if (message.isSentByMe()) {
            this.messageBody.setBackgroundResource(R.drawable.chat_bubble_mine);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMarginEnd(0);
            params.setMarginStart(120);
        } else {
            this.messageBody.setBackgroundResource(R.drawable.chat_bubble);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMarginStart(0);
            params.setMarginEnd(120);

            if (showImage) {
                if (this.contact.getPicUri() != null && !this.contact.getPicUri().equals("")) {
                    this.contactImage.setImageURI(Uri.parse(this.contact.getPicUri()));
                } else {
                    this.contactImage.setImageResource(R.mipmap.empty_portait);
                }
                this.contactImage.setAlpha(255);
                this.contactImage.setImageAlpha(255);
            }
        }
        this.messageBody.setLayoutParams(params);
        this.messageBody.setText(message.getBody());
    }

    @Override
    public void onClick(View v) {}
}
