package com.example.deliveryapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import com.example.deliveryapp.Models.OrderDetailsModel
import com.example.deliveryapp.Network.LoginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.TextView
import android.view.View
import android.widget.LinearLayout
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.map.VisibleRegionUtils

class OrderActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener, Session.SearchListener {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var drivingRouter: DrivingRouter
    private lateinit var drivingSession: DrivingSession
    private var searchSession: Session? = null
    private lateinit var searchManager: SearchManager
    private lateinit var repository: LoginRepository
    private var orderId: Int = 0
    private var initialDateSet = false
    private val TAG = "OrderActivity"

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

        mapObjects = mapView.mapWindow.map.mapObjects.addCollection()
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        
        // Инициализируем репозиторий
        Log.e("ORDER_DEBUG", "OrderActivity: onCreate, инициализируем репозиторий")
        repository = LoginRepository(this as Context)

        // Получаем ID заказа из Intent
        orderId = intent.getIntExtra("orderId", 0)
        Log.e("ORDER_DEBUG", "OrderActivity: получен orderId=$orderId из Intent")
        Log.d(TAG, "Получен ID заказа: $orderId")
        
        // Сначала отображаем данные из Intent для быстрого отображения
        displayOrderFromIntent()
        
        // Затем, если есть ID, пытаемся загрузить детальную информацию
        if (orderId > 0) {
            Log.e("ORDER_DEBUG", "OrderActivity: orderId > 0, вызываем loadOrderDetails($orderId)")
            loadOrderDetails(orderId)
        } else {
            Log.e("ORDER_DEBUG", "OrderActivity: orderId <= 0, не вызываем loadOrderDetails!")
            Toast.makeText(this, "Ошибка: ID заказа не передан.", Toast.LENGTH_LONG).show()
            // Возможно, стоит закрыть активити, если ID нет
            // finish()
        }
        
        binding.backActiveOrders.setOnClickListener {
            finish()
        }
        
