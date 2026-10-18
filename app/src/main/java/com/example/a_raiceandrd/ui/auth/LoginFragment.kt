package com.example.a_raiceandrd.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.databinding.FragmentLoginBinding
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.hideKeyboard
import com.example.a_raiceandrd.util.parseFieldErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If a session exists from persistent storage, validate it with GET /user
        if (FLogin.hasSession()) {
            checkExistingSession()
        }

        binding.loginbtn.setOnClickListener {
            attemptLogin()
        }

        binding.registerbtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun checkExistingSession() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { ApiClient.service.getUser() }
                FLogin.updateCurrentUser(user)
                val role = FLogin.getCurrentRole()
                navigateToRoleDestination(role)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    FLogin.clearSessionLocally()
                    setLoading(false)
                } else {
                    // Non-401 server error: keep session and proceed
                    navigateToRoleDestination(FLogin.getCurrentRole())
                }
            } catch (_: IOException) {
                // Offline / network failure: keep the saved session and enter app
                navigateToRoleDestination(FLogin.getCurrentRole())
            } catch (_: Exception) {
                navigateToRoleDestination(FLogin.getCurrentRole())
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.email.text?.toString()?.trim().orEmpty()
        val password = binding.password.text?.toString().orEmpty()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var hasError = false
        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.Error_Email_Invalid)
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.Error_Email_Invalid)
            hasError = true
        }

        if (password.isBlank()) {
            binding.tilPassword.error = getString(R.string.Pass_hint)
            hasError = true
        }

        if (hasError) return

        hideKeyboard()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val role = withContext(Dispatchers.IO) {
                    FLogin.login(email, password)
                }
                navigateToRoleDestination(role)
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    val fieldErrors = parseFieldErrors(e)
                    fieldErrors["email"]?.let { binding.tilEmail.error = it }
                    fieldErrors["password"]?.let { binding.tilPassword.error = it }
                    if (binding.tilEmail.error == null && binding.tilPassword.error == null) {
                        Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                if (_binding != null) {
                    setLoading(false)
                }
            }
        }
    }

    private fun navigateToRoleDestination(role: Role?) {
        when (role) {
            Role.FARMER -> findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            Role.ADMIN -> findNavController().navigate(R.id.action_loginFragment_to_adHomeFragment)
            null -> {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        _binding?.let { b ->
            b.loginProgress.isVisible = loading
            b.loginbtn.isEnabled = !loading
            b.registerbtn.isEnabled = !loading
            b.email.isEnabled = !loading
            b.password.isEnabled = !loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}