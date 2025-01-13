package com.example.deliveryapp

import LoginRepository
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryapp.databinding.ActivityStartBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding // Инициализация binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("com.example.deliveryapp.LoginActivity", "Activity запущена")

        // Устанавливаем обработчик на кнопку входа
        binding.button3.setOnClickListener {
            val username = binding.signInMail.text.toString() // Получаем текст из EditText через binding
            val password = binding.signInPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {

                performLogin(username, password) // Выполняем авторизацию
            } else {
                Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        Toast.makeText(this, "Отправка данных...", Toast.LENGTH_SHORT).show()

        val repository = LoginRepository()


        // Используем корутины для выполнения сетевого запроса
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Выполняем запрос
                Log.d("com.example.deliveryapp.LoginActivity", "Логин1 ${username} и пароль1 ${password}")

                val response = repository.login(username, password)
                Log.d("com.example.deliveryapp.LoginActivity", "Логин ${username} и пароль ${password}")

                // Обработка успешного ответа на главном потоке
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Добро пожаловать, ${response.token}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Переход на MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                // Обработка ошибок
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
}
