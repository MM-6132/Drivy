package com.example.drivy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.example.drivy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permission = Manifest.permission.BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 13456432155.toInt())
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission, Manifest.permission.BLUETOOTH_SCAN), 13456432154.toInt())
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission, Manifest.permission.CAMERA), 13456432156.toInt())
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val transaction = supportFragmentManager.beginTransaction()
        initUI(transaction)
    }

    private fun initUI(transaction: FragmentTransaction){
        binding.toolbar.ivBack.setOnClickListener() {
            Toast.makeText(this, "back button clicked", Toast.LENGTH_SHORT).show()
        }
        val bluetoothBtn = findViewById<ImageButton>(R.id.ivBluetooth)
        bluetoothBtn.setOnClickListener{
            val intent = Intent(this, Bluetooth::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.button_controller).setOnClickListener{
            val intent = Intent(this, Controller::class.java)
            startActivity(intent)
            finish()
        }
        binding.toolbar.tvTitle.text = "Drivy"
    }

}