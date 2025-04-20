package com.example.deliveryapp.fragments

import com.example.deliveryapp.Network.LoginRepository
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.deliveryapp.Adapters.ActiveOrderAdapter
import com.example.deliveryapp.Models.ActiveOrderModel
import com.example.deliveryapp.R
import com.example.deliveryapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.content.Intent
import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope


class ActiveOrdersFragment : Fragment() {
    private lateinit var activeAdapter: ActiveOrderAdapter
    private lateinit var listActiveOrders: ArrayList<ActiveOrderModel>
    private lateinit var activeRv: RecyclerView
    private lateinit var repository: LoginRepository
    private val TAG = "ActiveOrdersFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = LoginRepository(requireContext())
        Log.d(TAG, "onCreate: инициализация LoginRepository")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_active_orders, container, false)
        activeRv=view.findViewById(R.id.active_orders_rv)
        listActiveOrders=ArrayList()
        
        // Initialize adapter with empty list
        activeAdapter = ActiveOrderAdapter(requireContext(), listActiveOrders)
        activeRv.layoutManager = LinearLayoutManager(requireContext())
        activeRv.adapter = activeAdapter
        
        // Добавляем обработчик кнопки обновления
        view.findViewById<View>(R.id.refresh_button)?.setOnClickListener {
            Log.d(TAG, "Нажата кнопка обновления, повторная загрузка заказов")
            // Очищаем текущие заказы и загружаем новые
            listActiveOrders.clear()
            activeAdapter.notifyDataSetChanged()
            fetchOrders()
        }
        
        // Fetch orders from API
        Log.d(TAG, "onCreateView: вызов fetchOrders()")
        fetchOrders()
        
        return view
    }
    
    private fun fetchOrders() {
        Log.d(TAG, "fetchOrders: начало получения заказов")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Проверяем, авторизован ли пользователь
                val isLoggedIn = repository.isLoggedIn()
                Log.d(TAG, "fetchOrders: isLoggedIn = $isLoggedIn")
                
                if (!isLoggedIn) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Необходимо авторизоваться для просмотра заказов",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Даже если не авторизован, показываем тестовый заказ
                        showTestOrder()
                    }
                    return@launch
                }
                
                Log.d(TAG, "fetchOrders: начало запроса к API getOrders()")
                val orders = repository.getOrders()
                Log.d(TAG, "fetchOrders: получено ${orders.size} заказов")
                
                withContext(Dispatchers.Main) {
                    listActiveOrders.clear()
                    if (orders.isNotEmpty()) {
                        Log.d(TAG, "fetchOrders: добавляем полученные заказы в список")
                        listActiveOrders.addAll(orders)
                    } else {
                        Log.d(TAG, "fetchOrders: список заказов пуст, добавляем тестовый заказ")
                        showTestOrder()
                    }
                    activeAdapter.notifyDataSetChanged()
                    Log.d(TAG, "fetchOrders: данные обновлены в адаптере")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchOrders: ошибка получения заказов", e)
                
                // Добавляем более подробную диагностику
                val errorMessage = when {
                    e.message?.contains("401") == true -> {
                        Log.e(TAG, "fetchOrders: Ошибка 401 Unauthorized - проблема с авторизацией, токен недействителен")
                        "Ошибка авторизации. Пожалуйста, войдите заново."
                    }
                    e.message?.contains("404") == true -> {
                        Log.e(TAG, "fetchOrders: Ошибка 404 Not Found - проверьте URL API")
                        "Ресурс не найден. Проверьте подключение."
                    }
                    e.message?.contains("connect") == true -> {
                        Log.e(TAG, "fetchOrders: Проблема с сетевым подключением")
                        "Ошибка сети. Проверьте интернет-соединение."
                    }
                    else -> "Ошибка при загрузке заказов: ${e.message}"
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Для ошибки 401 предлагаем выход из аккаунта
                    if (e.message?.contains("401") == true) {
                        showAuthErrorDialog()
                    }
                    
                    // If API call fails, add mock data as fallback
                    Log.d(TAG, "fetchOrders: добавление тестового заказа после ошибки")
                    listActiveOrders.clear()
                    showTestOrder()
                    activeAdapter.notifyDataSetChanged()
                }
            }
        }
    }
    
    private fun showTestOrder() {
        // Очищаем список и добавляем тестовый заказ с ID=4 и ценой 725
        listActiveOrders.clear()
        
        try {
            Log.d(TAG, "showTestOrder: создание тестового заказа с ID=4 и суммой 725")
            
            // Создаем тестовый заказ с точными данными из примера API
            val testOrder = ActiveOrderModel(
                id = 4,
                createdAt = "05.04.25 14:35",
                address = "Попова 129/1, 41",
                orderStatus = "inprocess",
                userId = 2,
                totalPrice = 725  // Точная сумма из примера API
            )
            
            // Дополнительно устанавливаем цену напрямую для уверенности
            testOrder.setTotalPrice(725)
            
            // Добавляем заказ в список
            listActiveOrders.add(testOrder)
            
            // Обновляем UI
            activeAdapter.notifyDataSetChanged()
            
            // Проверяем, что сумма корректно установлена
            Log.d(TAG, "showTestOrder: добавлен тестовый заказ с ID=${testOrder.getId()}, сумма=${testOrder.getTotalPrice()}, форматированная сумма=${testOrder.getFormattedPrice()}")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при создании тестового заказа", e)
        }
    }
    
    // Метод для выхода из аккаунта и перехода на экран авторизации
    private fun logoutAndRelogin() {
        // Launch a coroutine to call the suspend function
        lifecycleScope.launch(Dispatchers.IO) { // Use lifecycleScope bound to the fragment's lifecycle
            try {
                // Очищаем токен (вызываем suspend функцию из корутины)
                repository.logout() 
                Log.d(TAG, "logoutAndRelogin: токен авторизации очищен")

                // Switch back to the Main thread for UI operations (Intent, startActivity, finish)
                withContext(Dispatchers.Main) {
                    // Переходим на экран авторизации
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    
                    // Закрываем текущее активити
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "logoutAndRelogin: ошибка при выходе из аккаунта", e)
                // Optionally show a toast or message on the Main thread if logout fails
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка при выходе: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Показываем диалог с предложением выйти и войти заново
    private fun showAuthErrorDialog() {
        try {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Проблема с авторизацией")
                .setMessage("Ваша сессия устарела или токен недействителен. Необходимо войти в аккаунт заново.")
                .setPositiveButton("Войти заново") { _, _ ->
                    logoutAndRelogin()
                }
                .setNegativeButton("Позже") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "showAuthErrorDialog: ошибка при отображении диалога", e)
        }
    }
}