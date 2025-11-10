package com.example.nunosrealtyapp.ui.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nunosrealtyapp.databinding.ActivityBookingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private val viewModel: BookingViewModel by viewModels()
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val propertyId = intent.getStringExtra("property_id")
        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.setPropertyId(propertyId)
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup toolbar navigation instead of backButton
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set initial time to next available slot
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        selectedDate = calendar
        updateDateTimeUI()
    }

    private fun setupListeners() {
        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.selectTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.confirmBookingButton.setOnClickListener {
            val notes = binding.notesEditText.text.toString().trim()
            viewModel.createBooking(selectedDate.time, notes)
        }

        // Use toolbar navigation instead of backButton
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.bookingState.collect { state ->
                when (state) {
                    is BookingViewModel.BookingState.Loading -> {
                        binding.confirmBookingButton.isEnabled = false
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is BookingViewModel.BookingState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@BookingActivity, "Booking confirmed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is BookingViewModel.BookingState.Error -> {
                        binding.confirmBookingButton.isEnabled = true
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@BookingActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

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
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeUI()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to today
        datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                updateDateTimeUI()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun updateDateTimeUI() {
        val dateFormat = android.text.format.DateFormat.getMediumDateFormat(this)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(this)

        binding.dateTextView.text = dateFormat.format(selectedDate.time)
        binding.timeTextView.text = timeFormat.format(selectedDate.time)
    }
}