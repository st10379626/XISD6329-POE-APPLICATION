package com.example.nunosrealtyapp.ui.agent


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nunosrealtyapp.data.model.Property
import com.example.nunosrealtyapp.databinding.ItemListingBinding

class ListingsAdapter(
    private val onItemClick: (Property) -> Unit
) : ListAdapter<Property, ListingsAdapter.ListingViewHolder>(ListingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = getItem(position)
        holder.bind(listing)
    }

    inner class ListingViewHolder(private val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: Property) {
            binding.listingTitleTextView.text = listing.title
            binding.listingLocationTextView.text = "${listing.city}, ${listing.province}"
            binding.listingPriceTextView.text = if (listing.isForSale) {
                "R${listing.price}M"
            } else {
                "R${listing.price}/m"
            }
            binding.bedsTextView.text = listing.beds.toString()
            binding.bathsTextView.text = listing.baths.toString()
            binding.areaTextView.text = "${listing.areaSqft} sqft"

            // Load image if available
            if (listing.images.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(listing.images[0])
                    .into(binding.listingImageView)
            }

            binding.root.setOnClickListener {
                onItemClick(listing)
            }
        }
    }

    class ListingDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}