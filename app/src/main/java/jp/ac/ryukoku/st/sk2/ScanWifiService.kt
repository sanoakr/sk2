package jp.ac.ryukoku.st.sk2

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import me.mattak.moment.Moment
import org.jetbrains.anko.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class ScanWifiService : Service(), AnkoLogger {
    ////////////////////////////////////////
    private val binder: IBinder = ScanWifiBinder()
    inner class ScanWifiBinder: Binder() {
        internal val inService: ScanWifiService
            get() = this@ScanWifiService
    }
    ////////////////////////////////////////
    private val handler = Handler()
    private val timer = Runnable { interval() }
    var period: Long = 10*60*1000
    private fun interval() {
        sendApInfo('A')
        handler.postDelayed(timer, period)
    }
    fun startInterval(sec: Long) { period = sec*1000; info(period); handler.postDelayed(timer, period) }
    fun stopInterval() { handler.removeCallbacks(timer) }
    ////////////////////////////////////////
    fun sendApInfo(marker: Char): String {
        val sk2 = this.application as Sk2Globals
        val user = sk2.userMap.getOrDefault("uid", "")
        val info = sortedWifiString(scanWifi(), ",")
        val moment = Moment()
        val m = moment.format("yyy-MM-dd HH:mm:ss")
        val message = "$user,$marker,$m,$info"

        var toastMsg: String
        doAsync {
            try {
                val sslSocketFactory = SSLSocketFactory.getDefault()
                val sslsocket = sslSocketFactory.createSocket()
                sslsocket.connect(InetSocketAddress(sk2.serverHost, sk2.serverPort), sk2.timeOut)

                val input = sslsocket.inputStream
                val output = sslsocket.outputStream
                val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
                val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

                // Send message
                bufWriter.write(message)
                bufWriter.flush()
                // Receive message
                val recMessage = bufReader.use(BufferedReader::readText)
                if (recMessage == sk2.recFail) {
                    toastMsg = "データが記録できません"
                } else {
                    toastMsg = "データを記録しました"
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                toastMsg = "サーバに接続できません"
            }
            uiThread { toast(toastMsg) }
        }
        return message
    }
    ////////////////////////////////////////
    //fun getApInfo(): String {
    //    return sortedWifiString(scanWifi(), "\n")
    //}
    ////////////////////////////////////////
    fun sortedWifiString(results: List<ScanResult>?, delim: String): String {
        var wifiString = ""
        if ( results != null && results.isNotEmpty() ) {
            val sortedWifi = results.sortedWith(compareBy(ScanResult::level)).reversed()
            sortedWifi.forEach { ap ->
                wifiString += "${ap.SSID},${ap.BSSID},${ap.level}${delim}"
            }
        }
        return wifiString
    }
    ////////////////////////////////////////
    private val wifiManager: WifiManager
        get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    ////////////////////////////////////////
    fun scanWifi(): List<ScanResult>? {
        if ( wifiManager.startScan() ) {
            val result = wifiManager.scanResults
            return result
        }
        return null
    }
    ////////////////////////////////////////
    override fun onBind(p0: Intent?): IBinder {
        info("on Bind")
        return binder
    }
    ////////////////////////////////////////
    override fun onUnbind(intent: Intent?): Boolean {
        info("on Unbind")
        return super.onUnbind(intent)
    }
    ////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        info("on Create")
    }
    ////////////////////////////////////////
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("on StartCommand")
        return super.onStartCommand(intent, flags, startId)
        //return START_STICKY
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        info("on Destroy")
        handler.removeCallbacks(timer)
        stopSelf()
    }
}
