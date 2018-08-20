
package jp.ac.ryukoku.st.sk2

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.MainActivity.Companion.ATTENDANCE_TIME_DIFFERENCE_MILLISEC
import me.mattak.moment.Moment
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

//@Suppress("DEPRECATION")
class ScanService : Service(), AnkoLogger /*, BootstrapNotifier*/ {
    companion object {
        private const val PACKAGE_NAME = "jp.ac.ryukoku.st.sk2"
        private val TAG = ScanService::class.java.simpleName

        const val NOTIFICATION_ID = 12345678
        const val CHANNEL_ID = "channel_0123" //The name of the channel for notifications.

        internal const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        internal const val EXTRA_BLESCAN = "$PACKAGE_NAME.scan"
        const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

        private const val SCAN_INTERVAL_IN_MILLISECONDS: Long = 1000 // 1 sec.
        const val AUTO_SENDING_INTERVAL_IN_MILLISECONDS: Long = 5*60*1000 // 5 min.
        const val AUTO_SENDING_INTERVAL_IN_MILLISECONDS_DEBUG: Long = 5*1000 // 5 sec.

        //private const val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        //val ALTBEACON_FORMAT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        //val EDDYSTONE_FORMAT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
        //val UUID = "6F42B781-D072-4E20-A07A-53F6A1C7CB59"
    }
    private var mChangingConfiguration = false
    private var mNotificationManager: NotificationManager? = null

    private var mServiceHandler: Handler? = null
    private val mBinder: IBinder = LocalBinder()
    private val mScan = false

