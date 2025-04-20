package com.example.deliveryapp

import com.example.deliveryapp.Network.LoginRepository
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.deliveryapp.databinding.ActivityMainBinding
import com.example.deliveryapp.fragments.ActiveOrdersFragment
import com.example.deliveryapp.fragments.CreateUserFragment
import com.example.deliveryapp.fragments.FinishOrdersFragment
import com.example.deliveryapp.fragments.ProcessOrdersFragment
import com.example.deliveryapp.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: LoginRepository
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = LoginRepository(this)
        
        // Проверяем статус авторизации при старте
        val isLoggedIn = repository.isLoggedIn()
        Log.d(TAG, "onCreate: Статус авторизации при запуске: $isLoggedIn")
        
        if (!isLoggedIn) {
            Log.w(TAG, "onCreate: Пользователь не авторизован, перенаправляем на экран входа")
            redirectToLogin()
            return
        }
        
        val bottomNavView=findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navigationView=findNavController(R.id.fragment_container)
        bottomNavView.setupWithNavController(navigationView)
    }
    
    private fun redirectToLogin() {
        // Перенаправляем на экран входа
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    // Метод для отладки нажатия кнопки выхода
    fun logDebugClick(view: android.view.View) {
        Log.e("BUTTON_CLICK", "КНОПКА ${view.id} БЫЛА НАЖАТА!")
        Toast.makeText(this, "Кнопка нажата!", Toast.LENGTH_SHORT).show()
    }
}