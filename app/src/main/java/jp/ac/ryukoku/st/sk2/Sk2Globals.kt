package jp.ac.ryukoku.st.sk2

import android.app.Application
import android.content.Context
import android.content.ServiceConnection
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.stopService

////////////////////////////////////////////////////////////////////////////////
class Sk2Globals: Application() {
    ////////////////////////////////////////
    val serverHost = "sk2.st.ryukoku.ac.jp"
    val serverPort = 4440
    val serverInfoPort = 4441
    val timeOut = 5000                    // 5sec
    ////////////////////////////////////////
    val authWord = "AUTH"
    val authFail = "authfail"
    val recFail = "fail"
    ////////////////////////////////////////
    //val ryuid = "ryu-wireless"
    ////////////////////////////////////////
    val _autoitv: Int = 10*60             // 10min
    val beaconIntervalSec: Long = 10      // 10sec
    //val switchApInterval: Long = 60       // 60sec
    ////////////////////////////////////////
    //var androidId = ""
    val prefName = "st.ryukoku.sk2"
    var userMap: MutableMap<String, Any> = mutableMapOf()
    var prefMap: MutableMap<String, Any> = mutableMapOf()

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
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
        e.putBoolean("beacon", prefMap["beacon"] as Boolean)
        e.putBoolean("auto", prefMap["auto"] as Boolean)
        e.putInt("autoitv", prefMap["autoitv"] as Int)
        e.putBoolean("swtap", prefMap["swtap"] as Boolean)
        e.putBoolean("debug", prefMap["debug"] as Boolean)
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
        prefMap["beacon"] = pref.getBoolean("beacon", false)
        prefMap["auto"] = pref.getBoolean("auto", false)
        prefMap["autoitv"] = pref.getInt("autoitv", 0)
        prefMap["swtap"] = pref.getBoolean("swtap", false)
        prefMap["debug"] = pref.getBoolean("debug", false)
    }
    ////////////////////////////////////////
    fun logout(wifiConnection: ServiceConnection?) {
        userMap["uid"] = ""; userMap["key"] = "";
        userMap["gcos"] = ""; userMap["name"] = "";
        userMap["time"] = 0L
        saveUserData()

        stopService<ScanBeaconService>()
        if (wifiConnection != null) {
            unbindService(wifiConnection)
            stopService<ScanWifiService>()
        }
        startActivity(intentFor<LoginActivity>().clearTop())
    }
}

