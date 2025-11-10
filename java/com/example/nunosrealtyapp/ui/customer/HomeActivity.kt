package com.example.nunosrealtyapp.ui.customer

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nunosrealtyapp.data.seed.Seeder
import com.example.nunosrealtyapp.databinding.ActivityHomeBinding
import com.example.nunosrealtyapp.ui.customer.adapters.PropertyAdapter
import com.example.nunosrealtyapp.ui.shared.MapActivity
import com.example.nunosrealtyapp.ui.shared.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // Add this annotation
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var propertyAdapter: PropertyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupUI()
        setupListeners()
        observeViewModel()

        // Load initial properties
        viewModel.loadProperties()
    }


    private fun setupUI() {
        // Setup location text
        binding.locationTextView.text = "Sandton, Gauteng"

        // Setup property recycler view
        propertyAdapter = PropertyAdapter { property ->
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property_id", property.id)
            startActivity(intent)
        }

        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = propertyAdapter
        }

        // Setup search view
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchProperties(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchProperties(it) }
                return true
            }
        })
    }

    private fun setupListeners() {
        binding.buyButton.setOnClickListener {
            viewModel.filterProperties(isForSale = true, isForRent = false)
        }

        binding.rentButton.setOnClickListener {
            viewModel.filterProperties(isForSale = false, isForRent = true)
        }

        binding.nearbyButton.setOnClickListener {
            viewModel.filterByLocation()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.nunosrealtyapp.R.id.nav_home -> {
                    // Already on home
                    true
                }
                com.example.nunosrealtyapp.R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                com.example.nunosrealtyapp.R.id.nav_bookings -> {
                    startActivity(Intent(this, BookingsListActivity::class.java))
                    true
                }
                com.example.nunosrealtyapp.R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.properties.collect { properties ->
                propertyAdapter.submitList(properties)

                if (properties.isEmpty()) {
                    binding.emptyStateTextView.visibility = android.view.View.VISIBLE
                    binding.propertiesRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyStateTextView.visibility = android.view.View.GONE
                    binding.propertiesRecyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = android.view.View.VISIBLE
                    binding.propertiesRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = android.view.View.GONE
                    binding.propertiesRecyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}