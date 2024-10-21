package com.example.deliveryapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.deliveryapp.Adapters.ActiveOrderAdapter
import com.example.deliveryapp.Models.ActiveOrderModel
import com.example.deliveryapp.R


class ActiveOrdersFragment : Fragment() {
    private lateinit var activeAdapter: ActiveOrderAdapter
    private lateinit var listActiveOrders: ArrayList<ActiveOrderModel>
    private lateinit var activeRv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_active_orders, container, false)
        activeRv=view.findViewById(R.id.active_orders_rv)
        listActiveOrders=ArrayList()
        listActiveOrders.add(ActiveOrderModel(orderId = 2, orderTotalPrice = "576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 3, orderTotalPrice = "1576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 4, orderTotalPrice = "6782", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 5, orderTotalPrice = "1250", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 2, orderTotalPrice = "576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 3, orderTotalPrice = "1576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 4, orderTotalPrice = "6782", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 5, orderTotalPrice = "1250", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 2, orderTotalPrice = "576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 3, orderTotalPrice = "1576", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 4, orderTotalPrice = "6782", orderStatus = "новый"))
        listActiveOrders.add(ActiveOrderModel(orderId = 5, orderTotalPrice = "1250", orderStatus = "новый"))

        activeAdapter= ActiveOrderAdapter(requireContext(),listActiveOrders)
        activeRv.layoutManager=LinearLayoutManager(requireContext())
        activeRv.adapter=activeAdapter

        return view
    }
}