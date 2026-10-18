package com.example.a_raiceandrd.ui.encyclopedia

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.DiseaseAdapter
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.databinding.FragmentEncyListBinding
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav

class EncyListFragment : Fragment(R.layout.fragment_ency_list) {

    private var _binding: FragmentEncyListBinding? = null
    private val binding get() = _binding!!

    private val adapter = DiseaseAdapter { disease ->
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.ency) {
            nav.navigate(
                R.id.action_ency_to_encyDetailsFragment,
                bundleOf("diseaseId" to disease.id)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEncyListBinding.bind(view)

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.encyScroll))

        binding.rvEncyclopedia.adapter = adapter
        adapter.submitList(DiseaseRepo.getAll())
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _binding = null
    }
}