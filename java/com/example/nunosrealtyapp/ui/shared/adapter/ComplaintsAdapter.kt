package com.example.nunosrealtyapp.ui.shared.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nunosrealtyapp.data.model.Complaint
import com.example.nunosrealtyapp.databinding.ItemComplaintBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ComplaintsAdapter : ListAdapter<Complaint, ComplaintsAdapter.ComplaintViewHolder>(ComplaintDiffCallback()) {

    private var lastAnimatedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComplaintViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = getItem(position)
        holder.bind(complaint)

        val currentPos = holder.adapterPosition
        if (currentPos != RecyclerView.NO_POSITION && currentPos > lastAnimatedPosition) {
            holder.itemView.alpha = 0f
            holder.itemView.translationY = 100f
            holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .start()
            lastAnimatedPosition = currentPos
        }
    }


    inner class ComplaintViewHolder(private val binding: ItemComplaintBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(complaint: Complaint) {
            binding.complaintSubjectTextView.text = complaint.subject
            binding.complaintMessageTextView.text = complaint.message

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.complaintDateTextView.text = dateFormat.format(complaint.createdAt)

            binding.complaintStatusTextView.text = complaint.status.replaceFirstChar { it.uppercase() }

            val backgroundRes = when (complaint.status.lowercase()) {
                "resolved" -> com.example.nunosrealtyapp.R.drawable.bg_status_approved
                "rejected" -> com.example.nunosrealtyapp.R.drawable.bg_status_rejected
                else -> com.example.nunosrealtyapp.R.drawable.bg_status_pending
            }
            binding.complaintStatusTextView.setBackgroundResource(backgroundRes)
        }
    }

    class ComplaintDiffCallback : DiffUtil.ItemCallback<Complaint>() {
        override fun areItemsTheSame(oldItem: Complaint, newItem: Complaint): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Complaint, newItem: Complaint): Boolean {
            return oldItem == newItem
        }
    }
}
