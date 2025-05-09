package com.example.deliveryapp.Models

import com.google.gson.annotations.SerializedName

data class CourierStatsModel(
    @SerializedName("courier_id") val courierId: Int,
    @SerializedName("orders_delivered_today") val ordersDeliveredToday: Int,
    @SerializedName("orders_total_price_today") val ordersTotalPriceToday: Int,
    @SerializedName("orders_delivered_last_month") val ordersDeliveredLastMonth: Int,
    @SerializedName("orders_total_price_last_month") val ordersTotalPriceLastMonth: Int,
    @SerializedName("orders_delivered_all_time") val ordersDeliveredAllTime: Int,
    @SerializedName("orders_total_price_all_time") val ordersTotalPriceAllTime: Int
) 