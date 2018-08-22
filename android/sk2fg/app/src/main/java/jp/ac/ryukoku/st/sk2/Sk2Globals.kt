package jp.ac.ryukoku.st.sk2

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.google.gson.Gson
import me.mattak.moment.Moment
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.stopService
import org.json.JSONStringer
import java.lang.Math.pow
import java.util.*

////////////////////////////////////////////////////////////////////////////////
class Sk2Globals: Application() {
    ////////////////////////////////////////
    val app_name = "sk2"
    val app_title = "龍大理工学部出欠システム"
    val serverHost = "sk2.st.ryukoku.ac.jp"
    val serverPort = 4440
    val serverInfoPort = 4441
    val timeOut = 5000                    // 5sec
    ////////////////////////////////////////
    val authWord = "AUTH"
    val authFail = "authfail"
    val recFail = "fail"
    val testuser = "testuser"  // startwith
    ///////////////////////////////////////
    //var androidId = ""
    val prefName = "st.ryukoku.sk2"
    lateinit var pref: SharedPreferences
    ////////////////////////////////////////
    var localQueue = Queue<AttendData>(mutableListOf(), 100)
    var lastScan = ScanArray()
    ////////////////////////////////////////
    val SCAN_RUNNING = "scan_running"
    val VALID_IBEACON_UUID: List<String> = listOf(
            "ebf59ccc-21f2-4558-9488-00f2b388e5e6"//, // ru-wifi
            //"00000000-87b3-1001-b000-001c4d975326"  // sekimoto's
    )
    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        restoreQueue()
        //restoreLastScan()
    }
    ////////////////////////////////////////
    fun saveQueue(queue: Queue<AttendData> = Queue(mutableListOf())) {
        val gson = Gson()
        val jsonString = gson.toJson(queue)
        pref.edit()
                .putString("queue", jsonString as String)
                .apply()
    }
    ////////////////////////////////////////
    fun restoreQueue() {
        val gson = Gson()
        val jsonString = pref.getString("queue", gson.toJson(Queue<AttendData>(mutableListOf())))
        localQueue = gson.fromJson(jsonString, Queue<AttendData>(mutableListOf())::class.java)
    }
    ////////////////////////////////////////
    fun saveLastScan(scanarray: ScanArray = ScanArray()) {
        val gson = Gson()
        val jsonString = gson.toJson(scanarray)
        pref.edit()
                .putString("lastscan", jsonString as String)
                .apply()
    }
    ////////////////////////////////////////
    fun getScanRunning(): Boolean {
        return pref.getBoolean(SCAN_RUNNING, false)
    }
    ////////////////////////////////////////
    fun setScanRunning(running: Boolean){
        pref.edit()
                .putBoolean(SCAN_RUNNING, running)
                .apply()
    }
    ////////////////////////////////////////
    fun logout() {
        pref.edit()
                .putString("uid", "")
                .putString("key", "")
                .putString("name", "")
                .putLong("time", 0L)
                .putBoolean("debug", false)
                .putBoolean("auto", false)
                .apply()

        saveQueue() // clear Local Queue
        saveLastScan() // clear lastscan


        //stopService<ScanService>()
        /*
        if (connection != null) {
            unbindService(connection)
            stopService<ScanService>()
            //BluetoothAdapter.getDefaultAdapter().disable()
        }
        */
        startActivity(intentFor<LoginActivity>().clearTop())
    }
}



