package com.bradcypert.textico.views.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bradcypert.textico.R

class Tour1 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(
                R.layout.fragment_tour1, container, false) as ViewGroup
    }
}