        setupTakeOrderButtonListener()
    }
    
    private fun setupTakeOrderButtonListener() {
        binding.takeOrder.setOnClickListener {
            val currentStatus = binding.orderDetailsStatus.text.toString()
            val currentButtonText = binding.takeOrder.text.toString()
            
            Log.d(TAG, "Нажата кнопка '$currentButtonText'. Текущий статус: '$currentStatus'")

            if (currentButtonText == "Взять в работу") {
                updateStatusAndBuildRoute("transit", "В работе", "Завершить заказ")
            } else if (currentButtonText == "Завершить заказ") {
                updateStatusAndShowCompletion("delivered", "Доставлен", "Заказ доставлен")
            } else {
                Log.w(TAG, "Неизвестное состояние кнопки: '$currentButtonText'")
            }
        }
    }
    
    private fun updateStatusAndBuildRoute(newApiStatus: String, newUiStatus: String, newButtonText: String) {
        binding.takeOrder.isEnabled = false // Блокируем кнопку на время запроса
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Вызов updateOrderStatus($orderId, $newApiStatus)")
                repository.updateOrderStatus(orderId, newApiStatus)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Статус успешно обновлен на $newApiStatus")
                    binding.orderDetailsStatus.text = newUiStatus
                    binding.takeOrder.text = newButtonText
                    binding.progressBar.visibility = View.GONE
                    binding.takeOrder.isEnabled = true
                    loadOrderDetails(orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обновлении статуса на $newApiStatus", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderActivity, "Ошибка обновления статуса: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                    binding.takeOrder.isEnabled = true // Разблокируем кнопку при ошибке
                }
            }
        }
    }
    
     private fun updateStatusAndShowCompletion(newApiStatus: String, newUiStatus: String, newButtonText: String) {
        binding.takeOrder.isEnabled = false // Блокируем кнопку на время запроса
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Вызов updateOrderStatus($orderId, $newApiStatus)")
                repository.updateOrderStatus(orderId, newApiStatus)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Статус успешно обновлен на $newApiStatus")
                    binding.orderDetailsStatus.text = newUiStatus
                    binding.takeOrder.text = newButtonText
                    binding.progressBar.visibility = View.GONE
                    binding.takeOrder.isEnabled = false // Оставляем кнопку неактивной после завершения
                    Toast.makeText(this@OrderActivity, "Заказ завершен!", Toast.LENGTH_SHORT).show()
                    loadOrderDetails(orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обновлении статуса на $newApiStatus", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderActivity, "Ошибка обновления статуса: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                    binding.takeOrder.isEnabled = true // Разблокируем кнопку при ошибке, чтобы можно было попробовать снова
                }
            }
        }
    }
    
    private fun loadOrderDetails(orderId: Int) {
        // Показываем прогресс загрузки
        binding.progressBar.visibility = View.VISIBLE
        
        // Добавляем логи для диагностики
        Log.e("ORDER_DEBUG", "OrderActivity: loadOrderDetails начало, orderId=$orderId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Запрашиваем детали заказа по ID
                Log.d(TAG, "Запрос деталей заказа ID=$orderId")
                Log.e("ORDER_DEBUG", "OrderActivity: перед вызовом repository.getOrderDetails($orderId)")
                val orderDetails = repository.getOrderDetails(orderId)
                Log.e("ORDER_DEBUG", "OrderActivity: УСПЕХ! Данные получены: ID=${orderDetails.order_id}")
                Log.d(TAG, "Получены детали: ${orderDetails.order_id}, ${orderDetails.user_name}, статус: ${orderDetails.order_status}")
                
                // Обновляем UI в главном потоке
                withContext(Dispatchers.Main) {
                    displayOrderDetailsFormatted(orderDetails)
                    binding.progressBar.visibility = View.GONE
                    // Call buildRoute() immediately after displaying details
            buildRoute()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке заказа: ${e.message}", e)
                Log.e("ORDER_DEBUG", "OrderActivity: ОШИБКА при вызове repository.getOrderDetails: ${e.javaClass.simpleName} - ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderActivity, "Ошибка загрузки заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    
    private fun displayOrderDetailsFormatted(order: OrderDetailsModel) {
        // Логируем полученные данные для отладки
        Log.d("OrderDetails", "Отображаем данные: ${order.user_name}, ${order.user_phone}, ${order.address}, статус: ${order.order_status}")
        
        // Обновляем ID заказа
        binding.orderDetailsId.text = "Заказ №${order.order_id}"
        
        // Устанавливаем дату
        binding.createdAtDate.text = order.created_at
        
        // Устанавливаем информацию о пользователе в три отдельных поля
        binding.orderDetailsAddress.text = "Адрес: ${order.address}"
        binding.orderDetailsUserName.text = "Заказчик: ${order.user_name}"
        binding.orderDetailsUserPhone.text = "Телефон: ${order.user_phone}"
        
        // Показываем заголовок для пицц
        binding.pizzasTitle.text = "Состав заказа:"
        
        // Очищаем и заполняем контейнер с пиццами
        val pizzaContainer = binding.pizzaItemsContainer
        pizzaContainer.removeAllViews()
        
        // Добавляем каждую пиццу как TextView
        for (pizza in order.pizzas) {
            val pizzaView = TextView(this)
            pizzaView.text = "${pizza.pizzaname} ${pizza.count} шт ${pizza.size} см ${pizza.type} тесто"
            pizzaView.textSize = 16f
            pizzaView.setTextColor(ContextCompat.getColor(this, R.color.black))
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 8) // Добавляем отступ снизу
            pizzaView.layoutParams = layoutParams
            pizzaContainer.addView(pizzaView)
        }
        
        // Устанавливаем общую сумму с форматированием
        binding.orderDetailsTotalPrice.text = "${order.order_total_price} ₽"
        
        // Устанавливаем статус заказа с форматированием
        val statusText: String
        val buttonText: String
        val isButtonEnabled: Boolean

        when (order.order_status) {
            "inprocess" -> {
                statusText = "В процессе"
                buttonText = "Взять в работу"
                isButtonEnabled = true
            }
            "transit" -> {
                statusText = "В работе"
                buttonText = "Завершить заказ"
                isButtonEnabled = true
            }
            "delivered" -> {
                statusText = "Доставлен"
                buttonText = "Заказ доставлен"
                isButtonEnabled = false
            }
            else -> {
                statusText = order.order_status // Отображаем как есть, если статус неизвестен
                buttonText = "Статус: ${order.order_status}" // Информационный текст для неизвестного статуса
                isButtonEnabled = false
            }
        }
        binding.orderDetailsStatus.text = statusText
        binding.takeOrder.text = buttonText
        binding.takeOrder.isEnabled = isButtonEnabled
        Log.d(TAG, "Статус установлен: '$statusText', Текст кнопки: '$buttonText', Кнопка активна: $isButtonEnabled")
    }
    
    private fun displayOrderFromIntent() {
        // Получаем данные из Intent
        val orderTotalPrice = intent.getStringExtra("orderTotalPrice") ?: "0 ₽"
        val orderStatus = intent.getStringExtra("orderStatus") ?: ""
        val address = intent.getStringExtra("address") ?: "Нет адреса"
        val date = intent.getStringExtra("date") ?: ""
        
        // Логируем данные из Intent для диагностики
        Log.e("ORDER_DEBUG", "displayOrderFromIntent: orderId=$orderId, totalPrice=$orderTotalPrice, status=$orderStatus, address=$address, date=$date")
        
        // Устанавливаем основные данные
        binding.orderDetailsId.text = "Заказ №$orderId"
        
        // Устанавливаем дату только если она не пустая
        if (date.isNotEmpty()) {
            binding.createdAtDate.text = date
            initialDateSet = true
        } else {
            Log.e("ORDER_DEBUG", "displayOrderFromIntent: дата пустая!")
            binding.createdAtDate.text = "Дата не указана" // Плейсхолдер, если дата пустая
        }
        
        binding.orderDetailsAddress.text = "Адрес: $address"
        
        // Устанавливаем информацию о пользователе
        binding.orderDetailsUserName.text = "Заказчик: загружается..."
        binding.orderDetailsUserPhone.text = "Телефон: загружается..."
        
        // Устанавливаем заголовок для пицц
        binding.pizzasTitle.text = "Состав заказа:"
        
        // Очищаем контейнер с пиццами и добавляем временное сообщение
        binding.pizzaItemsContainer.removeAllViews()
        val loadingText = TextView(this)
        loadingText.text = "Загрузка содержимого заказа..."
        loadingText.textSize = 16f
        binding.pizzaItemsContainer.addView(loadingText)
        
        // Устанавливаем общую сумму
        binding.orderDetailsTotalPrice.text = orderTotalPrice
        
        // Устанавливаем статус и кнопку на основе данных из Intent
        val statusText: String
        val buttonText: String
        val isButtonEnabled: Boolean

        when (orderStatus) {
            "inprocess" -> {
                statusText = "В процессе"
                buttonText = "Взять в работу"
                isButtonEnabled = true
            }
            "transit" -> { // Добавим обработку transit из Intent
                statusText = "В работе"
                buttonText = "Завершить заказ"
                isButtonEnabled = true
            }
            "delivered" -> {
                statusText = "Доставлен"
                buttonText = "Заказ доставлен"
                isButtonEnabled = false
            }
            else -> {
                statusText = orderStatus // Если статус неизвестен
                buttonText = "Загрузка..."
                isButtonEnabled = false
            }
        }
        binding.orderDetailsStatus.text = statusText
        binding.takeOrder.text = buttonText
        binding.takeOrder.isEnabled = isButtonEnabled
    }

    private fun buildRoute() {
        mapObjects.clear()
        searchSession?.cancel()

        // Check user location
        val userPos = userLocationLayer.cameraPosition()?.target
        if (userPos == null) {
            Toast.makeText(this, "Ждём позицию GPS…", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "buildRoute: user location unknown")
            return
        }
        Log.d(TAG, "buildRoute: User location: ${userPos.latitude}, ${userPos.longitude}")

        // Prepare destination address
        val addressText = binding.orderDetailsAddress.text.toString().removePrefix("Адрес: ").trim()
        if (addressText.isEmpty()) {
            Toast.makeText(this, "Адрес доставки не указан", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "buildRoute: Destination address is empty")
            return
        }
        Log.d(TAG, "buildRoute: Searching for address: $addressText")

        // Geocode destination address within current map visible region
        searchSession = searchManager.submit(
            addressText,
            VisibleRegionUtils.toPolygon(mapView.mapWindow.map.visibleRegion),
            SearchOptions(),
            this
        )
        Log.d(TAG, "buildRoute: Submitted geocoding request for address: $addressText around $userPos")
    }

    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
        if (routes.isNotEmpty()) {
            val route = routes[0]
            mapObjects.clear()
            mapObjects.addPolyline(route.geometry)
            val endPoint = route.geometry.points.last()
            mapObjects.addPlacemark(endPoint).zIndex = 2f
            mapView.mapWindow.map.move(
                CameraPosition(endPoint, 14.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1.0f),
                null
            )
        } else {
            Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        val errorMessage = when (error) {
            is RemoteError -> "Ошибка сервера маршрутизации"
            is NetworkError -> "Проблема с сетью при построении маршрута"
            else -> "Неизвестная ошибка маршрутизации"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "onDrivingRoutesError: Error building route: $errorMessage", error as? Throwable ?: RuntimeException(errorMessage))
    }

    override fun onSearchResponse(response: com.yandex.mapkit.search.Response) {
        Log.e(TAG, "onSearchResponse children = ${response.collection.children.size}")
        val geo = response.collection.children.firstOrNull()?.obj
        val dest = geo?.geometry?.firstOrNull()?.point
        if (dest == null) {
            Toast.makeText(this, "Не удалось найти координаты для адреса", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onSearchResponse: no geocoding result")
            return
        }
        Log.e(TAG, "onSearchResponse: found destination = ${dest.latitude}, ${dest.longitude}")
        val userPos = userLocationLayer.cameraPosition()?.target
        if (userPos == null) {
            Toast.makeText(this, "Ждём позицию GPS…", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onSearchResponse: userPos unknown")
            return
        }
        val requestPoints = listOf(
            RequestPoint(userPos, RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(dest, RequestPointType.WAYPOINT, null, null, null)
        )
        drivingSession = drivingRouter.requestRoutes(
            requestPoints,
            DrivingOptions().apply { routesCount = 1 },
            VehicleOptions(),
            this
        )
        Log.d(TAG, "onSearchResponse: Requested routes from ${userPos.latitude},${userPos.longitude} to ${dest.latitude},${dest.longitude}")
    }

    override fun onSearchError(error: Error) {
        val errorMessage = when (error) {
            is RemoteError -> "Ошибка сервера геокодирования"
            is NetworkError -> "Проблема с сетью при геокодировании"
            else -> "Неизвестная ошибка геокодирования"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "onSearchError: Geocoding error: $errorMessage", error as? Throwable ?: RuntimeException(errorMessage))
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
             // Permission already granted, maybe center map?
             Log.d(TAG, "Location permission already granted.")
             centerMapOnUserIfNeeded()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 // Permission granted
                 Log.d(TAG, "Location permission granted by user.")
                 userLocationLayer.isVisible = true
                 userLocationLayer.isHeadingEnabled = true
                 centerMapOnUserIfNeeded()
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Разрешение на доступ к местоположению необходимо для работы карты",
                    Toast.LENGTH_LONG
                ).show()
                 Log.w(TAG, "Location permission denied by user.")
            }
        }
    }
    
    private fun centerMapOnUserIfNeeded() {
        // Center map only if user location is known
        val userPos = userLocationLayer.cameraPosition()?.target
        if (userPos != null) {
             Log.d(TAG, "Centering map on user location: ${userPos.latitude}, ${userPos.longitude}")
            mapView.mapWindow.map.move(
                CameraPosition(userPos, 14.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1.0f), null
            )
        } else {
             Log.d(TAG, "Cannot center map, user location is unknown yet.")
             // Optionally move to a default location
             // mapView.mapWindow.map.move(...)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
            Log.d(TAG, "MapKit onStart() called")
        } catch (e: Exception) {
            Log.e(TAG, "Error in MapKit onStart", e)
        }
    }

    override fun onStop() {
        try {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
            Log.d(TAG, "MapKit onStop() called")
        } catch (e: Exception) {
            Log.e(TAG, "Error in MapKit onStop", e)
        }
        super.onStop()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
