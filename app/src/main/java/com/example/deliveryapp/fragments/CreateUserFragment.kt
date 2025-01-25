package com.example.deliveryapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.deliveryapp.R
import com.example.deliveryapp.databinding.FragmentCreateUserBinding

class CreateUserFragment : Fragment() {
    private lateinit var binding: FragmentCreateUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    private fun showNoAccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.no_access, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateUserBinding.inflate(inflater, container, false)


        binding.profileBtnEdit.setOnClickListener {
            showNoAccessDialog()
        }

        return binding.root
    }
}
