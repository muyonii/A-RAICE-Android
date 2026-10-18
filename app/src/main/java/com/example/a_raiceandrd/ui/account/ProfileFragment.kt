package com.example.a_raiceandrd.ui.account

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.FragmentProfileBinding
import com.example.a_raiceandrd.ui.auth.Role
import com.example.a_raiceandrd.ui.auth.FLogin


//para mag-decide kung aling sectoins ang makikita
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdmin = FLogin.getCurrentRole() == Role.ADMIN
        val name = FLogin.getCurrentName() ?: getString(R.string.Not_Set)
        val email = FLogin.getCurrentEmail() ?: getString(R.string.Not_Set)

        binding.profileName.text = name
        binding.ProfileEmail.text = email
        binding.profileInitials.text = initialsOf(name)

        //color of chip depends sa role
        binding.profileRoleChip.setText(if (isAdmin) R.string.Role_Admin else R.string.Role_Farmer)
        val chipColor = ContextCompat.getColor(
            requireContext(), if (isAdmin) R.color.sky else R.color.ok_green
        )
        binding.profileRoleChip.backgroundTintList = ColorStateList.valueOf(chipColor)

        //FARMER-ONLY DETAILSSSS. TODO: fill galing sa regis kapag may backend na
        binding.detailsSection.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.detailMunicipalityValue.setText(R.string.Not_Set)
        binding.detailBarangayValue.setText(R.string.Not_Set)

        binding.editPorfileBtn.setOnClickListener {
            //TODO: navigate to edit profile fragment
            Toast.makeText(requireContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.lgoutBtn.setOnClickListener {
            FLogin.logout()
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    //initials ngani Alleah Jane to AJ
    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}