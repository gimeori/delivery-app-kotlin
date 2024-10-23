package com.example.deliveryapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.deliveryapp.R
import com.example.deliveryapp.databinding.FragmentProcessOrdersBinding


class ProcessOrdersFragment : Fragment() {

private lateinit var binding:FragmentProcessOrdersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding=FragmentProcessOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

}