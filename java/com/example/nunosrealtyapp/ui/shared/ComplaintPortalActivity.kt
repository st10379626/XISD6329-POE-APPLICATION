package com.example.nunosrealtyapp.ui.shared

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nunosrealtyapp.databinding.ActivityComplaintPortalBinding
import com.example.nunosrealtyapp.databinding.DialogAddComplaintBinding
import com.example.nunosrealtyapp.ui.shared.adapters.ComplaintsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComplaintPortalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintPortalBinding
    private val viewModel: ComplaintViewModel by viewModels()
    private lateinit var complaintsAdapter: ComplaintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintPortalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()

        viewModel.loadComplaints()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        complaintsAdapter = ComplaintsAdapter()
        binding.complaintsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ComplaintPortalActivity)
            adapter = complaintsAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.addComplaintButton.setOnClickListener {
            showAddComplaintDialog()
        }
    }

    private fun showAddComplaintDialog() {
        val inflater = layoutInflater
        val dialogBinding = DialogAddComplaintBinding.inflate(inflater)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("New Complaint")
            .setView(dialogBinding.root)
            .setPositiveButton("Submit", null) // We’ll override click below
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val submitButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            submitButton.setOnClickListener {
                val subject = dialogBinding.subjectEditText.text.toString().trim()
                val message = dialogBinding.messageEditText.text.toString().trim()

                if (subject.isEmpty() || message.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    submitComplaint(subject, message) {
                        dialog.dismiss() // Close dialog on success
                        dialogBinding.subjectEditText.text?.clear()
                        dialogBinding.messageEditText.text?.clear()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun submitComplaint(subject: String, message: String, onSuccess: () -> Unit) {
        binding.progressBar.visibility = View.VISIBLE
        binding.addComplaintButton.isEnabled = false

        lifecycleScope.launch {
            try {
                viewModel.submitComplaint(subject, message)
                Toast.makeText(this@ComplaintPortalActivity, "Complaint submitted successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
                // Scroll to newest complaint
                binding.complaintsRecyclerView.scrollToPosition(0)
            } catch (e: Exception) {
                Toast.makeText(this@ComplaintPortalActivity, "Failed to submit complaint: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.addComplaintButton.isEnabled = true
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.complaints.collect { complaints ->
                complaintsAdapter.submitList(complaints.reversed()) // Newest on top
                binding.complaintsRecyclerView.visibility =
                    if (complaints.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
}
