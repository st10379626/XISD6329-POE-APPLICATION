package com.example.nunosrealtyapp.ui.agent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nunosrealtyapp.data.model.Application
import com.example.nunosrealtyapp.databinding.ItemApplicationBinding

class ApplicationsAdapter : ListAdapter<Application, ApplicationsAdapter.ApplicationViewHolder>(ApplicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val binding = ItemApplicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = getItem(position)
        holder.bind(application)
    }

    inner class ApplicationViewHolder(private val binding: ItemApplicationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(application: Application) {
            binding.applicationTitleTextView.text = application.propertyTitle
            binding.applicationTypeTextView.text = application.type.replaceFirstChar { it.uppercase() }
            binding.applicationStatusTextView.text = application.status.replaceFirstChar { it.uppercase() }
            binding.applicationDateTextView.text = application.formattedDate

            // Set background based on status
            val backgroundRes = when (application.status.lowercase()) {
                "approved" -> com.example.nunosrealtyapp.R.drawable.bg_status_approved
                "rejected" -> com.example.nunosrealtyapp.R.drawable.bg_status_rejected
                else -> com.example.nunosrealtyapp.R.drawable.bg_status_pending
            }
            binding.applicationStatusTextView.setBackgroundResource(backgroundRes)
        }
    }

    class ApplicationDiffCallback : DiffUtil.ItemCallback<Application>() {
        override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean {
            return oldItem == newItem
        }
    }
}