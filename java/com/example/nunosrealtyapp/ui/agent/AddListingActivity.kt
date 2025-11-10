package com.example.nunosrealtyapp.ui.agent

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nunosrealtyapp.databinding.ActivityAddListingBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddListingBinding
    private val viewModel: AddListingViewModel by viewModels()
    private val imageUris = mutableListOf<Uri>()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            if (imageUris.size + it.size > 10) {
                Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            imageUris.addAll(it)
            updateImagePreview()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupUI() {
        val provinces = arrayOf(
            "Gauteng", "Western Cape", "KwaZulu-Natal", "Eastern Cape",
            "Limpopo", "Mpumalanga", "North West", "Free State", "Northern Cape"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinces)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.provinceSpinner.adapter = adapter

        val types = arrayOf("House", "Apartment", "Townhouse", "Land", "Commercial")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeSpinner.adapter = typeAdapter
    }

    private fun setupListeners() {
        binding.addImagesButton.setOnClickListener { imagePicker.launch("image/*") }

        binding.forSaleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.priceEditText.hint = "Price (R millions)"
        }

        binding.forRentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.priceEditText.hint = "Monthly Rent (R)"
        }

        binding.submitButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                Toast.makeText(this, "You must be logged in to create a listing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val price = binding.priceEditText.text.toString().trim().toDouble()
            val isForSale = binding.forSaleCheckbox.isChecked
            val isForRent = binding.forRentCheckbox.isChecked
            val city = binding.cityEditText.text.toString().trim()
            val province = binding.provinceSpinner.selectedItem.toString()
            val address = binding.addressEditText.text.toString().trim()
            val beds = binding.bedsEditText.text.toString().trim().toIntOrNull() ?: 0
            val baths = binding.bathsEditText.text.toString().trim().toIntOrNull() ?: 0
            val area = binding.areaEditText.text.toString().trim().toIntOrNull() ?: 0

            // Auto-fill coordinates if empty or invalid
            var latitude = binding.latitudeEditText.text.toString().trim().toDoubleOrNull()
            var longitude = binding.longitudeEditText.text.toString().trim().toDoubleOrNull()
            if (latitude == null || longitude == null || latitude == 0.0 || longitude == 0.0) {
                val coords = getCityCoordinates(city)
                latitude = coords.first
                longitude = coords.second
            }

            viewModel.createListing(
                title = title,
                description = description,
                price = price,
                isForSale = isForSale,
                isForRent = isForRent,
                city = city,
                province = province,
                address = address,
                latitude = latitude,
                longitude = longitude,
                beds = beds,
                baths = baths,
                area = area,
                images = imageUris,
                createdBy = currentUserId
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uploadState.collect { state ->
                when (state) {
                    is AddListingViewModel.UploadState.Loading -> {
                        binding.submitButton.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is AddListingViewModel.UploadState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@AddListingActivity, "Listing created successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is AddListingViewModel.UploadState.Error -> {
                        binding.submitButton.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@AddListingActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.titleEditText.text.isNullOrEmpty()) {
            binding.titleEditText.error = "Title is required"
            return false
        }
        if (binding.priceEditText.text.isNullOrEmpty()) {
            binding.priceEditText.error = "Price is required"
            return false
        }
        if (!binding.forSaleCheckbox.isChecked && !binding.forRentCheckbox.isChecked) {
            Toast.makeText(this, "Select at least one: For Sale or For Rent", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.cityEditText.text.isNullOrEmpty()) {
            binding.cityEditText.error = "City is required"
            return false
        }
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateImagePreview() {
        binding.imagesCountTextView.text = "${imageUris.size} images selected"
    }

    // Auto-fill city coordinates
    private fun getCityCoordinates(city: String): Pair<Double, Double> {
        return when (city.lowercase()) {
            "sandton" -> Pair(-26.1076, 28.0567)
            "johannesburg" -> Pair(-26.2041, 28.0473)
            "cape town" -> Pair(-33.9249, 18.4241)
            else -> Pair(-26.1076, 28.0567) // default to Sandton
        }
    }
}
