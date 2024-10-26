package com.example.deliveryapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.deliveryapp.R
import com.example.deliveryapp.databinding.ActivityLocationBinding
import com.example.deliveryapp.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    @SuppressLint("MissingInflatedId")
    private fun showDialogAtPosition() {
        val dialogView = layoutInflater.inflate(R.layout.done_dialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.profileBtnEdit.setOnClickListener {
            showDialogAtPosition()
        }
        return binding.root
    }

}