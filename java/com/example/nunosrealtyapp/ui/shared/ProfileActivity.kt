package com.example.nunosrealtyapp.ui.shared

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nunosrealtyapp.databinding.ActivityProfileBinding
import com.example.nunosrealtyapp.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private var profileImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            profileImageUri = selectedUri
            // Use Glide to load the image into CircleImageView
            com.bumptech.glide.Glide.with(this@ProfileActivity)
                .load(selectedUri)
                .into(binding.profileImageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()

        viewModel.loadUserProfile()
    }

    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupListeners() {
        binding.changePhotoButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.saveButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val companyName = binding.companyNameEditText.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.fullNameEditText.error = "Full name is required"
                return@setOnClickListener
            }

            viewModel.updateProfile(fullName, companyName, profileImageUri)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userProfile.collect { user ->
                user?.let { currentUser ->
                    binding.fullNameEditText.setText(currentUser.fullName)
                    binding.emailTextView.text = currentUser.email
                    binding.roleTextView.text = currentUser.role.replaceFirstChar { char -> char.uppercase() }

                    if (currentUser.role == "agent") {
                        binding.companyNameLayout.visibility = android.view.View.VISIBLE
                        binding.companyNameEditText.setText(currentUser.companyName ?: "")
                    } else {
                        binding.companyNameLayout.visibility = android.view.View.GONE
                    }

                    // Load profile image using Glide (for CircleImageView)
                    currentUser.profileImageUrl?.let { imageUrl ->
                        com.bumptech.glide.Glide.with(this@ProfileActivity)
                            .load(imageUrl)
                            .into(binding.profileImageView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is ProfileViewModel.UpdateState.Loading -> {
                        binding.saveButton.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is ProfileViewModel.UpdateState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is ProfileViewModel.UpdateState.Error -> {
                        binding.saveButton.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}