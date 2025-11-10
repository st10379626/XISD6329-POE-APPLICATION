package com.example.nunosrealtyapp.ui.customer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nunosrealtyapp.databinding.ActivityBookingsListBinding
import com.example.nunosrealtyapp.ui.customer.adapters.BookingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingsListBinding
    private val viewModel: BookingViewModel by viewModels() // Use BookingViewModel, not BookingsViewModel
    private lateinit var bookingsAdapter: BookingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()

        viewModel.loadBookings() // This should work now
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        bookingsAdapter = BookingsAdapter()
        binding.bookingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookingsListActivity)
            adapter = bookingsAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.bookings.collect { bookings ->
                bookingsAdapter.submitList(bookings)

                if (bookings.isEmpty()) {
                    binding.emptyStateTextView.visibility = android.view.View.VISIBLE
                    binding.bookingsRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.emptyStateTextView.visibility = android.view.View.GONE
                    binding.bookingsRecyclerView.visibility = android.view.View.VISIBLE
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