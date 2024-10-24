package com.example.deliveryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryapp.databinding.ActivityOrderBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("d00f0dd9-f999-4556-9da4-9086f95dbc06")
        MapKitFactory.initialize(this)
        binding=ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.map
        mapView.map.move(CameraPosition(Point(53.346048, 83.732656),11.0f,0.0f,0.0f), Animation(Animation.Type.SMOOTH, 300f), null)
        val orderId=intent.getIntExtra("orderId",0)
        val orderTotalPrice=intent.getStringExtra("orderTotalPrice")
        val orderStatus=intent.getStringExtra("orderStatus")
        binding.orderDetailsId.text=orderId.toString()
        binding.orderDetailsStatus.text=orderStatus
        binding.orderDetailsTotalPrice.text=orderTotalPrice
        binding.backActiveOrders.setOnClickListener{
            finish()
        }
        binding.takeOrder.setOnClickListener{

            binding.orderDetailsStatus.text="отправлен"
            binding.takeOrder.text="завершить заказ"
        }
    }
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}