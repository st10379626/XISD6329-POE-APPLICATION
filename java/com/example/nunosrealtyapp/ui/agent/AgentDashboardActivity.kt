package com.example.nunosrealtyapp.ui.agent

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nunosrealtyapp.databinding.ActivityAgentDashboardBinding
import com.example.nunosrealtyapp.ui.shared.ComplaintPortalActivity
import com.example.nunosrealtyapp.ui.shared.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var bookingsAdapter: PendingBookingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()

        // Load pending bookings
        val agentId = FirebaseAuth.getInstance().currentUser?.uid
        if (!agentId.isNullOrEmpty()) {
            viewModel.loadPendingBookings()
        }
    }

    private fun setupUI() {
        bookingsAdapter = PendingBookingsAdapter(
            onAccept = { id ->
                FirebaseAuth.getInstance().currentUser?.uid?.let { agentId ->
                    viewModel.acceptBooking(id, agentId)
                }
            },
            onReject = { id ->
                FirebaseAuth.getInstance().currentUser?.uid?.let { agentId ->
                    viewModel.rejectBooking(id, agentId)
                }
            },
            getCustomerName = { customerId ->
                viewModel.getCustomerName(customerId)
            },
            coroutineScope = lifecycleScope
        )

        binding.bookingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AgentDashboardActivity)
            adapter = bookingsAdapter
        }
    }

    private fun setupListeners() {
        binding.addListingButton.setOnClickListener {
            startActivity(Intent(this, AddListingActivity::class.java))
        }
        binding.viewListingsButton.setOnClickListener {
            startActivity(Intent(this, ListingsActivity::class.java))
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.nunosrealtyapp.R.id.nav_dashboard -> true
                com.example.nunosrealtyapp.R.id.nav_listings -> {
                    startActivity(Intent(this, ListingsActivity::class.java))
                    true
                }
                com.example.nunosrealtyapp.R.id.nav_complaints -> {
                    startActivity(Intent(this, ComplaintPortalActivity::class.java))
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
            viewModel.pendingBookings.collect { bookings ->
                bookingsAdapter.submitList(bookings)
                binding.emptyBookingsMessage.visibility =
                    if (bookings.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
}
