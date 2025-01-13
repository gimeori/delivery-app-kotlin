package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deliveryapp.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goSignUpUser.setOnClickListener{
            val intent= Intent(this@StartActivity, SignUpActivity :: class.java)
            startActivity(intent)
        }
//        binding.button3.setOnClickListener{
//            Log.d("com.example.deliveryapp.LoginActivity", "Кнопка нажата")
//            val intent=Intent(this@StartActivity, MainActivity :: class.java)
//            startActivity(intent)
//            finish()
//        }
    }
}
