package com.example.deliveryapp.Models

class ActiveOrderModel {

    private var orderId : Int?=null
    private var orderTotalPrice: String=""
    private var orderStatus: String=""

    constructor()
    constructor(orderId: Int?, orderTotalPrice: String, orderStatus: String) {
        this.orderId = orderId
        this.orderTotalPrice = orderTotalPrice
        this.orderStatus = orderStatus
    }

    fun getOrderId(): Int?{
        return orderId
    }
    fun getOrderTotalPrice(): String{
        return orderTotalPrice
    }
    fun getOrderStatus(): String{
        return orderStatus
    }
    fun setOrderId(orderId: Int?){
        this.orderId=orderId
    }
    fun setOrderTotalPrice(orderTotalPrice: String){
        this.orderTotalPrice=orderTotalPrice
    }
    fun setOrderStatus(orderStatus:String){
        this.orderStatus=orderStatus
    }
}