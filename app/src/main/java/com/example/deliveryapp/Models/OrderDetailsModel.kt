package com.example.deliveryapp.Models

import com.google.gson.annotations.SerializedName

data class OrderDetailsModel(
    @SerializedName("order_id") val order_id: Int,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("user_name") val user_name: String,
    @SerializedName("user_phone") val user_phone: String,
    @SerializedName("address") val address: String,
    @SerializedName("pizzas") val pizzas: List<PizzaItem>,
    @SerializedName("order_total_price") val order_total_price: Int,
    @SerializedName("order_status") val order_status: String
)

data class PizzaItem(
    @SerializedName("pizza_id") val pizza_id: Int,
    @SerializedName("pizzaname") val pizzaname: String,
    @SerializedName("count") val count: Int,
    @SerializedName("size") val size: String,
    @SerializedName("type") val type: String
) 