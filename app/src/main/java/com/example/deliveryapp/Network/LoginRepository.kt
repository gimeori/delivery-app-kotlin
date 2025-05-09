package com.example.deliveryapp.Network

import android.content.Context
import android.util.Log
import com.example.deliveryapp.Models.ActiveOrderModel
import com.example.deliveryapp.Models.OrderDetailsModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.HttpException

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import com.example.deliveryapp.Network.TokenManager
import com.google.gson.annotations.SerializedName
import com.google.gson.GsonBuilder
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PATCH
import java.io.IOException
import retrofit2.Call


data class LoginResponse(
    @SerializedName("access_token") val token: String? = null,
    @SerializedName("token_type") val tokenType: String = "bearer"
)

// Модель для тела запроса на обновление пользователя
data class UserUpdateRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password") val password: String? = null // Пароль опционален
)

// Модель для ответа после обновления пользователя (можно использовать существующую или новую)
// Например, если API возвращает обновленные данные пользователя:
data class UserProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
    // Добавьте другие поля, если API их возвращает
)

// Модель для тела запроса на обновление статуса заказа
data class OrderStatusUpdateRequest(
    @SerializedName("order_status") val orderStatus: String
)

interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "",
        @Field("scope") scope: String = "",
        @Field("client_id") clientId: String = "",
        @Field("client_secret") clientSecret: String = ""
    ): LoginResponse

    @GET("orders/")
    suspend fun getOrders(): List<ActiveOrderModel>
    
    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): OrderDetailsModel
    
    @POST("logout")
    suspend fun logout(): ResponseBody
    
    @POST("auth/logout")
    suspend fun logoutAlternative(): ResponseBody
    
    // Метод для обновления данных пользователя
    @PATCH("auth/{id}")
    suspend fun patchUser(
        @Path("id") userId: Int,
        @Body userUpdate: UserUpdateRequest
    ): UserProfileResponse // Укажите правильный тип ответа, если он отличается
    
    // Метод для обновления статуса заказа
    @PATCH("orders/update/{id}/")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body statusUpdate: OrderStatusUpdateRequest
    ): retrofit2.Response<Unit>

    @GET("orders/courier/stats")
    suspend fun getCourierStats(): com.example.deliveryapp.Models.CourierStatsModel

    @GET("auth/me")
    suspend fun getUserProfile(): com.example.deliveryapp.Models.UserProfileModel
    
    @PATCH("auth/me")
    suspend fun updateUserProfileMe(@Body userUpdate: UserUpdateRequest): com.example.deliveryapp.Models.UserProfileModel

    @GET("orders/delivered")
    suspend fun getDeliveredOrders(): List<com.example.deliveryapp.Models.DeliveredOrderModel>
}


class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    private val TAG = "AuthInterceptor"
    
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        
        if (request.url.encodedPath.contains("auth/login")) {
            Log.d(TAG, "intercept: Запрос на /auth/login, заголовок авторизации не добавляется.")
            return chain.proceed(request)
        }
        
        val token = tokenManager.getToken()
        
        if (token == null) {
            Log.w(TAG, "intercept: Токен отсутствует для запроса ${request.url}.")
            if (request.url.encodedPath.contains("orders/")) {
                Log.e(TAG, "intercept: ПОПЫТКА ЗАПРОСА К /orders/ БЕЗ ТОКЕНА!")
                return okhttp3.Response.Builder()
                    .request(request)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(401)
                    .message("Client error: Token is missing")
                    .body("".toResponseBody(null))
                    .build()
            }
            return chain.proceed(request)
        }
        
        val authHeader = "Bearer $token"
        Log.d(TAG, "intercept: Добавление заголовка авторизации к ${request.url}: ${authHeader.take(30)}...")
        
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", authHeader)
            .build()
            
        return chain.proceed(authenticatedRequest)
    }
}


