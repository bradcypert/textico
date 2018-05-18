package com.bradcypert.textico.views.fragments

import android.app.Fragment
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bradcypert.textico.R
import android.provider.Telephony.Sms.getDefaultSmsPackage
import android.os.Build.VERSION_CODES.KITKAT
import android.os.Build.VERSION.SDK_INT
import android.provider.Telephony


class Tour2 : Fragment() {

    private var makeDefaultButton : Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(
                R.layout.fragment_tour2, container, false) as ViewGroup;

        makeDefaultButton = view.findViewById(R.id.make_default_button)

        makeDefaultButton!!.setOnClickListener {
            if (Telephony.Sms.getDefaultSmsPackage(activity.applicationContext) != activity.applicationContext.packageName) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        activity.applicationContext.packageName)
                startActivity(intent)
            }
        }

        return view
    }
}