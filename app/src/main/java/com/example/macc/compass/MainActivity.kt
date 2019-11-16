package com.example.macc.compass

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import kotlin.math.roundToInt
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings_dialog.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var mLastAccelerometer = FloatArray(3)
    private var mLastMagnetometer = FloatArray(3)
    private var mLastGyroscope = FloatArray(3)
    private var mRotMatrix = FloatArray(9)
    private var mOrientation = FloatArray(3)
    private var accOK :Boolean = false
    private var magOK :Boolean = false
    private var gyrOK :Boolean = false
    private val sample = 50

    private var ip = "127.0.0.1"
    private val port ="8000"


    private lateinit var queue :RequestQueue


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        ip = sharedPref.getString("IP", "127.0.0.1")!!

        queue  =  Volley.newRequestQueue(this)


        setContentView(R.layout.activity_main)
        Configuration.text=ip.toString()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager



        setting.setOnClickListener {

            val builder = AlertDialog.Builder(this).create()
            builder.setTitle("Set Server IP address: "+ip.toString())
            val dialogView = getLayoutInflater().inflate(R.layout.settings_dialog,null)
            dialogView.ip.hint=ip.toString()
            builder.setView(dialogView)

            dialogView.btnOK.setOnClickListener {
                ip = dialogView.ip.text.toString()

                with (sharedPref.edit()) {
                    putString("IP", ip)
                    commit()
                }
                Configuration.text=ip
                builder.dismiss()
            }

            dialogView.btnNOK.setOnClickListener {
                builder.dismiss()
            }

            builder.show()

        }



    }


    override fun onStart() {
        super.onStart()

        if ( (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) and
            (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
                )
        {
            sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)

        }

    }


    override fun onStop() {
        super.onStop()
       // sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accOK = true
            mLastAccelerometer = event.values.clone()
            val params = HashMap<String, String>()
            params["x"] = mLastAccelerometer[0].toString()
            params["y"] = mLastAccelerometer[1].toString()
            params["z"] = mLastAccelerometer[2].toString()
            params["time"] = System.currentTimeMillis().toString()
            params["type"] = "Acceleration"
            postData(JSONObject(params.toMap()))

        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magOK = true
            mLastMagnetometer = event.values.clone()
            val params = HashMap<String, String>()
            params["time"] = System.currentTimeMillis().toString()
            params["type"] = "Magnetic"
            params["x"] = mLastMagnetometer[0].toString()
            params["y"] = mLastMagnetometer[1].toString()
            params["z"] = mLastMagnetometer[2].toString()
            postData(JSONObject(params.toMap()))

        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyrOK = true
            mLastGyroscope = event.values.clone()
            val params = HashMap<String, String>()
            params["time"] = System.currentTimeMillis().toString()
            params["type"] = "Gyroscope"
            params["x"] = mLastGyroscope[0].toString()
            params["y"] = mLastGyroscope[1].toString()
            params["z"] = mLastGyroscope[2].toString()
            postData(JSONObject(params.toMap()))

        }
        if (accOK && magOK) {
            SensorManager.getRotationMatrix(
                mRotMatrix,
                null,
                mLastAccelerometer,
                mLastMagnetometer
            );
            SensorManager.getOrientation(mRotMatrix, mOrientation)
            val params = HashMap<String, String>()
            params["type"] = "Orientation"
            params["time"] = System.currentTimeMillis().toString()
            params["A"] = mOrientation[0].toString()
            params["P"] = mOrientation[1].toString()
            params["R"] = mOrientation[2].toString()

            val jsonObject = JSONObject(params.toMap())
                postData(jsonObject)
            }
    }


        fun postData(x: JSONObject) {


            val url = "http://"+ip+":"+port //Change according to the server...

            val stringRequest = JsonObjectRequest(
                Request.Method.POST, url, x,
                Response.Listener<JSONObject> { response ->
                    Log.d(
                        "POST",
                        "/post request OK! Response"
                    )
                    log.setTextColor(Color.GREEN)
                    log.text="Server is OK"

                    measures.text=x.toString()
                },
                Response.ErrorListener { error: VolleyError? ->
                    Log.e(
                        "POST",
                        "/post request NOK! Response: $error"
                    )
                    log.setTextColor(Color.RED)
                    log.text="Server is NOK"
                })

            // Add the request to the RequestQueue.
            queue.add(stringRequest)

        }
    }

