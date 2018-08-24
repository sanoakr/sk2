package jp.ac.ryukoku.st.sk2

import android.app.Activity.DEFAULT_KEYS_SEARCH_GLOBAL
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ATTENDANCE_TIME_DIFFERENCE_SEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.AUTO_SENDING_INTERVAL_IN_MILLISECONDS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.AUTO_SENDING_INTERVAL_IN_MILLISECONDS_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_NO_BEACON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_NO_VALITTIME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.CHANNEL_ID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_STARTED_FROM_NOTIFICATION
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_ACTION_LAUNCE_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_CHANNEL_NAME_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_ID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_TITLE_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PERMISSION_SENDING_TIME_FROM
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PERMISSION_SENDING_TIME_TO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_INTERVAL_IN_MILLISECONDS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_HOSTNAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_PORT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_REPLY_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_TIMEOUT_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.VALID_IBEACON_UUID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.WAKEUP_INTERVAL_IN_MILLISEC
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

////////////////////////////////////////////////////////////////////////////////
/*** BLEスキャン・出席記録送信用サービスクラス ***/
class ScanService : Service() /*, BootstrapNotifier*/ {
    companion object {
        val TAG = this::class.java.simpleName
    }
    //private var mChangingConfiguration = false
    /*** Notification Manager ***/
    private var mNotificationManager: NotificationManager? = null

    /*** ScanService service Handler & Binder ***/
    private var mServiceHandler: Handler? = null
    private val mBinder: IBinder = LocalBinder()

    /*** Notification ***/
    private val notification: Notification
        get() {
            // Notification intent to ScanService
            val intent = Intent(this, ScanService::class.java)
            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
            /*
            // ScanService への Pending intent: BLE スキャンを止める
            val servicePendingIntent = PendingIntent.getService(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            */
            // MainActivity への Pending intent: アプリを起こす
            val activityPendingIntent = PendingIntent.getActivity(this, 0,
                    Intent(this, MainActivity::class.java), 0)

            // 最終スキャンの取得日時: notification は出席データ送信でトリガされる
            val text = sk2.lastScan.datetime.toString()

            //val builder = NotificationCompat.Builder(this)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .addAction(R.drawable.ic_launch, NOTIFICATION_ACTION_LAUNCE_TEXT, activityPendingIntent)
                    //.addAction(R.drawable.ic_cancel, NOTIFICATION_STOP_SCAN_TEXT, servicePendingIntent)
                    .setContentText(text)
                    .setContentTitle(NOTIFICATION_TITLE_TEXT)
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
            return builder.build()
        }

    /*** my SharedPreferences ***/
    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    /*** BLE スキャナ ***/
    private lateinit var mScanner: BluetoothLeScannerCompat
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilters: ArrayList<ScanFilter>

    /*** BLE スキャナの再起動用インターバル Handler & Timer ***/
    private val btWakeupHandler = Handler()
    private val wakeupTimer = Runnable { wakeup() }
    private fun wakeup() {
        restartScanner()
        btWakeupHandler.postDelayed(wakeupTimer, WAKEUP_INTERVAL_IN_MILLISEC)
    }
    /*** 自動データ送信用インターバル Handler & Timer ***/
    private val intervalHandler = Handler()
    private val timer = Runnable { interval() }
    private var period: Long = AUTO_SENDING_INTERVAL_IN_MILLISECONDS
    private fun interval() {
        sendInfoToServer('A')
        intervalHandler.postDelayed(timer, period)
    }
    /*** 自動データ送信を開始 ***/
    fun startInterval(debug: Boolean) {
        stopInterval() // すでに起動しているものを停止

        period = if (debug) // デバック用のインターバルは短い
            AUTO_SENDING_INTERVAL_IN_MILLISECONDS_DEBUG
        else
            AUTO_SENDING_INTERVAL_IN_MILLISECONDS

        // Handler を投げる
        intervalHandler.postDelayed(timer, period)
        Log.d(TAG,"start auto interval in $period msec")

        pref.edit() // SharedPreferences にフラグを書き込む
                .putBoolean(PREF_AUTO, true)
                .apply()
    }
    /*** 自動データ送信を停止 ***/
    fun stopInterval() {
        Log.d(TAG,"stop auto interval")
        intervalHandler.removeCallbacks(timer)

        pref.edit() // SharedPreferences にフラグを書き込む
                .putBoolean("auto", false)
                .apply()
    }
    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        sk2 = this.application as Sk2Globals
        pref = sk2.pref

        // BLE スキャナ
        mScanner = BluetoothLeScannerCompat.getScanner()
        scanSettings = ScanSettings.Builder()
                .setLegacy(false)
                //.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(10)
                .setCallbackType(DEFAULT_KEYS_SEARCH_GLOBAL)
                //.setReportDelay(SCAN_INTERVAL_IN_MILLISECONDS)
                .setUseHardwareBatchingIfSupported(false)
                .build()

