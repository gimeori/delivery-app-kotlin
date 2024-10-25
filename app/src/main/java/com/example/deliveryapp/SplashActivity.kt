package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yandex.mapkit.MapKitFactory

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("d00f0dd9-f999-4556-9da4-9086f95dbc06")
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent= Intent(this@SplashActivity, StartActivity :: class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}