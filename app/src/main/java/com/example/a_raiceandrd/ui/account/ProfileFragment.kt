package com.example.a_raiceandrd.ui.account

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ChangePasswordRequest
import com.example.a_raiceandrd.data.repository.api.MunicipalityDto
import com.example.a_raiceandrd.data.repository.api.UpdateUserRequest
import com.example.a_raiceandrd.data.repository.api.UserDto
import com.example.a_raiceandrd.databinding.DialogChangePasswordBinding
import com.example.a_raiceandrd.databinding.DialogEditProfileBinding
import com.example.a_raiceandrd.databinding.FragmentProfileBinding
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
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

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUser: UserDto? = null

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

    private var dialogBindingForLocation: DialogEditProfileBinding? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestLocationForDialog()
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        bindInitialUser()
        loadUserProfile()

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMyScans.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_scanHistoryFragment)
        }

        binding.editPorfileBtn.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.lgoutBtn.setOnClickListener {
            FLogin.logout()
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    private fun bindInitialUser() {
        val isAdmin = FLogin.getCurrentRole() == Role.ADMIN
        val name = FLogin.getCurrentName() ?: getString(R.string.Not_Set)
        val email = FLogin.getCurrentEmail() ?: getString(R.string.Not_Set)

        binding.profileName.text = name
        binding.ProfileEmail.text = email
        binding.profileInitials.text = initialsOf(name)

        binding.profileRoleChip.setText(if (isAdmin) R.string.Role_Admin else R.string.Role_Farmer)
        val chipColor = ContextCompat.getColor(
            requireContext(), if (isAdmin) R.color.sky else R.color.ok_green
        )
        binding.profileRoleChip.backgroundTintList = ColorStateList.valueOf(chipColor)

        binding.detailsSection.isVisible = !isAdmin
        binding.detailMunicipalityValue.setText(R.string.Not_Set)
        binding.detailBarangayValue.setText(R.string.Not_Set)
        binding.detailContactValue.setText(R.string.Not_Set)
        binding.detailCoordinatesValue.setText(R.string.Not_Set)
    }

    private fun loadUserProfile() {
        binding.profileProgress.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { ApiClient.service.getUser() }
                currentUser = user
                FLogin.updateCurrentUser(user)
                if (_binding != null) {
                    renderUser(user)
                }
            } catch (e: Exception) {
                // If network fails, keep showing cached session
            } finally {
                if (_binding != null) {
                    binding.profileProgress.isVisible = false
                }
            }
        }
    }

    private fun renderUser(user: UserDto) {
        val role = FLogin.mapRole(user.role) ?: FLogin.getCurrentRole()
        val isAdmin = role == Role.ADMIN
        val name = user.name ?: FLogin.getCurrentName() ?: getString(R.string.Not_Set)
        val email = user.email ?: FLogin.getCurrentEmail() ?: getString(R.string.Not_Set)

        binding.profileName.text = name
        binding.ProfileEmail.text = email
        binding.profileInitials.text = initialsOf(name)

        binding.profileRoleChip.setText(if (isAdmin) R.string.Role_Admin else R.string.Role_Farmer)
        val chipColor = ContextCompat.getColor(
            requireContext(), if (isAdmin) R.color.sky else R.color.ok_green
        )
        binding.profileRoleChip.backgroundTintList = ColorStateList.valueOf(chipColor)

        if (isAdmin) {
            binding.detailsSection.isVisible = false
        } else {
            binding.detailsSection.isVisible = true
            val profile = user.farmerProfile
            binding.detailMunicipalityValue.text = profile?.municipality?.replaceFirstChar { it.uppercase() }
                ?: getString(R.string.Not_Set)
            binding.detailBarangayValue.text = profile?.barangay ?: getString(R.string.Not_Set)
            binding.detailContactValue.text = profile?.contactNumber ?: getString(R.string.Not_Set)

            val lat = profile?.farmLat
            val lng = profile?.farmLong
            binding.detailCoordinatesValue.text = if (lat != null && lng != null) {
                String.format(Locale.US, "%.4f, %.4f", lat, lng)
            } else {
                getString(R.string.Not_Set)
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val dBinding = DialogEditProfileBinding.bind(dialogView)
        dialogBindingForLocation = dBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val user = currentUser
        val isAdmin = FLogin.getCurrentRole() == Role.ADMIN

        dBinding.etEditName.setText(user?.name ?: FLogin.getCurrentName().orEmpty())
        dBinding.etEditEmail.setText(user?.email ?: FLogin.getCurrentEmail().orEmpty())

        dBinding.editFarmerSection.isVisible = !isAdmin

        var selectedSlug = user?.farmerProfile?.municipality
        if (!isAdmin) {
            val profile = user?.farmerProfile
            dBinding.etEditContact.setText(profile?.contactNumber.orEmpty())
            dBinding.etEditBarangay.setText(profile?.barangay.orEmpty())
            profile?.farmLat?.let { dBinding.etEditLat.setText(String.format(Locale.US, "%.6f", it)) }
            profile?.farmLong?.let { dBinding.etEditLong.setText(String.format(Locale.US, "%.6f", it)) }

            val names = defaultMunicipalities.map { it.name.orEmpty() }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            dBinding.actvEditMunicipality.setAdapter(adapter)

            val currentMunicipalityName = defaultMunicipalities.firstOrNull {
                it.slug.equals(profile?.municipality, ignoreCase = true)
            }?.name ?: profile?.municipality?.replaceFirstChar { it.uppercase() }.orEmpty()
            dBinding.actvEditMunicipality.setText(currentMunicipalityName, false)

            dBinding.actvEditMunicipality.setOnItemClickListener { _, _, position, _ ->
                selectedSlug = defaultMunicipalities.getOrNull(position)?.slug
                dBinding.tilEditMunicipality.error = null
            }

            dBinding.btnEditUseLocation.setOnClickListener {
                checkAndRequestLocationForDialog()
            }
        }

        dBinding.btnCancelEdit.setOnClickListener { dialog.dismiss() }

        dBinding.btnSaveProfile.setOnClickListener {
            dBinding.tilEditName.error = null
            dBinding.tilEditEmail.error = null
            dBinding.tilEditContact.error = null
            dBinding.tilEditBarangay.error = null
            dBinding.tilEditMunicipality.error = null
            dBinding.tilEditLat.error = null
            dBinding.tilEditLong.error = null

            val name = dBinding.etEditName.text?.toString()?.trim().orEmpty()
            val email = dBinding.etEditEmail.text?.toString()?.trim().orEmpty().lowercase()

            var hasError = false
            if (name.isBlank()) {
                dBinding.tilEditName.error = getString(R.string.Error_Name_Required)
                hasError = true
            }
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dBinding.tilEditEmail.error = getString(R.string.Error_Email_Invalid)
                hasError = true
            }

            val contact = dBinding.etEditContact.text?.toString()?.trim().orEmpty()
            val barangay = dBinding.etEditBarangay.text?.toString()?.trim().orEmpty()
            val latStr = dBinding.etEditLat.text?.toString()?.trim().orEmpty()
            val longStr = dBinding.etEditLong.text?.toString()?.trim().orEmpty()

            var lat: Double? = null
            var lng: Double? = null
            var slug = selectedSlug

            if (!isAdmin) {
                if (slug.isNullOrBlank()) {
                    val text = dBinding.actvEditMunicipality.text.toString().trim()
                    slug = defaultMunicipalities.firstOrNull { it.name.equals(text, ignoreCase = true) }?.slug
                }

                if (latStr.isNotBlank()) {
                    lat = latStr.toDoubleOrNull()
                    if (lat == null || lat < 14.3 || lat > 14.95) {
                        dBinding.tilEditLat.error = getString(R.string.Error_Lat_Bounds)
                        hasError = true
                    }
                }

                if (longStr.isNotBlank()) {
                    lng = longStr.toDoubleOrNull()
                    if (lng == null || lng < 120.2 || lng > 120.7) {
                        dBinding.tilEditLong.error = getString(R.string.Error_Long_Bounds)
                        hasError = true
                    }
                }
            }

            if (hasError) return@setOnClickListener

            dialogView.hideKeyboard()
            dBinding.editProfileProgress.isVisible = true
            dBinding.btnSaveProfile.isEnabled = false
            dBinding.btnCancelEdit.isEnabled = false

            val request = UpdateUserRequest(
                name = name,
                email = email,
                barangay = if (isAdmin) null else barangay.ifBlank { null },
                municipality = if (isAdmin) null else slug,
                contactNumber = if (isAdmin) null else contact.ifBlank { null },
                farmLat = if (isAdmin) null else lat,
                farmLong = if (isAdmin) null else lng
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val updatedUser = withContext(Dispatchers.IO) {
                        ApiClient.service.updateUser(request)
                    }
                    currentUser = updatedUser
                    FLogin.updateCurrentUser(updatedUser)
                    (activity as? MainActivity)?.refreshDrawer()
                    if (_binding != null) {
                        renderUser(updatedUser)
                    }
                    Toast.makeText(requireContext(), R.string.Profile_Updated, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: HttpException) {
                    if (e.code() == 422) {
                        val fieldErrors = parseFieldErrors(e)
                        fieldErrors["name"]?.let { dBinding.tilEditName.error = it }
                        fieldErrors["email"]?.let { dBinding.tilEditEmail.error = it }
                        fieldErrors["contact_number"]?.let { dBinding.tilEditContact.error = it }
                        fieldErrors["municipality"]?.let { dBinding.tilEditMunicipality.error = it }
                        fieldErrors["barangay"]?.let { dBinding.tilEditBarangay.error = it }
                        fieldErrors["farm_lat"]?.let { dBinding.tilEditLat.error = it }
                        fieldErrors["farm_long"]?.let { dBinding.tilEditLong.error = it }

                        val mappedKeys = setOf("name", "email", "contact_number", "municipality", "barangay", "farm_lat", "farm_long")
                        val unmapped = fieldErrors.filterKeys { it !in mappedKeys }
                        if (unmapped.isNotEmpty()) {
                            Toast.makeText(requireContext(), unmapped.values.firstOrNull() ?: getString(R.string.Error_Validation_Failed), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                } finally {
                    dBinding.editProfileProgress.isVisible = false
                    dBinding.btnSaveProfile.isEnabled = true
                    dBinding.btnCancelEdit.isEnabled = true
                }
            }
        }

        dialog.show()
    }

    private fun checkAndRequestLocationForDialog() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fineLocation) == PackageManager.PERMISSION_GRANTED) {
            requestLocationForDialog()
        } else {
            locationPermissionLauncher.launch(fineLocation)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationForDialog() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            dialogBindingForLocation?.let { d ->
                if (loc != null) {
                    d.etEditLat.setText(String.format(Locale.US, "%.6f", loc.latitude))
                    d.etEditLong.setText(String.format(Locale.US, "%.6f", loc.longitude))
                    d.tilEditLat.error = null
                    d.tilEditLong.error = null
                    Toast.makeText(requireContext(), R.string.Location_Fetched, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.Location_Unavailable, Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), R.string.Location_Unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val dBinding = DialogChangePasswordBinding.bind(dialogView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dBinding.btnCancelPassword.setOnClickListener { dialog.dismiss() }

        dBinding.btnSavePassword.setOnClickListener {
            dBinding.tilCurrentPassword.error = null
            dBinding.tilNewPassword.error = null
            dBinding.tilConfirmNewPassword.error = null

            val currentPassword = dBinding.etCurrentPassword.text?.toString().orEmpty()
            val newPassword = dBinding.etNewPassword.text?.toString().orEmpty()
            val confirmNewPassword = dBinding.etConfirmNewPassword.text?.toString().orEmpty()

            var hasError = false
            if (currentPassword.isBlank()) {
                dBinding.tilCurrentPassword.error = getString(R.string.Current_Password_hint)
                hasError = true
            }
            if (newPassword.length < 8) {
                dBinding.tilNewPassword.error = getString(R.string.Error_Password_Length)
                hasError = true
            }
            if (newPassword != confirmNewPassword) {
                dBinding.tilConfirmNewPassword.error = getString(R.string.Error_Password_Mismatch)
                hasError = true
            }

            if (hasError) return@setOnClickListener

            dialogView.hideKeyboard()
            dBinding.changePasswordProgress.isVisible = true
            dBinding.btnSavePassword.isEnabled = false
            dBinding.btnCancelPassword.isEnabled = false

            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                password = newPassword,
                passwordConfirmation = confirmNewPassword
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        ApiClient.service.changePassword(request)
                    }
                    Toast.makeText(requireContext(), R.string.Password_Updated, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: HttpException) {
                    if (e.code() == 422) {
                        val fieldErrors = parseFieldErrors(e)
                        fieldErrors["current_password"]?.let { dBinding.tilCurrentPassword.error = it }
                        fieldErrors["password"]?.let { dBinding.tilNewPassword.error = it }
                        fieldErrors["password_confirmation"]?.let { dBinding.tilConfirmNewPassword.error = it }

                        val mappedKeys = setOf("current_password", "password", "password_confirmation")
                        val unmapped = fieldErrors.filterKeys { it !in mappedKeys }
                        if (unmapped.isNotEmpty()) {
                            Toast.makeText(requireContext(), unmapped.values.firstOrNull() ?: getString(R.string.Error_Validation_Failed), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                } finally {
                    dBinding.changePasswordProgress.isVisible = false
                    dBinding.btnSavePassword.isEnabled = true
                    dBinding.btnCancelPassword.isEnabled = true
                }
            }
        }

        dialog.show()
    }

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogBindingForLocation = null
        _binding = null
    }
}