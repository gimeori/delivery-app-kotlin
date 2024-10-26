package com.example.deliveryapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.deliveryapp.databinding.ActivityOrderBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView


class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        binding=ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.map
        var mapKit: MapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        requestLocationPermission()
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        userLocationLayer.setObjectListener(object : UserLocationObjectListener {
            override fun onObjectAdded(userLocationView: UserLocationView) {
                userLocationLayer.cameraPosition()?.target?.let { userLocation ->
                    mapView.map.move(
                        CameraPosition(userLocation, 12.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 5f),
                        null
                    )
                }
            }

            override fun onObjectRemoved(view: UserLocationView) {
            }

            override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
            }
        })


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

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),0)
            return
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