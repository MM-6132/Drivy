package com.example.drivy.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.drivy.Bluetooth
import com.example.drivy.R

class BluetoothFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.bluetooth, container, false)
        val turnOnBtn = view?.findViewById<ImageButton>(R.id.turnOnBtn)
        return view
    }
}