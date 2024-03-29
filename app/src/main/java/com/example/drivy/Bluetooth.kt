package com.example.drivy


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class Bluetooth : AppCompatActivity() {
    private val devicesList: ArrayList<BluetoothDevice> = ArrayList()
    private lateinit var devicesListView: ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    companion object {
        lateinit var bAdapter: BluetoothAdapter
        lateinit var bSocket: BluetoothSocket
        lateinit var bOutputStream: OutputStream
        lateinit var bInputStream: InputStream
        var connectedDevice: BluetoothDevice? = null
        fun isInit() = this::bAdapter.isInitialized
        var reconnecting = false

        @SuppressLint("MissingPermission")
        fun reconnect(device: BluetoothDevice, trials: Int = 1) {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val connectionThread = Thread {
                try {
                    reconnecting = true
                    bAdapter.cancelDiscovery()
                    bSocket = device.createRfcommSocketToServiceRecord(uuid)
                    bSocket.connect()
                    connectedDevice = device
                    bOutputStream = bSocket.outputStream
                    bInputStream = bSocket.inputStream
                } catch (e: Exception) {
                    if (trials < 4) reconnect(device, trials + 1)
                } finally {
                    reconnecting = false
                }
            }
            connectionThread.start()
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetooth)
        Controls.hideSystemBars(window)

        //-------------------------------------------------------------------------------------//


        //------------------------------------ Devices list -----------------------------------//
        devicesListView = findViewById(R.id.devicesList)
        //-------------------------------------------------------------------------------------//


        //------------------------------ Bluetooth initialization -----------------------------//
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bAdapter = bluetoothManager.adapter
        if (bAdapter.isEnabled) {
            devicesList.clear()
            for (device in bAdapter.bondedDevices) {
                devicesList.add(device)
            }
            updateDevicesList()
        }
        //-------------------------------------------------------------------------------------//


        //------------------------------- Enable bluetooth button -----------------------------//
        val enableBluetoothButton = findViewById<Button>(R.id.turnOnBtn)
        enableBluetoothButton.setOnClickListener {
            if (!bAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                } else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    requestBluetoothPermission.launch(enableBtIntent)
                }
            } else Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
        }
        //-------------------------------------------------------------------------------------//

        //----------------------------- Get paired devices ----------------------------//
        val getPairedDevicesButton = findViewById<Button>(R.id.pairedBtn)
        getPairedDevicesButton.setOnClickListener {
            if (bAdapter.isEnabled) {
                devicesList.clear()
                for (device in bAdapter.bondedDevices) {
                    devicesList.add(device)
                }
                updateDevicesList()
            } else {
                Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }
        //-------------------------------------------------------------------------------------//

        //--------------------------------- Pair to new device --------------------------------//
        val pairToNewDeviceButton = findViewById<Button>(R.id.pairToNewDevice)
        pairToNewDeviceButton.setOnClickListener {
            val intentOpenBluetoothSettings = Intent()
            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(intentOpenBluetoothSettings)
        }
        //-------------------------------------------------------------------------------------//

        //--------------------------------- Controller --------------------------------//

        findViewById<Button>(R.id.controllerBtn).setOnClickListener {
            val intent = Intent(this, Controller::class.java)
            startActivity(intent)
            finish()
        }
        //-------------------------------------------------------------------------------------//

        //------------------------------------- Connection ------------------------------------//
        devicesListView.setOnItemClickListener { _, child, position, _ ->
            val device = devicesList[position]
            val deviceInView = child as TextView
            var connecting = true

            val toast = Toast.makeText(this, "Device is not available", Toast.LENGTH_SHORT)

            val animationThread = Thread {
                val name = device.name
                while (connecting) {
                    for (i in 1..3) {
                        try {
                            deviceInView.text = "$name (Connecting${".".repeat(i)})"
                            Thread.sleep(250)
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                }
            }

            val connectionThread = Thread {
                try {
                    if (connectedDevice != null) {
                        reconnect(connectedDevice!!)
                        connecting = false
                        animationThread.interrupt()
                        deviceInView.text = "${device.name} (Connected)"
                    } else {
                        bAdapter.cancelDiscovery()
                        bSocket = device.createRfcommSocketToServiceRecord(uuid)
                        bSocket.connect()
                        connectedDevice = device
                        connecting = false
                        animationThread.interrupt()
                        deviceInView.text = "${device.name} (Connected)"
                        bOutputStream = bSocket.outputStream
                        bInputStream = bSocket.inputStream
                    }
                } catch (e: Exception) {
                    Log.v("Error", e.printStackTrace().toString())
                    toast.show()
                    connecting = false
                    animationThread.interrupt()
                    deviceInView.text = "${device.name} (Failed to connect)"
                    Thread.sleep(1000)
                    deviceInView.text = device.name
                }
            }
            connectionThread.start()
            animationThread.start()

        }
        //-------------------------------------------------------------------------------------//


    }

    private var requestBluetoothPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    @SuppressLint("MissingPermission")
    private fun updateDevicesList() {
        val empty = findViewById<TextView>(R.id.empty)
        if (devicesList.isEmpty()) {
            empty.visibility = View.VISIBLE
            arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListOf())
            devicesListView.adapter = arrayAdapter
            arrayAdapter.notifyDataSetChanged()
        } else {
            empty.visibility = View.INVISIBLE
            val stringListOfDevices: List<String> = devicesList.map { it.name }
            arrayAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, stringListOfDevices)
            devicesListView.adapter = arrayAdapter
            arrayAdapter.notifyDataSetChanged()
        }
    }


}