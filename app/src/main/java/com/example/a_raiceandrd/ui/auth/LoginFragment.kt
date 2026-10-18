package com.example.a_raiceandrd.ui.auth

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.loginbtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString()

            binding.loginbtn.isEnabled = false          // stop double-taps while waiting
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    when (FLogin.login(email, password)) {
                        Role.FARMER -> findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        Role.ADMIN -> findNavController().navigate(R.id.action_loginFragment_to_adHomeFragment)
                        else -> {}
                    }
                } catch (e: HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    Toast.makeText(requireContext(), "Error ${e.code()}: $body", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.loginbtn.isEnabled = true
                }
            }
        }

        binding.registerbtn.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return binding.root
    }
}