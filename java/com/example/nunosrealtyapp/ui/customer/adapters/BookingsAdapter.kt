package com.example.nunosrealtyapp.ui.customer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nunosrealtyapp.databinding.ItemBookingBinding
import com.example.nunosrealtyapp.data.model.Booking
import java.text.SimpleDateFormat
import java.util.Locale

class BookingsAdapter : ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = getItem(position)
        holder.bind(booking)
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.bookingPropertyTextView.text = "Booking #${booking.id.take(8)}"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            binding.bookingDateTextView.text = dateFormat.format(booking.slotTime)
            binding.bookingTimeTextView.text = timeFormat.format(booking.slotTime)

            binding.bookingStatusTextView.text = booking.status.replaceFirstChar { it.uppercase() }

            // Set background based on status
            val backgroundRes = when (booking.status.lowercase()) {
                "confirmed" -> com.example.nunosrealtyapp.R.drawable.bg_status_approved
                "cancelled" -> com.example.nunosrealtyapp.R.drawable.bg_status_rejected
                else -> com.example.nunosrealtyapp.R.drawable.bg_status_pending
            }
            binding.bookingStatusTextView.setBackgroundResource(backgroundRes)
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}