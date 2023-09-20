package com.example.driveefficiencyapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.*


private var acceleration: Float = 0.0f // holds accelerometer value
private var appRunning = false
private var pointsEarned = 0.000
private var possiblePoints = 0.000

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager


    override fun onCreate(savedInstanceState: Bundle?) {
        // sets up the layout of our app
        super.onCreate(savedInstanceState)

        setContent {
            DriveEfficiencyApp()

        }


        setUpSensorStuff()
    }

    // sets up sensorManager 
    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also{
            sensorManager.registerListener(this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    // gets acceleration value from accelerometer
    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION){
            acceleration = event.values[0]
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        // The app is running
        appRunning = true
    }

    override fun onPause() {
        super.onPause()
        // The app is not running
        appRunning = false
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}


// sets up layout of our app
@Composable
fun DriveEfficiencyApp() {
    var averageScore by remember { mutableStateOf(0.0) }
    var isDriving by remember { mutableStateOf(false) }

    var currentAcceleration by remember { mutableStateOf(0.0f) }



    // Grading algorithm parameters
    val minThreshold = 0.02f // <---- Alter these to fit needs.
    val maxThreshold = 1.00f
    val coroutineScope = rememberCoroutineScope()




    LaunchedEffect(acceleration) {
        currentAcceleration = acceleration
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Add an image in the top left corner
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(256.dp)
        )

        Text(
            text = "Acceleration Efficiency",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )


    // Start of algorithm
    Button(
            onClick = {
                isDriving = true

                pointsEarned = 0.0
                possiblePoints = 0.0
                coroutineScope.launch {
                    while (isDriving) {
                        if (kotlin.math.abs(currentAcceleration) >= minThreshold) {
                            if (kotlin.math.abs(currentAcceleration) <= maxThreshold) {
                                pointsEarned += 0.1
                                possiblePoints += 0.1
                            }
                        } else {
                            possiblePoints += 0.1

                        }

                        delay(100)
                    }


                }
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isDriving
        ) {
            Text(text = "Start Drive")
        }

        Button(
            onClick = {
                isDriving = false

                averageScore= (pointsEarned/ possiblePoints)*100 // endo of algorithm
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = isDriving
        ) {
            Text(text = "End Drive")
        }

        if (!isDriving) {
            Text(
                text = "Score: $averageScore",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        }

        //Debugging: makes acceleration visible
        /*
        Text(
            text = "Acceleration: $currentAcceleration",
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
        */

    }

}







