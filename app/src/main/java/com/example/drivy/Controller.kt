package com.example.drivy


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import io.github.controlwear.virtual.joystick.android.JoystickView
import com.example.drivy.Bluetooth.Companion as b
import com.example.drivy.Controls.Companion as c

class Controller : AppCompatActivity() {
    var readingThread: Thread? = null
    var read = false
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.controller)
        c.hideSystemBars(window)
        if(b.connectedDevice != null) findViewById<TextView>(R.id.deviceName).text = b.connectedDevice!!.name
        else findViewById<TextView>(R.id.deviceName).text = "No connection"


        //------------------------------- Direction mode toggle -------------------------------//
        val directionsToggleButton = findViewById<ToggleButton>(R.id.directionsToggle)
        //-------------------------------------------------------------------------------------//


        //--------------------------------- Bluetooth button ----------------------------------//
        val bButton = findViewById<ImageButton>(R.id.bluetooth)
        bButton.setOnClickListener{
            val intent = Intent(this, Bluetooth::class.java)
            startActivity(intent)
            finish()
        }
        //-------------------------------------------------------------------------------------//


        //-------------------------------- Movement info button -------------------------------//
        val movementInfoButton= findViewById<ImageButton>(R.id.movementInfo)
        movementInfoButton.setOnClickListener {
            val msgHtml = "Moving North East will send :<br>" +
                    "<font color = 'blue'>4 Directions : </font>2 commands : '${c.front}' -> '${c.right}'<br>" +
                    "<font color = 'blue'>8 Directions : </font>1 command  : '${c.front_right}'<br>" +
                    "<font color = 'red'>Note : </font> 8 Directions is recommended when using Omni wheels"
            val msg = HtmlCompat.fromHtml(msgHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            Controls.dialog(this, msg)
        }
        c.setClickEffect(movementInfoButton, "#aaaaaa")
        //-------------------------------------------------------------------------------------//


        //-------------------------------- Movement controller --------------------------------//
        val movementController = findViewById<JoystickView>(R.id.movementController)
        movementController.setOnMoveListener{ angle, strength ->
            val dir8 = directionsToggleButton.isChecked
            if(strength > 50){

                if(angle < 22.5 || angle >= 337.5) { // East

                    sendCmd(c.right)

                }else if(angle < 67.5){ // North east

                    if(dir8) sendCmd(c.front_right)
                    else {
                        sendCmd(c.front)
                        sendCmd(c.right)
                    }

                }else if(angle < 112.5) { // North

                    sendCmd(c.front)

                }else if(angle < 157.5){ // North west

                    if(dir8) sendCmd(c.front_left)
                    else {
                        sendCmd(c.front)
                        sendCmd(c.left)
                    }

                }else if(angle < 202.5) { //West

                    sendCmd(c.left)

                }else if(angle < 247.5){ //South west

                    if(dir8) sendCmd(c.back_left)
                    else {
                        sendCmd(c.back)
                        sendCmd(c.left)
                    }

                }else if(angle < 292.5) { //South

                    sendCmd(c.back)

                }else if(angle < 337.5){ //South east

                    if(dir8) sendCmd(c.back_right)
                    else {
                        sendCmd(c.back)
                        sendCmd(c.right)
                    }

                }
            }else{
                sendCmd(c.stop)
            }
        }

        //--------------------------------- Speed controller ----------------------------------//
        val speedController = findViewById<SeekBar>(R.id.speedController)
        speedController.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {
                val progress = seek?.progress
                val cmd = "${c.speed}:$progress;"
                sendCmd(cmd)
            }
        })



    }

    private fun sendCmd(cmd:String){
        if(b.isInit() && b.bAdapter.isEnabled && b.connectedDevice != null && !b.reconnecting){
            try {
                b.bOutputStream.write(cmd.toByteArray())
            }catch (e:Exception){
                b.reconnect(b.connectedDevice!!)
                //b.connectedDevice = null
                //findViewById<TextView>(R.id.deviceName).text = "No connection"
            }
        }
    }
    private fun sendCmd(cmd:Char) = sendCmd(cmd.toString())




}