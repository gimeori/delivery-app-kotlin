package com.example.deliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deliveryapp.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cityList= arrayOf("Барнаул", "Новосибирск", "Красноярск", "Москва","Томск")
        val adapter=ArrayAdapter(this@LocationActivity,android.R.layout.simple_list_item_1, cityList)
        binding.locationList.setAdapter(adapter)
        binding.locationList.setOnItemClickListener { parent, _, position, _ ->
            val selectLocation=parent.getItemAtPosition(position) as String
            showDialogAtPosition(selectLocation)
        }
    }

    @SuppressLint("MissingInflatedId")
    fun showDialogAtPosition(location: String){
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog,null)
        val dialogBuilder=AlertDialog.Builder(this@LocationActivity)
        dialogBuilder.setView(dialogView)
        val dialog=dialogBuilder.create()
        dialogView.findViewById<AppCompatButton>(R.id.btn_yes).setOnClickListener{
            startActivityWithLocation(location)
            dialog.dismiss()
        }
        dialogView.findViewById<AppCompatButton>(R.id.btn_cancel)?.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun startActivityWithLocation(location: String){
        val intent= Intent(this@LocationActivity, MainActivity::class.java)
        intent.putExtra("location",location)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
}