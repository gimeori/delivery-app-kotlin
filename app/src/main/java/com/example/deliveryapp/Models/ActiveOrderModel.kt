package com.example.deliveryapp.Models

import com.google.gson.annotations.SerializedName
import android.util.Log

class ActiveOrderModel {

    @SerializedName("id")
    private var id: Int? = null
    
    @SerializedName("created_at")
    private var createdAt: String = ""
    
    @SerializedName("address")
    private var address: String = ""
    
    @SerializedName("order_status")
    private var orderStatus: String = ""
    
    @SerializedName("user_id")
    private var userId: Int? = null
    
    // Получаем сумму как Any для максимальной совместимости
    @SerializedName("total_price")
    private var totalPrice: Any? = null

    constructor()
    constructor(
        id: Int?,
        createdAt: String,
        address: String,
        orderStatus: String,
        userId: Int?,
        totalPrice: Any?
    ) {
        this.id = id
        this.createdAt = createdAt
        this.address = address
        this.orderStatus = orderStatus
        this.userId = userId
        this.totalPrice = totalPrice
    }

    fun getId(): Int? {
        return id
    }

    fun getCreatedAt(): String {
        return createdAt
    }

    fun getAddress(): String {
        return address
    }

    fun getOrderStatus(): String {
        return orderStatus
    }

    fun getUserId(): Int? {
        return userId
    }

    fun getTotalPrice(): Double {
        Log.d("ActiveOrderModel", "getTotalPrice вызван для заказа ID=$id, raw=$totalPrice (${totalPrice?.javaClass?.simpleName})")
        
        try {
            val rawPrice = totalPrice
            val price = when (rawPrice) {
                is Int -> rawPrice.toDouble()
                is Double -> rawPrice
                is String -> {
                    try {
                        rawPrice.toDouble()
                    } catch (e: Exception) {
                        Log.e("ActiveOrderModel", "Не удалось преобразовать строку в Double: $rawPrice", e)
                        0.0
                    }
                }
                is Number -> rawPrice.toDouble()
                null -> 0.0
                else -> {
                    try {
                        rawPrice.toString().toDoubleOrNull() ?: 0.0
                    } catch (e: Exception) {
                        Log.e("ActiveOrderModel", "Не удалось преобразовать к Double: $rawPrice", e)
                        0.0
                    }
                }
            }
            
            Log.d("ActiveOrderModel", "getTotalPrice: rawPrice=$rawPrice (${rawPrice?.javaClass?.simpleName}), преобразовано в price=$price")
            return price
        } catch (e: Exception) {
            Log.e("ActiveOrderModel", "Ошибка при получении цены", e)
            return 0.0
        }
    }
    
    // Метод для форматирования суммы в рублях
    fun getFormattedPrice(): String {
        val price = getTotalPrice()
        
        // Для форматирования используем целое число, если дробная часть равна нулю
        val priceInt = if (price == price.toInt().toDouble()) {
            price.toInt()
        } else {
            null
        }
        
        if (priceInt != null) {
            // Форматируем целое число с разделителями тысяч
            val formattedPrice = priceInt.toString()
                .reversed()
                .chunked(3)
                .joinToString(" ")
                .reversed()
            return "$formattedPrice ₽"
        } else {
            // Форматируем дробное число
            val formattedPrice = String.format("%.2f", price)
                .replace(".", ",")
            return "$formattedPrice ₽"
        }
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun setCreatedAt(createdAt: String) {
        this.createdAt = createdAt
    }

    fun setAddress(address: String) {
        this.address = address
    }

    fun setOrderStatus(orderStatus: String) {
        this.orderStatus = orderStatus
    }

    fun setUserId(userId: Int?) {
        this.userId = userId
    }

    fun setTotalPrice(totalPrice: Any?) {
        this.totalPrice = totalPrice
        Log.d("ActiveOrderModel", "setTotalPrice: установлено значение $totalPrice (${totalPrice?.javaClass?.simpleName})")
    }
    
    override fun toString(): String {
        val rawPrice = totalPrice
        return "ActiveOrderModel(id=$id, total_price=$rawPrice (${rawPrice?.javaClass?.simpleName}), address='$address', status='$orderStatus')"
    }
}