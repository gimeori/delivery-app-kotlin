package com.example.deliveryapp.Models

import com.google.gson.annotations.SerializedName

data class DeliveredOrderModel(
    @SerializedName("id") val id: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("address") val address: String,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_price") val totalPrice: Int
) {
    fun getFormattedPrice(): String {
        return "$totalPrice ₽"
    }
    
    fun getOrderId(): String {
        return "Заказ №$id"
    }
} 