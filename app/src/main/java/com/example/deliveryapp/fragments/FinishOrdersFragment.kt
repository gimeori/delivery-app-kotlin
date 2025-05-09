package com.example.deliveryapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import com.example.deliveryapp.R
import com.example.deliveryapp.databinding.FragmentFinishOrdersBinding
import com.example.deliveryapp.Network.LoginRepository
import com.example.deliveryapp.Adapters.DeliveredOrdersAdapter
import com.example.deliveryapp.Models.DeliveredOrderModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinishOrdersFragment : Fragment() {

    private var _binding: FragmentFinishOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DeliveredOrdersAdapter
    private lateinit var loginRepository: LoginRepository
    
    private val TAG = "FinishOrdersFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate вызван")
        loginRepository = LoginRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView вызван")
        _binding = FragmentFinishOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated вызван")
        
        try {
            // Проверяем, что все UI элементы существуют
            if (binding.recyclerViewFinishOrders == null) {
                Log.e(TAG, "recyclerViewFinishOrders не найден в макете!")
            } else {
                Log.d(TAG, "recyclerViewFinishOrders найден")
            }
            
            if (binding.progressBarFinishOrders == null) {
                Log.e(TAG, "progressBarFinishOrders не найден в макете!")
            } else {
                Log.d(TAG, "progressBarFinishOrders найден")
            }
            
            setupRecyclerView()
            
            // Добавляем тестовые данные для проверки адаптера
            addTestData()
            
            // После теста загружаем реальные данные
            loadDeliveredOrders()
            
            // Для тестирования добавим текстовое сообщение
            Toast.makeText(requireContext(), "Загрузка заказов...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при инициализации: ${e.message}", e)
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addTestData() {
        // Создаем тестовые данные
        val testOrders = listOf(
            DeliveredOrderModel(
                id = 1,
                createdAt = "01.05.2023",
                address = "Тестовый адрес 1",
                orderStatus = "delivered",
                userId = 1,
                totalPrice = 1000
            ),
            DeliveredOrderModel(
                id = 2,
                createdAt = "02.05.2023",
                address = "Тестовый адрес 2",
                orderStatus = "delivered",
                userId = 1,
                totalPrice = 2000
            )
        )
        
        // Отображаем тестовые данные
        adapter.submitList(testOrders)
        Log.d(TAG, "Добавлены тестовые данные (${testOrders.size} записей)")
    }
    
    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView вызван")
        try {
            adapter = DeliveredOrdersAdapter()
            binding.recyclerViewFinishOrders.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewFinishOrders.adapter = adapter
            Log.d(TAG, "RecyclerView настроен успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при настройке RecyclerView: ${e.message}", e)
        }
    }
    
    private fun loadDeliveredOrders() {
        Log.d(TAG, "loadDeliveredOrders вызван")
        binding.progressBarFinishOrders.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Начинаем запрос к API для получения заказов")
                val orders = loginRepository.getDeliveredOrders()
                Log.d(TAG, "Получен ответ от API: ${orders.size} заказов")
                
                // Логируем полученные заказы
                if (orders.isNotEmpty()) {
                    orders.forEach { order ->
                        Log.d(TAG, "Заказ ID: ${order.id}, Адрес: ${order.address}, Сумма: ${order.totalPrice}")
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            Log.d(TAG, "Передаем данные в адаптер")
                            adapter.submitList(orders)
                            Toast.makeText(requireContext(), 
                                "Загружено ${orders.size} заказов", 
                                Toast.LENGTH_SHORT).show()
                            binding.progressBarFinishOrders.visibility = View.GONE
                        } else {
                            Log.w(TAG, "Фрагмент уже не прикреплен к активности")
                        }
                    }
                } else {
                    Log.d(TAG, "Получен пустой список заказов")
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Нет выполненных заказов", Toast.LENGTH_SHORT).show()
                            binding.progressBarFinishOrders.visibility = View.GONE
                            
                            // Оставляем тестовые данные для демонстрации
                            // adapter.submitList(testOrders)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке: ${e.message}", e)
                e.printStackTrace()
                
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
                        binding.progressBarFinishOrders.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView вызван")
        _binding = null
    }
}