        val builder = ScanFilter.Builder()
        // Apple Manufacture ID 0x004c
        builder.setManufacturerData(0x004c, byteArrayOf())
        scanFilters = arrayListOf( builder.build() )
                // フィルターが空だとバックグラウンドで止まる?
        //scanFilters = ArrayList() // ArrayList<ScanFilter>() // フィルタは空 == 全て受け取る

        // Handler スレッドを開始
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        // Service Handler
        mServiceHandler = Handler(handlerThread.looper)

        // Notification Manager を取得
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Create the channel for the notification
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val mChannel = NotificationChannel(CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME_TEXT,
                        NotificationManager.IMPORTANCE_LOW)
                mChannel.apply {
                    description = NOTIFICATION_TITLE_TEXT
                }
                // Set the Notification Channel for the Notification Manager.
                mNotificationManager!!.createNotificationChannel(mChannel)
            }
        }

        // Always restart BLE scanner
        restartScanner()

        // Start auto interval
        if (pref.getBoolean(PREF_AUTO, false))
            startInterval(pref.getBoolean(PREF_DEBUG, false))

        // Start BLE Scanner forced Wakeup interval
        btWakeupHandler.postDelayed(wakeupTimer, WAKEUP_INTERVAL_IN_MILLISEC)
    }
    ////////////////////////////////////////
    /*** BLE スキャナのコールバック ***/
    private val mScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onReceivedBleScan(listOf(result))
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            onReceivedBleScan(results)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "ScanFailed: $errorCode")/* should never be called */ }
    }
    ////////////////////////////////////////
    /*** BLE スキャン受診時のコールバック ***/
    private fun onReceivedBleScan(results: List<ScanResult>) {
        val scanArray = ScanArray()

        // スキャン日時
        scanArray.datetime = Moment()
        // ビーコン情報をパースして ScanArray() に保存
        results.forEach { r ->
            val bytes = r.scanRecord?.bytes
            val rssi = r.rssi
            val structures = ADPayloadParser.getInstance().parse(bytes)
            structures.forEach { s ->
                if (s is IBeacon && (s.uuid.toString() in VALID_IBEACON_UUID)) {
                    scanArray.add(Pair(s, rssi))

                    val dist = getBleDistance(s.power, rssi)
                    Log.d(TAG,"${s.uuid}, ${s.major}, ${s.minor}, ${dist}")
                }
            }
            // 空でなければ最新スキャンに保存して、MainActivity にBroadcastで送る
            if (scanArray.isNotEmpty()) {
                sk2.lastScan = scanArray
                sendBroadcastLastScan(scanArray.getBeaconsText(signal = true, ios = true))
            }
        }
    }
    ////////////////////////////////////////
    /*** ブロードキャストでスキャン結果を送信: to MainActivity.MyReceiver.BroadcastReceiver() ***/
    private fun sendBroadcastLastScan(lastscan: String) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_BLESCAN, lastscan)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
    ////////////////////////////////////////
    /*** ブロードキャストメッセージを送信: to MainActivity.MyReceiver.BroadcastReceiver() ***/
    private fun sendBroadcastMessage(message: String) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_TOAST, message)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
    ////////////////////////////////////////
    /*** 出席データをサーバに送信 ***/
    fun sendInfoToServer(type: Char) { // not Boolean, couldn't return from in any async blocks
        val lastArray = sk2.lastScan  // 最新のスキャンデータ
        val curDatetime = Moment()               // 現在時刻

        Log.d(TAG, "Try to send an attendance info to the Server with type $type")

        /*** 送信可能時刻をチェック ***/
        // debug モードでなければ、’..TIME_FROM'時から’..TIME_TO'時の間でないなら終了
        if ( (! pref.getBoolean("debug", false))
                && (curDatetime.hour < PERMISSION_SENDING_TIME_FROM
                        || curDatetime.hour > PERMISSION_SENDING_TIME_TO)) {

            // 失敗のブロードキャストメッセージを送信して終了
            sendBroadcastMessage(BROADCAST_ATTEND_NO_VALITTIME)
            Log.d(TAG,"sendInfoToServer; Overtime use")
            return
        }

        // 送信データ用文字列
        var message: String

        /*** 最新スキャンが DIFFERENCE_SEC 以内 && スキャンされたビーコンがあれば送信する ***/
        if (differenceSec(curDatetime, lastArray.datetime) < ATTENDANCE_TIME_DIFFERENCE_SEC
                && lastArray.count() > 0) {

            // 日時、ユーザ、認証キー
            val dtString = lastArray.datetime.format("yyy-MM-dd HH:mm:ss")
            val uid = pref.getString("uid", "")
            val key = pref.getString("key", "")

            // 送信データは「ラベルなし」で生成
            message = "$uid,$key,$type,$dtString" +
                    lastArray.getBeaconsText(false, false, false)

            Log.d(TAG,"SEND to sk2; $message")

            // ローカル記録キューに追加（送信の可否に依存しない）
            sk2.localQueue.push(AttendData(curDatetime, type, lastArray))

            // ブロードキャストメッセージを送信
            sendBroadcastMessage(BROADCAST_ATTEND_SEND)

            /*** サーバ接続 ***/
            doAsync {
                try {
                    // SSL Socket
                    val sslSocketFactory = SSLSocketFactory.getDefault()
                    val sslsocket = sslSocketFactory.createSocket()

                    // SSL Connect with TimeOut
                    sslsocket.connect(InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT), SERVER_TIMEOUT_MILLISEC)

                    // 入出力バッファ
                    val input = sslsocket.inputStream
                    val output = sslsocket.outputStream
                    val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
                    val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

                    // Send message
                    bufWriter.write(message)
                    bufWriter.flush()

                    // Receive message
                    val recMessage = bufReader.use(BufferedReader::readText)

                    if (recMessage == SERVER_REPLY_FAIL)
                        Log.d(TAG, "sendInfoToServer; Couldn't write a record on sk2 server")
                    else
                        Log.d(TAG, "sendInfoToServer; Success recording on sk2 server")

                } catch (e: Exception) {

                    Log.d(TAG, "sendInfoToServer; Couldn't connect to sk2 server")
                }
            }
            // Update notification content if running as a foreground service.
            if (serviceIsRunningInForeground(this)) {
                mNotificationManager!!.notify(NOTIFICATION_ID, notification)
            }
        } else {
            // サーバエラーのブロードキャストメッセージ送信
            sendBroadcastMessage(BROADCAST_ATTEND_NO_BEACON)
        }
        return
    }
    ////////////////////////////////////////
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Service が Nortification から呼ばれたか？
        val startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false)

        // Nortification から呼ばれたなら Foreground なので Service を一旦止める
        if (startedFromNotification) {
            Log.d(TAG,"Start from notification")
            removeScanUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY
    }
    ////////////////////////////////////////
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //mChangingConfiguration = true
    }
    ////////////////////////////////////////
    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "in onBind()")

        // Bind 時には Background として動かすので Foreground を止める
        stopForeground(true)
        //mChangingConfiguration = false
        return mBinder
    }
    ////////////////////////////////////////
    override fun onRebind(intent: Intent) {
        Log.d(TAG, "in onRebind()")

        // Bind 時には Background として動かすので Foreground を止める
        stopForeground(true)
        //mChangingConfiguration = false
        super.onRebind(intent)
    }
    ////////////////////////////////////////
    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Last client unbound from service")

        // UnBind するときに Service を走らせているなら、Foreground で再起動
        if (sk2.getScanRunning()) {
            Log.d(TAG, "Starting foreground service")
            startScanForeground()
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        // スキャナを止めて
        removeScanUpdates()
        // コールバックを止めて
        intervalHandler.removeCallbacks(timer)
        btWakeupHandler.removeCallbacks(wakeupTimer)
        // サービスハンドラを捨てる
        mServiceHandler!!.removeCallbacksAndMessages(null)
    }
    ////////////////////////////////////////
    /*** BLE スキャンを開始 ***/
    fun requestScanUpdates() {
        Log.d(TAG, "Requesting BLE scan updates")

        // BLE がある && スキャンサービスが走ってない
        if (sk2.checkBt() && !sk2.getScanRunning()) {
            mScanner.stopScan(mScanCallback) // 停止 for safety
            // BLEスキャン開始
            mScanner.startScan(scanFilters, scanSettings, mScanCallback) // start Ble Scanner
            // Status を更新
            sk2.setScanRunning(true)

            startService(Intent(applicationContext, ScanService::class.java))

            Log.d(TAG, "BLE Scanner is started")
        } else {
            //info("BLE Scanner can not be started; BLE not found")
        }
    }
    ////////////////////////////////////////
    /*** BLE スキャンを停止 ***/
    fun removeScanUpdates() {
        //info("Removing BLE scan updates")

        mScanner.stopScan(mScanCallback)   // stop Ble Scanner
        sk2.setScanRunning(false)
    }
    ////////////////////////////////////////////////////////////////////////////////
    /*** ScanService を Foreground で開始 ***/
    fun startScanForeground() {
        // auto interval
        if (pref.getBoolean(PREF_AUTO, false))
            startInterval(pref.getBoolean(PREF_DEBUG, false))

        val fgIntent = Intent(this, ScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.startForegroundService(fgIntent)
        else
            this.startService(fgIntent)

        startForeground(NOTIFICATION_ID, notification)
    }
    ////////////////////////////////////////
    /*** BLE スキャナを再スタート ***/
    fun restartScanner() {
        Log.d(TAG, "Restart BLE scanner")

        mScanner.stopScan(mScanCallback)
        mScanner.startScan(scanFilters, scanSettings, mScanCallback) // start Ble Scanner

        //if (pref.getBoolean(PREF_AUTO, false))
        //    startInterval(pref.getBoolean(PREF_DEBUG, false))
    }
    ////////////////////////////////////////
    /*** 自分への Binder ***/
    inner class LocalBinder : Binder() {
        internal val service: ScanService
            get() = this@ScanService
    }
    ////////////////////////////////////////
    @Suppress("DEPRECATION")
    /*** サービスが Foreground で動いているかをチェック ***/
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
}
