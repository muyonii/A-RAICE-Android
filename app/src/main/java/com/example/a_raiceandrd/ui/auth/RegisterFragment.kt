package com.example.a_raiceandrd.ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.MunicipalityDto
import com.example.a_raiceandrd.data.repository.api.RegisterRequest
import com.example.a_raiceandrd.databinding.FragmentRegisterBinding
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.hideKeyboard
import com.example.a_raiceandrd.util.parseFieldErrors
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Fallback 12 Bataan municipalities (slug to display name)
    private val defaultMunicipalities = listOf(
        MunicipalityDto("Balanga", "balanga"),
        MunicipalityDto("Abucay", "abucay"),
        MunicipalityDto("Bagac", "bagac"),
        MunicipalityDto("Dinalupihan", "dinalupihan"),
        MunicipalityDto("Hermosa", "hermosa"),
        MunicipalityDto("Limay", "limay"),
        MunicipalityDto("Mariveles", "mariveles"),
        MunicipalityDto("Morong", "morong"),
        MunicipalityDto("Orani", "orani"),
        MunicipalityDto("Orion", "orion"),
        MunicipalityDto("Pilar", "pilar"),
        MunicipalityDto("Samal", "samal")
    )

    private var currentMunicipalities: List<MunicipalityDto> = defaultMunicipalities
    private var selectedSlug: String? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestLocation()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.Location_Permission_Denied,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupMunicipalityDropdown()
        loadMunicipalities()

        binding.btnUseLocation.setOnClickListener {
            checkAndRequestLocationPermission()
        }

        binding.createaccbtn.setOnClickListener {
            attemptRegister()
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupMunicipalityDropdown() {
        val names = currentMunicipalities.map { it.name.orEmpty() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.actvMunicipality.setAdapter(adapter)

        binding.actvMunicipality.setOnItemClickListener { _, _, position, _ ->
            selectedSlug = currentMunicipalities.getOrNull(position)?.slug
            binding.tilMunicipality.error = null
        }
    }

    private fun loadMunicipalities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) { ApiClient.service.getMunicipalities() }
                if (list.isNotEmpty()) {
                    currentMunicipalities = list
                    if (_binding != null) {
                        setupMunicipalityDropdown()
                    }
                }
            } catch (_: Exception) {
                // Keep default hardcoded list when offline
            }
        }
    }

    private fun checkAndRequestLocationPermission() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fineLocation) == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        } else {
            locationPermissionLauncher.launch(fineLocation)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null && _binding != null) {
                binding.etFarmLat.setText(String.format(Locale.US, "%.6f", loc.latitude))
                binding.etFarmLong.setText(String.format(Locale.US, "%.6f", loc.longitude))
                binding.tilFarmLat.error = null
                binding.tilFarmLong.error = null
                Toast.makeText(requireContext(), R.string.Location_Fetched, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.Location_Unavailable, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), R.string.Location_Unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptRegister() {
        clearFieldErrors()

        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty().lowercase()
        val contactNumber = binding.etContactNumber.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val passwordConfirm = binding.etPasswordConfirm.text?.toString().orEmpty()
        val barangay = binding.etBarangay.text?.toString()?.trim().orEmpty()
        val latStr = binding.etFarmLat.text?.toString()?.trim().orEmpty()
        val longStr = binding.etFarmLong.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (name.isBlank()) {
            binding.tilName.error = getString(R.string.Error_Name_Required)
            hasError = true
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.Error_Email_Invalid)
            hasError = true
        }

        if (contactNumber.isBlank()) {
            binding.tilContactNumber.error = getString(R.string.Error_Contact_Required)
            hasError = true
        }

        if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.Error_Password_Length)
            hasError = true
        }

        if (password != passwordConfirm) {
            binding.tilPasswordConfirm.error = getString(R.string.Error_Password_Mismatch)
            hasError = true
        }

        val slug = selectedSlug ?: run {
            val text = binding.actvMunicipality.text.toString().trim()
            currentMunicipalities.firstOrNull { it.name.equals(text, ignoreCase = true) }?.slug
        }

        if (slug.isNullOrBlank()) {
            binding.tilMunicipality.error = getString(R.string.Error_Municipality_Required)
            hasError = true
        }

        if (barangay.isBlank()) {
            binding.tilBarangay.error = getString(R.string.Error_Barangay_Required)
            hasError = true
        }

        val lat = latStr.toDoubleOrNull()
        if (lat == null || lat < 14.3 || lat > 14.95) {
            binding.tilFarmLat.error = getString(R.string.Error_Lat_Bounds)
            hasError = true
        }

        val lng = longStr.toDoubleOrNull()
        if (lng == null || lng < 120.2 || lng > 120.7) {
            binding.tilFarmLong.error = getString(R.string.Error_Long_Bounds)
            hasError = true
        }

        if (hasError) return

        hideKeyboard()
        setLoading(true)

        val request = RegisterRequest(
            name = name,
            email = email,
            password = password,
            passwordConfirmation = passwordConfirm,
            barangay = barangay,
            municipality = slug,
            contactNumber = contactNumber,
            farmLat = lat,
            farmLong = lng
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.service.register(request)
                }

                FLogin.saveSession(response.token, response.user)
                Toast.makeText(requireContext(), R.string.Register_Success, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_home)
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    val fieldErrors = parseFieldErrors(e)
                    applyFieldErrors(fieldErrors)
                } else {
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                if (_binding != null) {
                    setLoading(false)
                }
            }
        }
    }

    private fun applyFieldErrors(errors: Map<String, String>) {
        errors["name"]?.let { binding.tilName.error = it }
        errors["email"]?.let { binding.tilEmail.error = it }
        errors["password"]?.let { binding.tilPassword.error = it }
        errors["password_confirmation"]?.let { binding.tilPasswordConfirm.error = it }
        errors["contact_number"]?.let { binding.tilContactNumber.error = it }
        errors["municipality"]?.let { binding.tilMunicipality.error = it }
        errors["barangay"]?.let { binding.tilBarangay.error = it }
        errors["farm_lat"]?.let { binding.tilFarmLat.error = it }
        errors["farm_long"]?.let { binding.tilFarmLong.error = it }

        val mappedKeys = setOf("name", "email", "password", "password_confirmation", "contact_number", "municipality", "barangay", "farm_lat", "farm_long")
        val unmapped = errors.filterKeys { it !in mappedKeys }
        if (unmapped.isNotEmpty()) {
            Toast.makeText(
                requireContext(),
                unmapped.values.firstOrNull() ?: getString(R.string.Error_Validation_Failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun clearFieldErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilContactNumber.error = null
        binding.tilPassword.error = null
        binding.tilPasswordConfirm.error = null
        binding.tilMunicipality.error = null
        binding.tilBarangay.error = null
        binding.tilFarmLat.error = null
        binding.tilFarmLong.error = null
    }

    private fun setLoading(loading: Boolean) {
        _binding?.let { b ->
            b.registerProgress.isVisible = loading
            b.createaccbtn.isEnabled = !loading
            b.btnUseLocation.isEnabled = !loading
            b.btnBackToLogin.isEnabled = !loading
            b.etName.isEnabled = !loading
            b.etEmail.isEnabled = !loading
            b.etContactNumber.isEnabled = !loading
            b.etPassword.isEnabled = !loading
            b.etPasswordConfirm.isEnabled = !loading
            b.actvMunicipality.isEnabled = !loading
            b.etBarangay.isEnabled = !loading
            b.etFarmLat.isEnabled = !loading
            b.etFarmLong.isEnabled = !loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}