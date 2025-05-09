package com.example.deliveryapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryapp.Models.DeliveredOrderModel
import com.example.deliveryapp.databinding.ItemDeliveredOrderBinding

class DeliveredOrdersAdapter : ListAdapter<DeliveredOrderModel, DeliveredOrdersAdapter.DeliveredOrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveredOrderViewHolder {
        val binding = ItemDeliveredOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeliveredOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveredOrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class DeliveredOrderViewHolder(private val binding: ItemDeliveredOrderBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: DeliveredOrderModel) {
            binding.tvOrderId.text = order.getOrderId()
            binding.tvOrderPrice.text = order.getFormattedPrice()
            binding.tvOrderDate.text = order.createdAt
            binding.tvOrderAddress.text = order.address
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<DeliveredOrderModel>() {
        override fun areItemsTheSame(oldItem: DeliveredOrderModel, newItem: DeliveredOrderModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DeliveredOrderModel, newItem: DeliveredOrderModel): Boolean {
            return oldItem == newItem
        }
    }
} 