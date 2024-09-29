package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deliveryapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView( binding.root)

        binding.goLoginUserPage.setOnClickListener{
            val intent= Intent(this@SignUpActivity, StartActivity :: class.java)
            startActivity(intent)
        }
        binding.button15.setOnClickListener{
            val intent=Intent(this@SignUpActivity, LocationActivity :: class.java)
            startActivity(intent)
            finish()
        }
    }
}