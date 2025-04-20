package com.example.deliveryapp.Network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("DeliveryAppPrefs", Context.MODE_PRIVATE)
    private val TAG = "TokenManager"
    
    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_FORCED_LOGOUT = "forced_logout"
        
        @Volatile
        private var instance: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun saveToken(token: String?) {
        if (token == null) {
            Log.w(TAG, "saveToken: попытка сохранить null-токен")
            return
        }
        
        try {
            val tokenPreview = if (token.length > 15) token.substring(0, 15) + "..." else token
            Log.d(TAG, "Сохранение токена: $tokenPreview")
            
            // Сохраняем с commit() вместо apply() для мгновенного сохранения
            val result = sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putBoolean(KEY_FORCED_LOGOUT, false) // Сброс флага принудительного выхода при сохранении токена
                .commit()
                
            if (result) {
                Log.d(TAG, "saveToken: токен успешно сохранен")
                
                // Проверяем сохранение
                val savedToken = sharedPreferences.getString(KEY_TOKEN, null)
                if (savedToken != null) {
                    val savedPreview = if (savedToken.length > 10) savedToken.substring(0, 10) + "..." else savedToken
                    Log.d(TAG, "saveToken: проверка - токен в SharedPreferences: $savedPreview")
                } else {
                    Log.e(TAG, "saveToken: ОШИБКА - токен не сохранился!")
                }
            } else {
                Log.e(TAG, "saveToken: ОШИБКА - не удалось сохранить токен!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении токена", e)
        }
    }
    
    fun getToken(): String? {
        // Проверяем флаг принудительного выхода
        val forcedLogout = sharedPreferences.getBoolean(KEY_FORCED_LOGOUT, false)
        if (forcedLogout) {
            Log.d(TAG, "getToken: Установлен флаг принудительного выхода, возвращаем null независимо от наличия токена")
            return null
        }
        
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        try {
            if (token == null) {
                Log.d(TAG, "getToken: токен не найден")
            } else {
                val tokenPreview = if (token.length > 15) token.substring(0, 15) + "..." else token
                Log.d(TAG, "getToken: токен найден, начало: $tokenPreview")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении токена", e)
        }
        return token
    }
    
    fun saveUsername(username: String?) {
        if (username == null) {
            Log.w(TAG, "saveUsername: попытка сохранить null-имя пользователя")
            return
        }
        
        try {
            Log.d(TAG, "Сохранение имени пользователя: $username")
            sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении имени пользователя", e)
        }
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    fun clearToken() {
        try {
            // Получаем текущий токен для логирования
            val currentToken = getToken()
            Log.e("LOGOUT_DEBUG", "clearToken: Начинаем очистку токена. Текущий токен: ${currentToken?.take(10) ?: "отсутствует"}")
            
            // ПРИНУДИТЕЛЬНОЕ УДАЛЕНИЕ всех данных авторизации
            try {
                // 1. Полная очистка всех данных
                sharedPreferences.edit().clear().commit()
                Log.e("LOGOUT_DEBUG", "clearToken: Полная очистка SharedPreferences")
                
                // 2. Для надежности явно устанавливаем пустой токен
                sharedPreferences.edit()
                    .putString(KEY_TOKEN, "")
                    .putBoolean(KEY_FORCED_LOGOUT, true)
                    .commit()
                Log.e("LOGOUT_DEBUG", "clearToken: Установлен пустой токен и флаг принудительного выхода")
                
                // 3. Проверка после очистки
                val tokenAfterClear = sharedPreferences.getString(KEY_TOKEN, null)
                Log.e("LOGOUT_DEBUG", "clearToken: Проверка - токен после очистки: ${tokenAfterClear ?: "null"}")
                
                // 4. Для полной уверенности, сброс instance TokenManager
                instance = null
                Log.e("LOGOUT_DEBUG", "clearToken: Сброс instance TokenManager")
            } catch (e: Exception) {
                Log.e("LOGOUT_DEBUG", "clearToken: Ошибка при очистке: ${e.message}")
            }
            
            // Финальная проверка через getToken() - должен вернуть null из-за флага принудительного выхода
            val finalTokenCheck = getToken()
            Log.e("LOGOUT_DEBUG", "clearToken: Финальная проверка - getToken() возвращает ${finalTokenCheck != null}")
            
            if (finalTokenCheck != null) {
                Log.e("LOGOUT_DEBUG", "clearToken: КРИТИЧЕСКАЯ ОШИБКА! Метод getToken() все еще возвращает не-null значение!")
            } else {
                Log.e("LOGOUT_DEBUG", "clearToken: Токен успешно очищен.")
            }
        } catch (e: Exception) {
            Log.e("LOGOUT_DEBUG", "Ошибка при удалении токена: ${e.message}", e)
        }
    }
    
    fun isLoggedIn(): Boolean {
        try {
            // Проверяем наличие флага принудительного выхода
            val forcedLogout = sharedPreferences.getBoolean(KEY_FORCED_LOGOUT, false)
            if (forcedLogout) {
                Log.d(TAG, "isLoggedIn: Установлен флаг принудительного выхода, считаем пользователя не авторизованным")
                return false
            }
            
            val hasToken = getToken() != null
            Log.d(TAG, "isLoggedIn: $hasToken")
            return hasToken
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке авторизации", e)
            return false
        }
    }
    
    fun resetForcedLogout() {
        try {
            val result = sharedPreferences.edit()
                .putBoolean(KEY_FORCED_LOGOUT, false)
                .commit()
            Log.d(TAG, "resetForcedLogout: Сброс флага принудительного выхода: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сбросе флага принудительного выхода", e)
        }
    }
} 