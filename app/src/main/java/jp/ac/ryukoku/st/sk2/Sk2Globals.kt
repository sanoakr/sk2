package jp.ac.ryukoku.st.sk2

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.ServiceConnection
import com.google.gson.Gson
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.stopService

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
    ///////////////////////////////////////
    val _autoitv: Int = 10*60            // sec
    val fgBeaconIntervalSec: Long = 2    // sec
    val bgBeaconIntervalSec: Long = 10   // sec
    ///////////////////////////////////////
    //var androidId = ""
    val prefName = "st.ryukoku.sk2"
    var userMap: MutableMap<String, Any> = mutableMapOf()
    var prefMap: MutableMap<String, Any> = mutableMapOf()
    ////////////////////////////////////////
    //var latest: MutableMap<String, String> = mutableMapOf()
    var localQueue = Queue<MutableMap<String, String>>(mutableListOf(), 100)
    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        restoreUserData()
        restorePrefData()
    }
    ////////////////////////////////////////
    fun saveUserData() {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val e = pref.edit()
        e.putString("uid", userMap["uid"] as String)
        e.putString("key", userMap["key"] as String)
        e.putString("gcos", userMap["gcos"] as String)
        e.putString("name", userMap["name"] as String)
        e.putLong("time", userMap["time"] as Long)
        e.apply()
    }
    ////////////////////////////////////////
    fun savePrefData() {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val e = pref.edit()
        e.putBoolean("auto", prefMap["auto"] as Boolean)
        e.putInt("autoitv", prefMap["autoitv"] as Int)
        e.putBoolean("debug", prefMap["debug"] as Boolean)
        val gson = Gson()
        val jsonQueueString = gson.toJson(localQueue)
        e.putString("queue", jsonQueueString as String)
        e.apply()
    }
    ////////////////////////////////////////
    fun restoreUserData() {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        userMap["uid"] = pref.getString("uid", "")
        userMap["key"] = pref.getString("key", "")
        userMap["gcos"] = pref.getString("gcos", "")
        userMap["name"] = pref.getString("name", "")
        userMap["time"] = pref.getLong("time", 0L)
    }
    ////////////////////////////////////////
    fun restorePrefData() {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefMap["auto"] = pref.getBoolean("auto", false)
        prefMap["autoitv"] = pref.getInt("autoitv", 0)
        prefMap["debug"] = pref.getBoolean("debug", false)
        val gson = Gson()
        val jsonQueueString = pref.getString("queue", gson.toJson(localQueue))
        localQueue = gson.fromJson(jsonQueueString, localQueue::class.java)
    }
    ////////////////////////////////////////
    fun logout(connection: ServiceConnection?) {
        userMap["uid"] = ""; userMap["key"] = "";
        userMap["gcos"] = ""; userMap["name"] = "";
        userMap["time"] = 0L
        saveUserData()

        stopService<ScanService>()
        if (connection != null) {
            unbindService(connection)
            stopService<ScanService>()
            //BluetoothAdapter.getDefaultAdapter().disable()
        }
        startActivity(intentFor<LoginActivity>().clearTop())
    }
}