    private val notification: Notification
        get() {
            val intent = Intent(this, ScanService::class.java)
            val text = sk2.lastScan.datetime.toString() // get datetime string of lastscan

            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
            val servicePendingIntent = PendingIntent.getService(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val activityPendingIntent = PendingIntent.getActivity(this, 0,
                    Intent(this, MainActivity::class.java), 0)

            //val builder = NotificationCompat.Builder(this)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                            activityPendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                            servicePendingIntent)
                    .setContentText(text)
                    //.setContentTitle(Utils.getLocationTitle(this))
                    .setContentTitle("Sk2サーバへの送信")
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //    builder.setChannelId(CHANNEL_ID)
            //}
            return builder.build()
        }

    // my SharedPreferences
    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    /********** BLE **********/
    private lateinit var mScanner: BluetoothLeScannerCompat
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilters: ArrayList<ScanFilter>

    ////////////////////////////////////////
    private var auto = false
    private val intervalHandler = Handler()
    private val timer = Runnable { interval() }
    private var period: Long = AUTO_SENDING_INTERVAL_IN_MILLISECONDS
    private fun interval() {
        sendInfoToServer('A')
        intervalHandler.postDelayed(timer, period)
    }
    ////////////////////////////////////////
    fun startInterval(debug: Boolean) {
        if (auto) { stopInterval(); auto = false }

        period = if (debug) AUTO_SENDING_INTERVAL_IN_MILLISECONDS_DEBUG
        else
            AUTO_SENDING_INTERVAL_IN_MILLISECONDS

        info("start auto interval in $period msec")
        intervalHandler.postDelayed(timer, period)

        auto = true
        pref.edit()
                .putBoolean("auto", true)
                .apply()
    }
    fun stopInterval() {
        info("stop auto interval")
        intervalHandler.removeCallbacks(timer)

        auto = false
        pref.edit()
                .putBoolean("auto", false)
                .apply()
    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
    override fun onCreate() {
        sk2 = this.application as Sk2Globals
        pref = sk2.pref

        /********** BLE **********/
        mScanner = BluetoothLeScannerCompat.getScanner()
        scanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(SCAN_INTERVAL_IN_MILLISECONDS)
                .setUseHardwareBatchingIfSupported(false).build()
        scanFilters = ArrayList() // ArrayList<ScanFilter>()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
                mChannel.apply {
                    description = "notification Description"
                }
                // Set the Notification Channel for the Notification Manager.
                mNotificationManager!!.createNotificationChannel(mChannel)
            }
        }

        // start auto interval
        if (pref.getBoolean("auto", false)) startInterval(pref.getBoolean("debug", false))
    }
    /********** BLE **********/
    ////////////////////////////////////////
    private val mScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onReceivedBleScan(listOf(result))
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            onReceivedBleScan(results)
        }
        override fun onScanFailed(errorCode: Int) {
            warn("ScanFailed: $errorCode")/* should never be called */ }
    }
    ////////////////////////////////////////
    private fun onReceivedBleScan(results: List<ScanResult>) {
        val scanArray = ScanArray()

        scanArray.datetime = Moment()
        results.forEach { r ->
            val bytes = r.scanRecord?.bytes
            val rssi = r.rssi
            val structures = ADPayloadParser.getInstance().parse(bytes)
            structures.forEach { s ->
                if (s is IBeacon) {
                    scanArray.add(Pair(s, rssi))

                    val dist = getBleDistance(s.power, rssi)
                    info("${s.uuid}, ${s.major}, ${s.minor}, ${dist}")
                }
            }
            if (scanArray.isNotEmpty()) {
                sk2.lastScan = scanArray
                //sendScanBroadcast(iBeaconRssiPairs)
            }
        }
        /*
        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager!!.notify(NOTIFICATION_ID, notification)
        }
        */
    }
    ////////////////////////////////////////
    private fun sendBroadcast(message: String) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_BLESCAN, message)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
    ////////////////////////////////////////
    fun sendInfoToServer(type: Char): String? { // not Boolean, couldn't return from the async block
        info("Sending attendance info to Server with $type")
        val lastArray = sk2.lastScan
        val curDatetime = Moment()

        var message: String? = null
        // Last scan recording time difference within 60 secs
        if (curDatetime.compareTo(lastArray.datetime) < ATTENDANCE_TIME_DIFFERENCE_MILLISEC
                && lastArray.count() > 0) {

            val dtString = lastArray.datetime.format("yyy-MM-dd HH:mm:ss")
            val uid = pref.getString("uid", "")
            val key = pref.getString("key", "")
            val message = "$uid,$key,$type,$dtString" +
                    lastArray.getBeaconsText(false, false, false)
            info("SEND to sk2; $message")

            //push the last scan record with current daytime and attendance type into the localQueue
            sk2.localQueue.push(AttendData(curDatetime, type, lastArray))
            sendBroadcast("出席")

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
                        warn("sendInfoToServer; Couldn't write a record on sk2 server")
                    } else {
                        info("sendInfoToServer; Success recording on sk2 server")
                    }
                } catch (e: Exception) {
                    //e.printStackTrace()
                    warn("sendInfoToServer; Couldn't connect to sk2 server")
                }
            }
            // Update notification content if running as a foreground service.
            if (serviceIsRunningInForeground(this)) {
                mNotificationManager!!.notify(NOTIFICATION_ID, notification)
            }
        } else {
            sendBroadcast("Beacon が見つかりません")
        }
        return message
    }
    ////////////////////////////////////////
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        info("Service started")
        val startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false)

        if (startedFromNotification) {
            info("Start from notification")
            removeLocationUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY
    }
    ////////////////////////////////////////
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }
    ////////////////////////////////////////
    override fun onBind(intent: Intent): IBinder? {
        info("in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }
    ////////////////////////////////////////
    override fun onRebind(intent: Intent) {
        info("in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }
    ////////////////////////////////////////
    override fun onUnbind(intent: Intent): Boolean {
        info("Last client unbound from service")

        if (sk2.getScanRunning()) {
            info("Starting foreground service")

            val fgIntent = Intent(this, ScanService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(fgIntent)
            } else {
                this.startService(fgIntent)
            }
            startForeground(NOTIFICATION_ID, notification)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        removeLocationUpdates()
        stopInterval()
        mServiceHandler!!.removeCallbacksAndMessages(null)
    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
    fun requestLocationUpdates() {
        info("Requesting BLE scan updates")
        if (checkBt() && !sk2.getScanRunning()) {
            mScanner.startScan(scanFilters, scanSettings, mScanCallback) // start Ble Scanner
            sk2.setScanRunning(true)

            startService(Intent(applicationContext, ScanService::class.java))

            info("BLE Scanner is started")
        } else {
            info("BLE Scanner can not be started; BLE not found")
        }
    }
    ////////////////////////////////////////
    fun removeLocationUpdates() {
        info("Removing BLE scan updates")

        mScanner.stopScan(mScanCallback)   // stop Ble Scanner
        sk2.setScanRunning(false)
        stopSelf()
    }
    ////////////////////////////////////////
    inner class LocalBinder : Binder() {
        internal val service: ScanService
            get() = this@ScanService
    }
    ////////////////////////////////////////
    fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
                Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }
    ////////////////////////////////////////
    private fun hasBLE(): Boolean {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    ////////////////////////////////////////
    fun checkBt(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null || !hasBLE()) {
            toast("このデバイスのBLEアダプタが見つかりません")
            return false
        } else if (!btAdapter.isEnabled) {
            toast("Bluetoothをオンにしてください")
            return false
        }
        return true
    }
}
