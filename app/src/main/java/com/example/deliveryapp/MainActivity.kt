package com.example.deliveryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.deliveryapp.databinding.ActivityMainBinding
import com.example.deliveryapp.fragments.ActiveOrdersFragment
import com.example.deliveryapp.fragments.CreateUserFragment
import com.example.deliveryapp.fragments.FinishOrdersFragment
import com.example.deliveryapp.fragments.ProcessOrdersFragment
import com.example.deliveryapp.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeFragment(ActiveOrdersFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.active_orders ->{
                    changeFragment(ActiveOrdersFragment())
                }
                R.id.process_orders ->{
                    changeFragment(ProcessOrdersFragment())
                }
                R.id.finish_orders ->{
                    changeFragment(FinishOrdersFragment())
                }
                R.id.create_user ->{
                    changeFragment(CreateUserFragment())
                }
                R.id.profile ->{
                    changeFragment(ProfileFragment())
                }
            }
            return@setOnItemSelectedListener true
        }

    }
    fun changeFragment(fragment: Fragment){
        val fragmentManager=supportFragmentManager
        val fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()

    }
}