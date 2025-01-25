package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryapp.databinding.ActivityStartBinding



class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.button3.setOnClickListener{
            Log.d("com.example.deliveryapp.LoginActivity", "Кнопка нажата")
            val intent=Intent(this@StartActivity, MainActivity :: class.java)
            startActivity(intent)
            finish()
        }
    }
}
