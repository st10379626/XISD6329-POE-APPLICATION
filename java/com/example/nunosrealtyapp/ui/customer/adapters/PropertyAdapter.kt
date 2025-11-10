package com.example.nunosrealtyapp.ui.customer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nunosrealtyapp.data.model.Property
import com.example.nunosrealtyapp.databinding.ItemPropertyBinding

class PropertyAdapter(
    private val onItemClick: (Property) -> Unit
) : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)
        holder.bind(property)
    }

    inner class PropertyViewHolder(private val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            binding.propertyTitleTextView.text = property.title
            binding.propertyLocationTextView.text = "${property.city}, ${property.province}"
            binding.bedsTextView.text = property.beds.toString()
            binding.bathsTextView.text = property.baths.toString()
            binding.areaTextView.text = "${property.areaSqft} sqft"

            if (property.isForSale) {
                binding.propertyPriceTextView.text = "R${property.price}M"
            } else {
                binding.propertyPriceTextView.text = "R${property.price}/m"
            }

            if (property.images.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(property.images[0])
                    .into(binding.propertyImageView)
            }

            binding.ratingBar.rating = property.rating.toFloat()
            binding.ratingTextView.text = property.rating.toString()

            binding.root.setOnClickListener {
                onItemClick(property)
            }
        }
    }

    class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}