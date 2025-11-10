package com.example.nunosrealtyapp.ui.customer


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.nunosrealtyapp.databinding.ActivityPropertyDetailBinding
import com.example.nunosrealtyapp.ui.customer.adapters.PropertyImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PropertyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPropertyDetailBinding
    private val viewModel: PropertyViewModel by viewModels()
    private lateinit var imageAdapter: PropertyImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val propertyId = intent.getStringExtra("property_id")
        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupListeners()
        observeViewModel()

        viewModel.loadProperty(propertyId)
    }

    private fun setupUI() {
        // Setup image view pager
        imageAdapter = PropertyImageAdapter(emptyList())
        binding.imagesViewPager.adapter = imageAdapter

        // Add custom dots indicator for ViewPager2
        binding.dotsIndicator.setViewPager2(binding.imagesViewPager)

        binding.imagesViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.imageCounterTextView.text = "${position + 1}/${imageAdapter.itemCount}"
            }
        })
    }

    private fun setupListeners() {
        binding.bookNowButton.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("property_id", viewModel.property.value?.id)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.property.collect { property ->
                property?.let {
                    binding.propertyTitleTextView.text = it.title
                    binding.propertyPriceTextView.text = if (it.isForSale) {
                        "R${it.price}M"
                    } else {
                        "R${it.price}/m"
                    }
                    binding.propertyLocationTextView.text = "${it.city}, ${it.province}"
                    binding.bedsTextView.text = it.beds.toString()
                    binding.bathsTextView.text = it.baths.toString()
                    binding.areaTextView.text = "${it.areaSqft} sqft"
                    binding.descriptionTextView.text = it.description

                    // Load images
                    imageAdapter = PropertyImageAdapter(it.images)
                    binding.imagesViewPager.adapter = imageAdapter

                    // Update dots indicator
                    binding.dotsIndicator.setViewPager2(binding.imagesViewPager)

                    binding.imageCounterTextView.text = "1/${it.images.size}"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.contentLayout.visibility = android.view.View.GONE
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.contentLayout.visibility = android.view.View.VISIBLE
                }
            }
        }


        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.contentLayout.visibility = android.view.View.GONE
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.contentLayout.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}