package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryapp.Network.LoginRepository
import com.example.deliveryapp.databinding.ActivityStartBinding
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding // Инициализация binding
    private lateinit var repository: LoginRepository
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("d00f0dd9-f999-4556-9da4-9086f95dbc06")

        // Инициализация View Binding
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = LoginRepository(this)
        
        // Проверяем, пришли ли мы из процесса выхода
        val isLogoutSuccess = intent.getBooleanExtra("LOGOUT_SUCCESS", false)
        val isLogoutError = intent.getBooleanExtra("LOGOUT_ERROR", false)
        
        if (isLogoutSuccess || isLogoutError) {
            // Если мы пришли из процесса выхода, принудительно выполняем локальный выход
            // для гарантии, что токены очищены
            Log.d(TAG, "Пришли из процесса выхода (успех=$isLogoutSuccess, ошибка=$isLogoutError), очищаем токены")
            repository.logoutSync()
            
            if (isLogoutSuccess) {
                Toast.makeText(this, "Вы успешно вышли из аккаунта", Toast.LENGTH_SHORT).show()
            } else if (isLogoutError) {
                Toast.makeText(this, "Выход с ошибкой, токен очищен локально", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Проверяем наличие токена и что не было принудительного выхода
            val isLoggedIn = repository.isLoggedIn()
            Log.d(TAG, "Проверка авторизации: $isLoggedIn")
            
            if (isLoggedIn) {
                Log.d(TAG, "Пользователь уже авторизован, переходим в MainActivity")
                navigateToMainActivity()
                return
            }
        }

        Log.d(TAG, "Activity запущена, ожидание ввода учетных данных")

        // Устанавливаем обработчик на кнопку входа
        binding.button3.setOnClickListener {
            val username = binding.signInMail.text.toString() // Получаем текст из EditText через binding
            val password = binding.signInPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "Начинаем авторизацию для пользователя: $username")
                performLogin(username, password) // Выполняем авторизацию
            } else {
                Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        // Проверка на пустые поля
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "Отправка данных...", Toast.LENGTH_SHORT).show()

        // Используем корутины для выполнения сетевого запроса
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Выполняем запрос
                Log.d(TAG, "Отправка запроса авторизации: username=$username, password=$password")

                val response = repository.login(username, password)
                Log.d(TAG, "Успешный ответ от сервера. Токен получен: ${response.token?.take(15)}...")
                
                // Сервер не возвращает имя пользователя, используем введенный логин
                Log.d(TAG, "Используем логин как имя пользователя: $username")

                // Обработка успешного ответа на главном потоке
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Авторизация успешна!",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                // Обработка ошибок
                Log.e(TAG, "Ошибка при авторизации", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка авторизации: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun navigateToMainActivity() {
        // Переход на MainActivity
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
