package com.example.nunosrealtyapp.ui.agent

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nunosrealtyapp.databinding.ActivityListingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingsBinding
    private val viewModel: ListingsViewModel by viewModels()
    private lateinit var listingsAdapter: ListingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()

        viewModel.loadListings()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        listingsAdapter = com.example.nunosrealtyapp.ui.agent.ListingsAdapter { listing ->
            // Handle listing click
            val intent = Intent(this, AddListingActivity::class.java)
            intent.putExtra("listing_id", listing.id)
            startActivity(intent)
        }

        binding.listingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListingsActivity)
            adapter = listingsAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.addListingButton.setOnClickListener {
            startActivity(Intent(this, AddListingActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.listings.collect { listings ->
                listingsAdapter.submitList(listings)

                if (listings.isEmpty()) {
                    binding.emptyStateTextView.visibility = android.view.View.VISIBLE
                    binding.listingsRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyStateTextView.visibility = android.view.View.GONE
                    binding.listingsRecyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }
}