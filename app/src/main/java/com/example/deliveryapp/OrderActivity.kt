package com.example.deliveryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deliveryapp.databinding.ActivityOrderBinding

class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId=intent.getIntExtra("orderId",0)
        val orderTotalPrice=intent.getStringExtra("orderTotalPrice")
        val orderStatus=intent.getStringExtra("orderStatus")
        binding.orderDetailsId.text=orderId.toString()
        binding.orderDetailsStatus.text=orderStatus
        binding.orderDetailsTotalPrice.text=orderTotalPrice
        binding.backActiveOrders.setOnClickListener{
            finish()
        }


    }
}