package com.example.deliveryapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.deliveryapp.databinding.ActivityOrderBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

class OrderActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var drivingRouter: DrivingRouter
    private lateinit var drivingSession: DrivingSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.map
        val mapKit: MapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        requestLocationPermission()
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        mapObjects = mapView.map.mapObjects.addCollection()
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE) // Передаем контекст

        val orderId = intent.getIntExtra("orderId", 0)
        val orderTotalPrice = intent.getStringExtra("orderTotalPrice")
        val orderStatus = intent.getStringExtra("orderStatus")
        binding.orderDetailsId.text = orderId.toString()
        binding.orderDetailsStatus.text = orderStatus
        binding.orderDetailsTotalPrice.text = orderTotalPrice
        binding.backActiveOrders.setOnClickListener {
            finish()
        }
        binding.takeOrder.setOnClickListener {
            binding.orderDetailsStatus.text = "отправлен"
            binding.takeOrder.text = "завершить заказ"
            buildRoute()
        }
    }

    private fun buildRoute() {
        val startPoint = userLocationLayer.cameraPosition()!!.target
        val endPoint = Point(53.34047480346402, 83.71960030266673) // Замените на ваш конечный адрес

        val requestPoints = listOf(
            RequestPoint(startPoint, RequestPointType.WAYPOINT, null, null),
            RequestPoint(endPoint, RequestPointType.WAYPOINT, null, null)
        )

        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()

        drivingSession = drivingRouter.requestRoutes(
            requestPoints,
            drivingOptions,
            vehicleOptions,
            this
        )
    }

    override fun onDrivingRoutes(routes: MutableList<out DrivingRoute>) {
        if (routes.isNotEmpty()) {
            val route = routes[0]
            val polyline = mapObjects.addPolyline(route.geometry)
            polyline.setStrokeColor(0xFF0000FF.toInt())
            polyline.setStrokeWidth(5f)
            moveCameraToPoint(route.geometry.points[0])
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        val errorMessage = when (error) {
            is RemoteError -> "Remote error: ${error}"
            is NetworkError -> "Network error"
            else -> "Unknown error"
        }
        // Показ уведомления пользователю
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun moveCameraToPoint(point: Point) {
        mapView.map.move(
            CameraPosition(point, 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
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
