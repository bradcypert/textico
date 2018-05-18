package com.bradcypert.textico.views.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bradcypert.textico.R

class Tour3 : Fragment() {

    interface OnDBBuiltListener {
        fun onDBBuilt()
    }

    private var setupDbButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(
                R.layout.fragment_tour3, container, false) as ViewGroup

        setupDbButton = view.findViewById(R.id.setup_db_button)
        setupDbButton!!.setOnClickListener {
            val a = parentFragment as OnDBBuiltListener
            a.onDBBuilt()
        }
        return view
    }
}