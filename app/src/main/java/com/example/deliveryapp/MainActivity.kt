package com.example.deliveryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.deliveryapp.databinding.ActivityMainBinding
import com.example.deliveryapp.fragments.ActiveOrdersFragment
import com.example.deliveryapp.fragments.CreateUserFragment
import com.example.deliveryapp.fragments.FinishOrdersFragment
import com.example.deliveryapp.fragments.ProcessOrdersFragment
import com.example.deliveryapp.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavView=findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navigationView=findNavController(R.id.fragment_container)
        bottomNavView.setupWithNavController(navigationView)
    }
}