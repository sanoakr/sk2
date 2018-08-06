package jp.ac.ryukoku.st.sk2

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import me.mattak.moment.Moment
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap
import org.jetbrains.anko.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class ScanService: Service(), BootstrapNotifier, AnkoLogger {

    private lateinit var bgPowerSaver: BackgroundPowerSaver
    private var btManager: BeaconManager? = null
    companion object {
        val UNQID = "sk2"
        val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        //val ALTBEACON_FORMAT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        //val EDDYSTONE_FORMAT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
        //val UUID = "6F42B781-D072-4E20-A07A-53F6A1C7CB59"
    }
    ////////////////////////////////////////
    private val binder: IBinder = ScanBinder()
    inner class ScanBinder: Binder() {
        internal val inService: ScanService
            get() = this@ScanService
    }
    ////////////////////////////////////////
    //val sk2: Sk2Globals? = this.application as? Sk2Globals
    private val handler = Handler()
    private val timer = Runnable { interval() }
    var period: Long = 10*60*1000 // temporaly initialized to 10min
    private fun interval() {
        sendInfo('A')
        handler.postDelayed(timer, period)
    }
    ////////////////////////////////////////
    fun startInterval(sec: Long) {
        period = sec*1000
        info(period)
        handler.postDelayed(timer, period)
    }
    fun stopInterval() { handler.removeCallbacks(timer) }

    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        bgPowerSaver = BackgroundPowerSaver(this)

        val sk2 = this.application as Sk2Globals
        val interval = sk2.beaconIntervalSec * 1000 // msec
        try {
            btManager = BeaconManager.getInstanceForApplication(this)
            btManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
            btManager?.foregroundBetweenScanPeriod = interval
            btManager?.backgroundBetweenScanPeriod = interval
            //val identifier = Identifier.parse(UUID)
            val region = Region(UNQID, null, null, null)
            RegionBootstrap(this, region)

            btManager?.addRangeNotifier { beacons, _ ->
                for (b in sortedBeaconString(beacons)) {
                    info("UUID:${b.id1}, major:${b.id2}, minor:${b.id3}, " +
                            "Distance:${b.distance} meters, RSSI:${b.rssi}, TxPower:${b.txPower}")
                }
            }
        } catch (e: RemoteException) { e.printStackTrace() }
        info("on Create")
    }
    //////////////////////////////////////// ////////////////////////////////////////
    override fun didEnterRegion(region: Region) {
        toast("ビーコン領域に入りました")
        //sendMessage("ENTER iBeacon Region")
        //startActivity<MainActivity>()
        try {
            btManager?.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    ////////////////////////////////////////
    override fun didExitRegion(region: Region) {
        toast("ビーコン領域から出ました")
        try {
            btManager?.stopRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    ////////////////////////////////////////
    override fun didDetermineStateForRegion(i: Int, region: Region) {
        info("Determine State: ${Integer.toString(i)}")
    }
    ////////////////////////////////////////
    fun sortedBeaconString(beacons: Collection<Beacon>): Collection<Beacon> {
        if ( beacons.isNotEmpty() ) {
            val sorted = beacons.sortedWith(compareBy(Beacon::getDistance))
            return sorted
        }
        return beacons
    }
    ////////////////////////////////////////
    fun sendInfo(marker: Char): String {
        val sk2 = this.application as Sk2Globals
        val user = sk2.userMap.getOrDefault("uid", "")

        //val info = sortedWifiString(scanWifi(), ",")

        val moment = Moment()
        val m = moment.format("yyy-MM-dd HH:mm:ss")
        //val message = "$user,$marker,$m,$info"

        var toastMsg: String
        /*
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
        }*/
        return "abc"
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
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("on StartCommand")
        return super.onStartCommand(intent, flags, startId)
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        info("on Destroy")
        handler.removeCallbacks(timer)
        stopSelf()
    }
}
