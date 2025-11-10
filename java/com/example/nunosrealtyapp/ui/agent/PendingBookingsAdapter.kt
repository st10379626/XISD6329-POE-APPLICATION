package com.example.nunosrealtyapp.ui.agent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nunosrealtyapp.data.model.Booking
import com.example.nunosrealtyapp.databinding.ItemPendingBookingBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PendingBookingsAdapter(
    private val onAccept: (String) -> Unit,
    private val onReject: (String) -> Unit,
    private val getCustomerName: suspend (String) -> String,
    private val coroutineScope: LifecycleCoroutineScope
) : ListAdapter<Booking, PendingBookingsAdapter.BookingViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemPendingBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemPendingBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            binding.bookingDate.text = sdf.format(booking.slotTime)

            // Load customer name asynchronously
            binding.customerName.text = "Loading..."
            coroutineScope.launch {
                try {
                    binding.customerName.text = getCustomerName(booking.customerId)
                } catch (e: Exception) {
                    binding.customerName.text = "Unknown"
                }
            }

            binding.acceptButton.setOnClickListener { onAccept(booking.id) }
            binding.rejectButton.setOnClickListener { onReject(booking.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
