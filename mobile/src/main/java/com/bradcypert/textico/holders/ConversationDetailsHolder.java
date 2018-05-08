package com.bradcypert.textico.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bradcypert.textico.ConversationDetails;
import com.bradcypert.textico.R;
import com.bradcypert.textico.models.Contact;
import com.bradcypert.textico.models.MMS;
import com.bradcypert.textico.models.Message;
import com.bradcypert.textico.models.SMS;
import com.bradcypert.textico.services.ContactsService;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConversationDetailsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;

    private RelativeLayout messageContainer;
    private LinearLayout messageBodyContainer;
    private TextView messageBody;
    private ImageView contactImage;
    private ImageView mmsImage;

    public Contact contact;
    private Message message;

    public ConversationDetailsHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;

        this.messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
        this.messageBodyContainer = (LinearLayout) itemView.findViewById(R.id.message_body_container);
        this.messageBody = (TextView) itemView.findViewById(R.id.message_body);
        this.contactImage = (ImageView) itemView.findViewById(R.id.contact_details_image);
        this.mmsImage = (ImageView) itemView.findViewById(R.id.mms_image);

        itemView.setOnClickListener(this);
    }

    public void bindMessage(Message message, boolean showImage, Contact contact) {
        this.contact = contact;
        this.message = message;

        this.messageBody.setText(message.getBody());
        this.contactImage.setImageDrawable(null);
        this.contactImage.setImageURI(null);
        this.contactImage.setVisibility(View.GONE);
        this.mmsImage.setVisibility(View.GONE);
        this.messageBody.setVisibility(View.VISIBLE);

        if (message.getBody() == null || message.getBody().equals("")) {
            this.messageBody.setVisibility(View.GONE);
        }

        // this bad boi is an SMS
        if (message.getMessageType() == 1) {
            this.mmsImage.setVisibility(View.VISIBLE);
            MMS m = (MMS) message;
            this.mmsImage.setImageBitmap(m.getImage());
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.messageBodyContainer.getLayoutParams();
        if (message.isSentByMe()) {
            this.messageBodyContainer.setBackgroundResource(R.drawable.chat_bubble_mine);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            this.messageBodyContainer.setBackgroundResource(R.drawable.chat_bubble);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            if (showImage) {
                if (this.contact.getPicUri() != null && !this.contact.getPicUri().equals("")) {
                    this.contactImage.setImageURI(Uri.parse(this.contact.getPicUri()));
                } else {
                    this.contactImage.setImageResource(R.mipmap.empty_portait);
                }
                this.contactImage.setVisibility(View.VISIBLE);
            }
        }
        this.messageBodyContainer.setLayoutParams(params);
        this.messageBody.setText(message.getBody());
    }

    @Override
    public void onClick(View v) {}
}
