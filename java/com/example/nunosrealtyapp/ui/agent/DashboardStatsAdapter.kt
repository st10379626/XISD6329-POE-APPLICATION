package com.example.nunosrealtyapp.ui.agent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nunosrealtyapp.databinding.ItemDashboardStatBinding

class DashboardStatsAdapter : ListAdapter<DashboardStat, DashboardStatsAdapter.StatViewHolder>(StatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemDashboardStatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        val stat = getItem(position)
        holder.bind(stat)
    }

    inner class StatViewHolder(private val binding: ItemDashboardStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: DashboardStat) {
            binding.statTitleTextView.text = stat.title
            binding.statValueTextView.text = stat.value
            binding.statSubtitleTextView.text = stat.subtitle
            binding.statIconImageView.setImageResource(stat.iconRes)
        }
    }

    class StatDiffCallback : DiffUtil.ItemCallback<DashboardStat>() {
        override fun areItemsTheSame(oldItem: DashboardStat, newItem: DashboardStat): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DashboardStat, newItem: DashboardStat): Boolean {
            return oldItem == newItem
        }
    }
}

data class DashboardStat(
    val title: String,
    val value: String,
    val subtitle: String,
    val iconRes: Int
)