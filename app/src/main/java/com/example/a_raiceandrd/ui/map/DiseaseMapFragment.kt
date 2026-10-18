package com.example.a_raiceandrd.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.a_raiceandrd.R
import androidx.core.widget.NestedScrollView
import com.example.a_raiceandrd.databinding.FragmentDiseaseMapBinding
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav

class DiseaseMapFragment : Fragment(R.layout.fragment_disease_map) {

    private var _b: FragmentDiseaseMapBinding? = null

    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentDiseaseMapBinding.bind(view)

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.DMapScroll))
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _b = null
    }
}