package com.example.mavsdkuploadmissionsample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.mavsdkuploadmissionsample.ui.theme.MavsdkUploadMissionSampleTheme
import io.mavsdk.MavsdkEventQueue
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer
import io.mavsdk.mission_raw.MissionRaw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val DRONE = "udp://localhost:14552"
    private val SIMULATION = "udp://:14540"
    private val BACKEND_IP_ADDRESS = "127.0.0.1"

    private var mavSdkServer: MavsdkServer = MavsdkServer()
    private var mavSdkSystem: System? = null
    private var missionRaw : MissionRaw? = null

    private val sampleMissionRaw = createMissionPlanRaw()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startMavsdkServer()

        setContent {
            MavsdkUploadMissionSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box (
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text (
                            text = "Sample app mission raw.",
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                        Button (
                            onClick = {
                                uploadMission(sampleMissionRaw)
                            },
                            enabled = missionRaw != null,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(text = "Upload Mission", color = Color.Black)
                        }
                    }

                }
            }
        }
    }

    fun uploadMission(missionPlan: List<MissionRaw.MissionItem>) {
        if (missionRaw == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val completable = missionRaw!!.uploadMission(missionPlan)
            completable.subscribe({
                Log.d("Mavsdk-Upload-Mission", "Upload mission success.")
            },{ e ->
                Log.d("Mavsdk-Upload-Mission", "Upload Error: ${e.message}")
            })
        }
    }

    fun startMavsdkServer() {
        CoroutineScope(Dispatchers.IO).launch {
            MavsdkEventQueue.executor().execute {
                Log.d("Mavsdk-Upload-Mission", "Start Connection")
                val port = mavSdkServer.run(DRONE, 0)
                mavSdkSystem = System(BACKEND_IP_ADDRESS, port)

                Log.d("Mavsdk-Upload-Mission", "Drone Connected")
                missionRaw = mavSdkSystem!!.missionRaw
            }
        }
    }

    private fun createMissionPlanRaw() : MutableList<MissionRaw.MissionItem> {
        val missionRawItems = mutableListOf<MissionRaw.MissionItem>()

        // set home position
        missionRawItems.add(createMissionItemRaw(0, 0, 16, 1, 1,
            0f, 0f, 0f, 0f,
            0f, 0f,0f, 0))

        // set take off
        missionRawItems.add(createMissionItemRaw(1, 0, 22, 0, 1,
            0f, 0f, 0f, 0f,
            0f, 0f, 100.0f, 0))

        missionRawItems.add(createMissionItemRaw(2, 3, 16, 0, 1,
            0f, 0f, 0f, 0f,
            34.1294439f, -117.9667605f,100.0f, 0))

        missionRawItems.add(createMissionItemRaw(3, 3, 16, 0, 1,
            0f, 0f, 0f, 0f,
            34.1292826f, -117.9667514f,100.0f, 0))

        // Return to launch
        missionRawItems.add(createMissionItemRaw(4, 2, 20, 0,1,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0))
        return missionRawItems
    }

    private fun createMissionItemRaw(seq: Int, frame: Int, command: Int, current: Int, autocontinue: Int,
                                     param1: Float, param2: Float, param3: Float, param4: Float,
                                     x: Float, y: Float, z: Float, missionType: Int): MissionRaw.MissionItem {

        val missionItem = MissionRaw.MissionItem(seq, frame, command, current, autocontinue,
            param1, param2, param3, param4,
            (x * 1e7).roundToInt(), (y * 1e7).roundToInt(), z, missionType)

        return missionItem
    }}
