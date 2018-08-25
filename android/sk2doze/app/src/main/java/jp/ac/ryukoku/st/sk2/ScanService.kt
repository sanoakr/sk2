package jp.ac.ryukoku.st.sk2

import android.app.*
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.CHANNEL_ID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_TITLE_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_KEY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_ALARM
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_INTERVAL_IN_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_INTERVAL_IN_MILLISEC_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_PERIOD_IN_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.VALID_IBEACON_UUID
import me.mattak.moment.Moment
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.doAsync
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory



/** ////////////////////////////////////////////////////////////////////////////// **/
/*** BLEスキャン・出席記録送信用サービスクラス ***/
class ScanService : Service() /*, BootstrapNotifier*/ {
    companion object {
        val TAG = this::class.java.simpleName
    }
    /*** my SharedPreferences ***/
    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    /*** BLE Scanner ***/
    private lateinit var mScanner: BluetoothLeScannerCompat
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilters: ArrayList<ScanFilter>
    /** Scan Result **/
    private lateinit var scanArray: ScanArray

    /**
    /*** Notification Manager ***/
    private var mNotificationManager: NotificationManager? = null

    private val notification: Notification
        get() {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE_TEXT)
                    .setContentText("通知の内容")
                    .setSmallIcon(R.drawable.ic_launcher_background)
            return builder.build()
        }
    **/
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreate() {
        super.onCreate()
        sk2 = this.application as Sk2Globals
        pref = Sk2Globals.pref
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onDestroy() {
        super.onDestroy()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val send = intent?.getBooleanExtra(SCANSERVICE_EXTRA_SEND, false)
        val auto = intent?.getBooleanExtra(SCANSERVICE_EXTRA_AUTO, false)
        val alarm = intent?.getBooleanExtra(SCANSERVICE_EXTRA_ALARM, false)

        Log.i(TAG, "on StartCommand: send=$send, auto=$auto, alarm=$alarm")

        /** Type Check: Alarm からだと AUTO **/
        val type = if (alarm == true) 'A' else 'M'

        /** Initialize Scan Result **/
        scanArray = ScanArray()

        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Notification **/
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification　Channel 設定
            val channel = NotificationChannel(CHANNEL_ID, NOTIFICATION_TITLE_TEXT, NotificationManager.IMPORTANCE_LOW)
            channel.setDescription("Silent Notification")
            // 通知音を消さないと毎回通知音が出てしまう
            // この辺りの設定はcleanにしてから変更
            channel.setSound(null, null)
            // 通知ランプを消す
            channel.enableLights(false)
            channel.setLightColor(Color.BLACK)
            // 通知バイブレーション無し
            channel.enableVibration(false)

            notificationManager.createNotificationChannel(channel);
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(NOTIFICATION_TITLE_TEXT)
                // android標準アイコンから
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText("Sk2 content textr")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()

        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Stop Repeat and Service **/
        if (auto == false) { // Alarm からでなくて、停止 Extra があるとき
            stopAlarmService()
            Log.i(TAG, "Stop Alarm")
            doAsync {
                notificationManager.notify(1, notification)
            }
        }
        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Start Scan **/
        doAsync {
            /** Create & Start BLE Scanner **/
            mScanner = BluetoothLeScannerCompat.getScanner()
            scanSettings = ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .setUseHardwareBatchingIfSupported(false)
                    .build()

            val builder = ScanFilter.Builder()
            /** Apple Manufacture ID 0x004c: iBeacon **/
            builder.setManufacturerData(0x004c, byteArrayOf())
            scanFilters = arrayListOf(builder.build())
            /** // フィルターが空だとバックグラウンドで止まる?
            scanFilters = ArrayList() // ArrayList<ScanFilter>() // フィルタは空 == 全て受け取る **/

            /** BLE Scan **/
            sk2.setScanRunning(true)
            mScanner.startScan(scanFilters, scanSettings, mScanCallback)
            Thread.sleep(SCAN_PERIOD_IN_MILLISEC)
            mScanner.stopScan(mScanCallback)
            sk2.setScanRunning(false)

            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** 空でなければ最新スキャンに保存して、MainActivity にBroadcastで送る & サーバ送信 **/
            if (scanArray.isNotEmpty()) {
                /** Broadcast Scan Result **/
                sendBroadcastScanResult(scanArray.getBeaconsText(
                        statistic = true, signal = true, ios = true))

                /** Send to Server **/
                if (send == null || send == true) // Alarm からのときも送信
                    sendInfoToServer(type)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** Stop Service **/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            else
                stopForeground(true)
        }

        /** ////////////////////////////////////////////////////////////////////////////// **/
        startForeground(1, notification)

        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Start Repeat **/
        if (auto == true) {
            if (pref.getBoolean(PREF_DEBUG, false))
                setAlarmService(SCAN_INTERVAL_IN_MILLISEC_DEBUG)
            else
                setAlarmService(SCAN_INTERVAL_IN_MILLISEC)
            Log.i(TAG, "Start Alarm")
        }
        return START_NOT_STICKY
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** BLE スキャナのコールバック **/
    private val mScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onReceivedBleScan(listOf(result))
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            onReceivedBleScan(results)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "ScanFailed: $errorCode")
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE スキャン受診時のコールバック ***/
    private fun onReceivedBleScan(results: List<ScanResult>) {
        /** スキャン日時 **/
        scanArray.datetime = Moment()
        /** ビーコン情報をパースして ScanArray() に保存 **/
        results.forEach { r ->
            val bytes = r.scanRecord?.bytes
            val rssi = r.rssi
            val structures = ADPayloadParser.getInstance().parse(bytes)
            structures.forEach { s ->
                if (s is IBeacon && (s.uuid.toString() in VALID_IBEACON_UUID)) {
                    scanArray.add(Pair(s, rssi))

                    val dist = getBleDistance(s.power, rssi)
                    Log.i(TAG,"${s.uuid}, ${s.major}, ${s.minor}, ${dist}")
                }
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** ブロードキャストでスキャン結果を送信: to MainActivity.MyReceiver.BroadcastReceiver() ***/
    private fun sendBroadcastScanResult(result: String) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_BLESCAN, result)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** ブロードキャストメッセージを送信: to MainActivity.MyReceiver.BroadcastReceiver() ***/
    private fun sendBroadcastMessage(message: String) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_TOAST, message)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** 出席データをサーバに送信 ***/
    fun sendInfoToServer(type: Char) { // not Boolean, couldn't return from in any async blocks
        val curDatetime = Moment() // 現在時刻

        Log.d(TAG, "Try to send an attendance info to the Server with type $type")

        /*** 送信可能時刻をチェック ***/
        // debug モードでなければ、’..TIME_FROM'時から’..TIME_TO'時の間でないなら終了
        if ( (! pref.getBoolean(PREF_DEBUG, false))
                && (curDatetime.hour < Sk2Globals.PERMISSION_SENDING_TIME_FROM
                        || curDatetime.hour > Sk2Globals.PERMISSION_SENDING_TIME_TO)) {

            /** 失敗のブロードキャストメッセージを送信して終了 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_NO_VALITTIME)
            Log.d(TAG,"sendInfoToServer; Out of the time for be parmitted")
            return
        }

        var message: String  /** 送信データ用文字列 **/
        /** ////////////////////////////////////////////////////////////////////////////// **/
        /*** ビーコンがあれば送信する ***/
        if (scanArray.count() > 0) {

            /** 日時、ユーザ、認証キー **/
            val dtString = scanArray.datetime.format("yyy-MM-dd HH:mm:ss")
            val uid = pref.getString(PREF_UID, "")
            val key = pref.getString(PREF_KEY, "")

            /** 送信データは「ラベルなし」で生成 **/
            message = "$uid,$key,$type,$dtString" +
                    scanArray.getBeaconsText(label=false, time=false, uuid=false,
                            signal=false, ios=false, statistic=true)
            Log.d(TAG,"SEND to sk2; $message")

            /** ローカル記録キューに追加（送信の可否に依存しない） **/
            sk2.localQueue.push(
                    Pair(addWeekday(scanArray.datetime.toString()), scanArray.getStatisticalList()))
            Log.i(TAG, Pair(addWeekday(scanArray.datetime.toString()), scanArray.getStatisticalList()).toString())

            /** ブロードキャストメッセージを送信 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_SEND)

            /** ////////////////////////////////////////////////////////////////////////////// **/
            /*** サーバ接続 ***/
            doAsync {
                try {
                    /** SSL Socket **/
                    val sslSocketFactory = SSLSocketFactory.getDefault()
                    val sslsocket = sslSocketFactory.createSocket()

                    /** SSL Connect with TimeOut **/
                    sslsocket.connect(InetSocketAddress(Sk2Globals.SERVER_HOSTNAME, Sk2Globals.SERVER_PORT), Sk2Globals.SERVER_TIMEOUT_MILLISEC)

                    /** 入出力バッファ **/
                    val input = sslsocket.inputStream
                    val output = sslsocket.outputStream
                    val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
                    val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

                    /** Send message **/
                    bufWriter.write(message)
                    bufWriter.flush()

                    /** Receive message **/
                    val recMessage = bufReader.use(BufferedReader::readText)

                    if (recMessage == Sk2Globals.SERVER_REPLY_FAIL)
                        Log.d(TAG, "sendInfoToServer; Couldn't write a record on sk2 server")
                    else
                        Log.d(TAG, "sendInfoToServer; Success recording on sk2 server")

                } catch (e: Exception) {

                    Log.d(TAG, "sendInfoToServer; Couldn't connect to sk2 server")
                }
            }
        } else {
            /** サーバエラーのブロードキャストメッセージ送信 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_NO_BEACON)
        }
        return
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Alarm をセット ***/
    private fun setAlarmService(period: Long) {
        val intent = Intent(this, ScanService::class.java)
        /** Alarm からは、サーバ送信 + 自動継続 **/
        intent.putExtra(SCANSERVICE_EXTRA_SEND, true)
        intent.putExtra(SCANSERVICE_EXTRA_AUTO, true)
        intent.putExtra(SCANSERVICE_EXTRA_ALARM, true)

        val pendingIntent= PendingIntent.getService(this, 0, intent,
                FLAG_UPDATE_CURRENT) /** Update extra data **/

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val startMillis = System.currentTimeMillis() + period

        // Oreo 以降なら Doze から抜ける
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent)

        sk2.setAutoRunning(true)
    }

    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Alarm を解除 ***/
    private fun stopAlarmService() {
        val indent = Intent(this, ScanService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, indent, 0)

        /** アラームを解除する **/
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        sk2.setAutoRunning(false)
    }
}
