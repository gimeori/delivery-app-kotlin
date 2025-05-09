package com.example.deliveryapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.Models.DeliveredOrderModel
import com.example.deliveryapp.Network.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FinishOrdersViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    
    private val _deliveredOrders = MutableLiveData<List<DeliveredOrderModel>>()
    val deliveredOrders: LiveData<List<DeliveredOrderModel>> = _deliveredOrders
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun loadDeliveredOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val orders = withContext(Dispatchers.IO) {
                    loginRepository.getDeliveredOrders()
                }
                
                _deliveredOrders.value = orders
            } catch (e: IOException) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Требуется авторизация"
                    403 -> "Доступ запрещен"
                    404 -> "Данные не найдены"
                    else -> "Ошибка сервера: ${e.code()}"
                }
                _errorMessage.value = errorMsg
            } catch (e: Exception) {
                _errorMessage.value = "Произошла ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class FinishOrdersViewModelFactory(private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinishOrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinishOrdersViewModel(loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 