public class LoginRepository(private val context: Context) {
    private val TAG = "LoginRepository"
    private val tokenManager = TokenManager.getInstance(context)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = AuthInterceptor(tokenManager)
    
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://fastapi-pizzatime-delivery.onrender.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(ApiService::class.java)

    suspend fun login(username: String, password: String): LoginResponse {
        Log.d(TAG, "login: Отправка запроса авторизации: username=$username")
        try {
            tokenManager.clearToken()
            Log.d(TAG, "login: Старый токен очищен (если был).")
            val response = api.login(username, password)
            if (response.token != null && response.token.isNotEmpty()) {
                Log.d(TAG, "login: Получен токен: ${response.token.take(15)}...")
                if (!response.token.matches("""^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$""".toRegex())) {
                    Log.w(TAG, "login: Токен имеет нестандартный формат: ${response.token.take(20)}...")
                }
                tokenManager.resetForcedLogout()
                tokenManager.saveToken(response.token)
                tokenManager.saveUsername(username)
                Log.d(TAG, "login: Имя пользователя сохранено из логина: $username")
                val savedToken = tokenManager.getToken()
                if (savedToken != null) {
                    Log.d(TAG, "login: Проверка сохраненного токена - OK: ${savedToken.take(15)}...")
                } else {
                    Log.e(TAG, "login: ОШИБКА - токен не сохранился после вызова saveToken!")
                }
            } else {
                Log.e(TAG, "login: Получен пустой или null токен от сервера!")
                throw IllegalStateException("Сервер вернул пустой токен")
            }
            return response
        } catch (e: Exception) {
            if (e is HttpException) {
                Log.e(TAG, "login: HTTP ошибка ${e.code()} при авторизации", e)
            } else {
                Log.e(TAG, "login: Общая ошибка при выполнении запроса авторизации", e)
            }
            throw e
        }
    }
    
