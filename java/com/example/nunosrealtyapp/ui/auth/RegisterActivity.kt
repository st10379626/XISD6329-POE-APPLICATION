package com.example.nunosrealtyapp.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nunosrealtyapp.databinding.ActivityRegisterBinding
import com.example.nunosrealtyapp.ui.agent.AgentDashboardActivity
import com.example.nunosrealtyapp.ui.customer.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private var companyDocUri: Uri? = null

    private val documentPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            companyDocUri = it
            val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "Document selected"
            binding.companyDocTextView.text = fileName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Customer", "Agent")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter
    }

    private fun setupListeners() {
        // Show/hide company fields for agents
        binding.roleSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val isAgent = position == 1
                binding.companyNameLayout.visibility = if (isAgent) android.view.View.VISIBLE else android.view.View.GONE
                binding.companyDocLayout.visibility = if (isAgent) android.view.View.VISIBLE else android.view.View.GONE
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Pick company document
        binding.selectDocButton.setOnClickListener {
            documentPicker.launch("application/pdf")
        }

        // Create account
        binding.createAccountButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val role = if (binding.roleSpinner.selectedItemPosition == 1) "agent" else "customer"
            val companyName = binding.companyNameEditText.text.toString().trim()

            // Basic validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Agent validation
            if (role == "agent") {
                if (companyName.isEmpty()) {
                    Toast.makeText(this, "Company name is required for agents", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (companyDocUri == null) {
                    Toast.makeText(this, "Please select a company document", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Register with ViewModel
            viewModel.register(fullName, email, password, role, companyName, companyDocUri)
        }

        // Navigate to login
        binding.signInTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is AuthViewModel.RegisterState.Loading -> {
                        binding.createAccountButton.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is AuthViewModel.RegisterState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        navigateToHome(state.user)
                    }
                    is AuthViewModel.RegisterState.Error -> {
                        binding.createAccountButton.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToHome(user: com.example.nunosrealtyapp.data.model.User) {
        val intent = if (user.role == "agent") {
            Intent(this, AgentDashboardActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
