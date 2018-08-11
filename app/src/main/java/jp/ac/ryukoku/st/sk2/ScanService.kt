package jp.ac.ryukoku.st.sk2

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
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
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context
import android.content.ContextWrapper
import android.support.v4.app.NotificationCompat


////////////////////////////////////////////////////////////////////////////////
class ScanService: IntentService("ScanService"), BootstrapNotifier, AnkoLogger {

    private lateinit var bgPowerSaver: BackgroundPowerSaver
    private var btManager: BeaconManager? = null

    private var region = Region("",null,null,null)
    //private var latest: MutableMap<String, String> = mutableMapOf()
    companion object {
        val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        //val ALTBEACON_FORMAT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        //val EDDYSTONE_FORMAT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
        //val UUID = "6F42B781-D072-4E20-A07A-53F6A1C7CB59"
    }
    /*
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
    */
    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        /*
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter.isEnabled) {
            btAdapter.disable()
            while (!btAdapter.isEnabled) {
                btAdapter.enable()
                Thread.sleep(100)
            }
        }
        */
        bgPowerSaver = BackgroundPowerSaver(this)

        //val sk2 = this.application as Sk2Globals
        val fgInterval: Long = 2 // sec
        val bgInterval: Long = 2 // sec
        val UNQID = "sk2-" + System.currentTimeMillis().toString() // for reconnecttion to BleAdapter
        try {
            btManager = BeaconManager.getInstanceForApplication(this)
            btManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
            btManager?.foregroundBetweenScanPeriod = fgInterval
            btManager?.backgroundBetweenScanPeriod = bgInterval
            //val identifier = Identifier.parse(UUID)
            region = Region(UNQID, null, null, null)
            RegionBootstrap(this, region)
            info("Region:${UNQID}")

            btManager?.addRangeNotifier { beacons, _ ->
                //latest.clear()
                val moment = Moment()
                val m: String = moment.format("yyy-MM-dd HH:mm:ss")
                //val wday: String = moment.weekdayName

                var info = "${m},"
                for ((ix, b) in sortedBeaconString(beacons).withIndex()) {
                    info += "${b.id2},${b.id3},${b.distance},"
                    //info += "${b.id1},${b.id2},${b.id3},${b.distance},"
                }
                sendMessage(info)
            }
        } catch (e: RemoteException) { e.printStackTrace() }
    }
    ////////////////////////////////////////
    private fun sendMessage(msg: String) {
        val broadcast = Intent()
        broadcast.putExtra("scaninfo", msg)
        broadcast.action = "BEACON"
        baseContext.sendBroadcast(broadcast)
    }
    ////////////////////////////////////////////////////////////////////////////////
    override fun didEnterRegion(region: Region) {
        //doAsync{uiThread{toast("ビーコン領域に入りました")}}

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
        //doAsync{uiThread{toast("ビーコン領域から出ました")}}
        try {
            btManager?.stopRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    ////////////////////////////////////////
    override fun didDetermineStateForRegion(i: Int, region: Region) {
        //info("Determine State: ${Integer.toString(i)}")
    }
    ////////////////////////////////////////
    fun sortedBeaconString(beacons: Collection<Beacon>): Collection<Beacon> {
        if ( beacons.isNotEmpty() ) {
            val sorted = beacons.sortedWith(compareBy(Beacon::getDistance))
            return sorted
        }
        return beacons
    }
    /*
    ////////////////////////////////////////
    fun sendInfo(marker: Char): String {
        val sk2 = this.application as Sk2Globals
        var info = ""
        var toastMsg = "データが取得できません"

        //info("${latest["major0"]}")
        if (! latest["major0"].isNullOrBlank()) {
            sk2.localQueue.push(latest)
            info = "${latest["user"]},${marker},${latest["datetime"]}," +
                    //"${latest["UUID0"] ?: ""}," +
                    "${latest["major0"] ?: ""},${latest["minor0"] ?: ""},${latest["dist0"] ?: ""}," +
                    //"${latest["UUID1"] ?: ""},
                    "${latest["major1"] ?: ""},${latest["minor1"] ?: ""},${latest["dist1"] ?: ""}," +
                    //"${latest["UUID2"] ?: ""},
                    "${latest["major2"] ?: ""},${latest["minor2"] ?: ""},${latest["dist2"] ?: ""}"

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
                    bufWriter.write(info)
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
        }
        return info
    }
    */
    ////////////////////////////////////////
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    startForeground(1, notification)
        //}
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onHandleIntent(intent: Intent) {
        //info("on HandleIntent")
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        info("on Destroy")
        stopSelf()
    }
}