    suspend fun getOrders(): List<ActiveOrderModel> {
        Log.d(TAG, "getOrders: Запрос на получение списка заказов...")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "getOrders: Попытка запроса заказов без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться перед запросом заказов (токен отсутствует или невалиден)")
        }
         Log.d(TAG, "getOrders: Токен найден (${token.take(15)}...), отправка запроса...")
        try {
            val orders = api.getOrders()
            Log.d(TAG, "getOrders: Получено ${orders.size} заказов.")
            if (orders.isEmpty()) {
                Log.w(TAG, "getOrders: Получен пустой список заказов от сервера.")
            } else {
                 orders.take(5).forEachIndexed { index, order ->
                    Log.d(TAG, "getOrders: Заказ $index: ID=${order.getId()}, Статус=${order.getOrderStatus()}, Сумма=${order.getTotalPrice()}, Форматированная=${order.getFormattedPrice()}")
                }
                if (orders.size > 5) {
                     Log.d(TAG, "getOrders: ... и еще ${orders.size - 5} заказов.")
                }
            }
            return orders
        } catch (e: Exception) {
             val isUnauthorized = e is HttpException && e.code() == 401
             Log.e(TAG, "getOrders: Ошибка при получении списка заказов. Unauthorized: $isUnauthorized", e)
            if (isUnauthorized) {
                Log.w(TAG, "getOrders: Ошибка авторизации 401. Токен недействителен или истек. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }
    
    fun isLoggedIn(): Boolean {
        val token = tokenManager.getToken()
        val loggedIn = token != null && token.isNotEmpty()
        Log.d(TAG, "isLoggedIn: Проверка статуса авторизации. Токен есть? $loggedIn")
        return loggedIn
    }
    
    suspend fun logout() {
        Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Начало процесса выхода")
        try {
            val token = tokenManager.getToken()
            Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Текущий токен: ${token?.take(10) ?: "отсутствует"}")

            if (token == null || token.isBlank()) {
                Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Токен отсутствует, выполняем локальный выход")
                // Ensure token is cleared even if it was already null/blank
            } else {
                // Try standard endpoint first
                try {
                    Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Пробуем endpoint /logout")
                    api.logout()
                    Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): УСПЕХ - API logout() вызван успешно")
                } catch (e: HttpException) {
                     if (e.code() == 404) { // Specifically handle 404 for standard endpoint
                         Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Endpoint /logout не найден (404), пробуем auth/logout")
                         try {
                            api.logoutAlternative()
                            Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): УСПЕХ - альтернативный метод auth/logout сработал")
                         } catch (eAlt: Exception) {
                              Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): И альтернативный endpoint auth/logout не сработал: ${eAlt.message}")
                              // Log error but proceed to clear token
                         }
                     } else {
                        // Log other HTTP errors for standard endpoint
                        Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Ошибка HTTP с endpoint /logout - Код ${e.code()}: ${e.message}")
                     }
                } catch (e: Exception) {
                    // Log general errors for standard endpoint
                    Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Общая ошибка с endpoint /logout - ${e.javaClass.simpleName}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            // Catch potential errors getting the token, though unlikely
            Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Общая ошибка до вызова API - ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            // Force clear token locally regardless of API call outcomes
            Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): ПРИНУДИТЕЛЬНАЯ очистка токена")
            tokenManager.clearToken()
            val finalCheck = isLoggedIn()
            Log.e("LOGOUT_DEBUG", "LoginRepository.logout(): Финальный статус: isLoggedIn=$finalCheck")
        }
    }
    
    fun logoutSync() {
        Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): Начало локального выхода")
        try {
            val tokenBefore = tokenManager.getToken()
            Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): Токен до очистки: ${tokenBefore?.take(10) ?: "отсутствует"}")
            tokenManager.clearToken()
            val isStillLoggedIn = isLoggedIn()
            Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): Статус после очистки: isLoggedIn=$isStillLoggedIn")
            if (isStillLoggedIn) {
                Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): ОШИБКА! Пользователь все еще авторизован!")
                tokenManager.clearToken()
            } else {
                Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): Успешный выход")
            }
        } catch (e: Exception) {
            Log.e("LOGOUT_DEBUG", "LoginRepository.logoutSync(): Ошибка - ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }

    // Reverted getOrderDetails to return OrderDetailsModel directly
    suspend fun getOrderDetails(orderId: Int): OrderDetailsModel {
        Log.d(TAG, "getOrderDetails: Запрос на получение деталей заказа ID=$orderId")
        Log.e("ORDER_DETAILS_DEBUG", "НАЧАЛО getOrderDetails для ID=$orderId")
        val token = tokenManager.getToken()
        Log.e("ORDER_DETAILS_DEBUG", "Токен: ${token?.take(10) ?: "ОТСУТСТВУЕТ"}")

        if (token == null || token.isBlank()) {
            Log.e(TAG, "getOrderDetails: Попытка запроса заказа без валидного токена.")
            Log.e("ORDER_DETAILS_DEBUG", "ОШИБКА: Токен отсутствует или пустой")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться перед запросом заказа (токен отсутствует или невалиден)")
        }
        Log.d(TAG, "getOrderDetails: Токен найден, отправка запроса...")
        Log.e("ORDER_DETAILS_DEBUG", "Токен найден, отправляем запрос к API...")

        try {
            Log.e("ORDER_DETAILS_DEBUG", "Перед вызовом api.getOrder($orderId)")
            val orderDetails = api.getOrder(orderId)
            Log.e("ORDER_DETAILS_DEBUG", "УСПЕХ: Получены детали заказа ID=${orderDetails.order_id}")
            Log.d(TAG, "getOrderDetails: Получены детали заказа: ID=${orderDetails.order_id}, Сумма=${orderDetails.order_total_price}")
            return orderDetails
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "getOrderDetails: Ошибка при получении деталей заказа. Unauthorized: $isUnauthorized", e)
            Log.e("ORDER_DETAILS_DEBUG", "ОШИБКА запроса: ${e.javaClass.simpleName} - ${e.message}")

            if (isUnauthorized) {
                Log.w(TAG, "getOrderDetails: Ошибка авторизации 401. Токен недействителен или истек. Очистка токена.")
                Log.e("ORDER_DETAILS_DEBUG", "Unauthorized 401 - очищаем токен")
                tokenManager.clearToken()
            }
            // Re-throw the original exception after handling unauthorized case
            throw e
        }
    }
    
    // Keep updateUserProfile as is
    suspend fun updateUserProfile(userId: Int, updateRequest: UserUpdateRequest): UserProfileResponse {
        Log.d(TAG, "updateUserProfile: Запрос на обновление профиля для userId=$userId")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "updateUserProfile: Попытка обновления профиля без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться для обновления профиля")
        }
        Log.d(TAG, "updateUserProfile: Токен найден, отправка PATCH запроса...")
        
        try {
            val response = api.patchUser(userId, updateRequest)
            Log.d(TAG, "updateUserProfile: Профиль успешно обновлен для userId=$userId. Новое имя: ${response.name}")
            
            if (updateRequest.name != null && updateRequest.name != tokenManager.getUsername()) {
                tokenManager.saveUsername(updateRequest.name)
                Log.d(TAG, "updateUserProfile: Сохранено новое имя пользователя: ${updateRequest.name}")
            }
            
            return response
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "updateUserProfile: Ошибка при обновлении профиля userId=$userId. Unauthorized: $isUnauthorized", e)
            if (isUnauthorized) {
                Log.w(TAG, "updateUserProfile: Ошибка авторизации 401. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }

    // Keep updateOrderStatus as is
    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Unit {
        Log.d(TAG, "updateOrderStatus: Запрос на обновление статуса заказа ID=$orderId на '$newStatus'")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "updateOrderStatus: Попытка обновления статуса без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться для обновления статуса заказа")
        }
        
        val requestBody = OrderStatusUpdateRequest(orderStatus = newStatus)
        Log.d(TAG, "updateOrderStatus: Токен найден, отправка PATCH запроса с телом: $requestBody")
        
        try {
            val response = api.updateOrderStatus(orderId, requestBody)
            if (response.isSuccessful) {
                 Log.d(TAG, "updateOrderStatus: Статус заказа ID=$orderId успешно обновлен на '$newStatus' (Код: ${response.code()})")
            } else {
                 Log.e(TAG, "updateOrderStatus: Сервер вернул ошибку при обновлении статуса. Код: ${response.code()}, Сообщение: ${response.message()}")
                 throw HttpException(response)
            }
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "updateOrderStatus: Ошибка при обновлении статуса заказа ID=$orderId. Unauthorized: $isUnauthorized", e)
            if (isUnauthorized) {
                Log.w(TAG, "updateOrderStatus: Ошибка авторизации 401. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }

    // Добавляю метод для получения статистики курьера
    suspend fun getCourierStats(): com.example.deliveryapp.Models.CourierStatsModel {
        Log.d(TAG, "getCourierStats: Запрос на получение статистики курьера")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "getCourierStats: Попытка запроса статистики без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться перед запросом статистики (токен отсутствует или невалиден)")
        }
        Log.d(TAG, "getCourierStats: Токен найден, отправка запроса...")
        
        try {
            val stats = api.getCourierStats()
            Log.d(TAG, "getCourierStats: Получена статистика: доставлено сегодня=${stats.ordersDeliveredToday}, всего=${stats.ordersDeliveredAllTime}")
            return stats
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "getCourierStats: Ошибка при получении статистики. Unauthorized: $isUnauthorized", e)
            
            if (isUnauthorized) {
                Log.w(TAG, "getCourierStats: Ошибка авторизации 401. Токен недействителен или истек. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }

    // Получение данных текущего пользователя
    suspend fun getUserProfile(): com.example.deliveryapp.Models.UserProfileModel {
        Log.d(TAG, "getUserProfile: Запрос на получение профиля пользователя")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "getUserProfile: Попытка запроса профиля без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться для получения профиля")
        }
        Log.d(TAG, "getUserProfile: Токен найден, отправка запроса...")
        
        try {
            val profile = api.getUserProfile()
            Log.d(TAG, "getUserProfile: Получены данные пользователя: ID=${profile.id}, Email=${profile.email}")
            return profile
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "getUserProfile: Ошибка при получении профиля. Unauthorized: $isUnauthorized", e)
            
            if (isUnauthorized) {
                Log.w(TAG, "getUserProfile: Ошибка авторизации 401. Токен недействителен или истек. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }
    
    // Обновление данных текущего пользователя (без указания ID)
    suspend fun updateUserProfileMe(updateRequest: UserUpdateRequest): com.example.deliveryapp.Models.UserProfileModel {
        Log.d(TAG, "updateUserProfileMe: Запрос на обновление профиля текущего пользователя")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "updateUserProfileMe: Попытка обновления профиля без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться для обновления профиля")
        }
        Log.d(TAG, "updateUserProfileMe: Токен найден, отправка PATCH запроса...")
        
        try {
            val response = api.updateUserProfileMe(updateRequest)
            Log.d(TAG, "updateUserProfileMe: Профиль успешно обновлен. Email: ${response.email}")
            
            if (updateRequest.name != null && updateRequest.name != tokenManager.getUsername()) {
                tokenManager.saveUsername(updateRequest.name)
                Log.d(TAG, "updateUserProfileMe: Сохранено новое имя пользователя: ${updateRequest.name}")
            }
            
            return response
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "updateUserProfileMe: Ошибка при обновлении профиля. Unauthorized: $isUnauthorized", e)
            if (isUnauthorized) {
                Log.w(TAG, "updateUserProfileMe: Ошибка авторизации 401. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }

    // Метод для получения списка выполненных заказов
    suspend fun getDeliveredOrders(): List<com.example.deliveryapp.Models.DeliveredOrderModel> {
        Log.d(TAG, "getDeliveredOrders: Запрос на получение выполненных заказов")
        val token = tokenManager.getToken()
        if (token == null || token.isBlank()) {
            Log.e(TAG, "getDeliveredOrders: Попытка запроса заказов без валидного токена.")
            tokenManager.clearToken()
            throw IllegalStateException("Необходимо авторизоваться перед запросом заказов (токен отсутствует или невалиден)")
        }
        Log.d(TAG, "getDeliveredOrders: Токен найден, отправка запроса...")
        
        try {
            val orders = api.getDeliveredOrders()
            Log.d(TAG, "getDeliveredOrders: Получено ${orders.size} выполненных заказов.")
            if (orders.isEmpty()) {
                Log.w(TAG, "getDeliveredOrders: Получен пустой список выполненных заказов от сервера.")
            } else {
                orders.take(5).forEachIndexed { index, order ->
                    Log.d(TAG, "getDeliveredOrders: Заказ $index: ID=${order.id}, Сумма=${order.totalPrice}, Адрес=${order.address}")
                }
                if (orders.size > 5) {
                    Log.d(TAG, "getDeliveredOrders: ... и еще ${orders.size - 5} заказов.")
                }
            }
            return orders
        } catch (e: Exception) {
            val isUnauthorized = e is HttpException && e.code() == 401
            Log.e(TAG, "getDeliveredOrders: Ошибка при получении списка заказов. Unauthorized: $isUnauthorized", e)
            
            if (isUnauthorized) {
                Log.w(TAG, "getDeliveredOrders: Ошибка авторизации 401. Токен недействителен или истек. Очистка токена.")
                tokenManager.clearToken()
            }
            throw e
        }
    }
}


