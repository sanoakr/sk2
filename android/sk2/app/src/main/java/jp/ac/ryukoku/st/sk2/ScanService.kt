package jp.ac.ryukoku.st.sk2

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_NO_BEACON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BROADCAST_ATTEND_SUCCESS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.CHANNEL_DESCRIPTION
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.CHANNEL_ID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.MAX_COUNT_NOBEACON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_ID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_TEXT_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_TEXT_STARTFORGROUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NOTIFICATION_TITLE_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_KEY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_ALARM
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_PERIOD_IN_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TYPE_SEND_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TYPE_SEND_MANUAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.VALID_IBEACON_UUID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.apNameMap
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.localQueue
import me.mattak.moment.Moment
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

/** ////////////////////////////////////////////////////////////////////////////// **/
/*** BLEスキャン・出席記録送信用サービスクラス ***/
class ScanService : Service(), AnkoLogger /*, BootstrapNotifier*/ {
    companion object {
        val TAG = this::class.java.simpleName

        /*** my SharedPreferences ***/
        lateinit var sk2: Sk2Globals
        lateinit var pref: SharedPreferences
    }
    /*** BLE Scanner ***/
    private lateinit var mScanner: BluetoothLeScannerCompat
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilters: ArrayList<ScanFilter>
    /** Scan Result **/
    private lateinit var scanArray: ScanArray


    /*** Notification Manager ***/
    private var pendingIntent: PendingIntent? = null
    private var notificationManager: NotificationManager? = null

    private val notificationBuilder: NotificationCompat.Builder
        get() {
            return NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE_TEXT)
                    .setContentText("Sk2 Content Text")
                    .setSmallIcon(R.drawable.ic_stat_sk2)
                    //.setSmallIcon(android.R.drawable.btn_star)
                    .setAutoCancel(true)
        }

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreate() {
        super.onCreate()
        sk2 = this.application as Sk2Globals
        pref = Sk2Globals.pref

        /** Notification **/
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        /** Notification　Channel 設定 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, NOTIFICATION_TITLE_TEXT,
                    NotificationManager.IMPORTANCE_LOW)
            channel.description = CHANNEL_DESCRIPTION
            channel.setSound(null, null)
            channel.enableLights(false)
            channel.lightColor = Color.BLACK
            channel.enableVibration(false)

            notificationManager?.createNotificationChannel(channel)
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), 0)
    }

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val send = intent?.getBooleanExtra(SCANSERVICE_EXTRA_SEND, false)
        val alarm = intent?.getBooleanExtra(SCANSERVICE_EXTRA_ALARM, false)

        Log.i(TAG, "on StartCommand: send=$send, alarm=$alarm")

        /** Type Check: Alarm からだと AUTO **/
        val type = if (alarm == true) TYPE_SEND_AUTO else TYPE_SEND_MANUAL

        /** Initialize Scan Result **/
        scanArray = ScanArray()

        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Start Scan **/
        doAsync {
            /** Create & Start BLE Scanner **/
            mScanner = BluetoothLeScannerCompat.getScanner()
            scanSettings = ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(SCAN_PERIOD_IN_MILLISEC)
                    .setUseHardwareBatchingIfSupported(false)
                    .build()

            val builder = ScanFilter.Builder()
            /** Apple Manufacture ID 0x004c: iBeacon **/
            builder.setManufacturerData(0x004c, byteArrayOf())
            scanFilters = arrayListOf(builder.build())
            /** // フィルターが空だとバックグラウンドで止まる?
            scanFilters = ArrayList() // ArrayList<ScanFilter>() // フィルタは空 == 全て受け取る **/

            /** ブロードキャストメッセージを送信 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_SCAN)

            /** BLE Scan **/
            var countScan = 0
            do {
                sk2.setScanRunning(true)
                mScanner.startScan(scanFilters, scanSettings, mScanCallback)
                Thread.sleep(SCAN_PERIOD_IN_MILLISEC)
                mScanner.stopScan(mScanCallback)
                sk2.setScanRunning(false)
                countScan++
            /** カラなら MAX_COUNT までリトライ **/
            } while(scanArray.isEmpty() && countScan < MAX_COUNT_NOBEACON)

            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** 空でなければサーバ送信 **/
            if (scanArray.isNotEmpty() && send == true) {
                sendInfoToServer(type)
            } else {
                sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_NO_BEACON)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** Stop Service **/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            else
                stopForeground(true)
        }

        /** ////////////////////////////////////////////////////////////////////////////// **/
        val notification = notificationBuilder
                .setContentText(NOTIFICATION_TEXT_STARTFORGROUND)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()
        startForeground(NOTIFICATION_ID, notification)

        /** ////////////////////////////////////////////////////////////////////////////// **/
        /** Set Next Alarm for Repeat **/
        if (sk2.getAutoRunning()) sk2.setAlarmService()

        return START_NOT_STICKY
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** BLE スキャナのコールバック **/
    private val mScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            scanArray = ScanArray(listOf(result))
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            scanArray = ScanArray(results)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "ScanFailed: $errorCode")
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
    private fun sendInfoToServer(type: String) { // not Boolean, couldn't return from in any async blocks
        val curDatetime = Moment() // 現在時刻

        Log.d(TAG, "Try to send an attendance info to the Server with type $type")

        /*** 送信可能時刻をチェック ***/
        // debug モードでなければ、’..TIME_FROM'時から’..TIME_TO'時の間でないなら終了
        if ( (! pref.getBoolean(PREF_DEBUG, false))
                && (curDatetime.hour < Sk2Globals.PERMISSION_SENDING_TIME_FROM
                        || curDatetime.hour > Sk2Globals.PERMISSION_SENDING_TIME_TO)) {

            /** 失敗のブロードキャストメッセージを送信して終了 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_NO_VALITTIME)
            Log.d(TAG,"sendInfoToServer; Out of the time for be permitted")
        }

        val message: String  /** 送信データ用文字列 **/
        /** ////////////////////////////////////////////////////////////////////////////// **/
        /*** ビーコンがあれば送信する ***/
        if (scanArray.count() > 0) {

            /** ブロードキャストメッセージを送信 **/
            sendBroadcastMessage(Sk2Globals.BROADCAST_ATTEND_TRY)

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
            info(localQueue)
            Sk2Globals.localQueue.push(
                    Triple(scanArray.datetime.toString(), type, scanArray.getStatisticalList()))
            sk2.saveQueue()

            /** スキャンデータをブロードキャストメッセージで送信 **/
            sendBroadcastScanResult(scanArray.getBeaconsText(
                    statistic = true, signal = true, map = apNameMap))

            /** Notification を送信 **/
            val notification = notificationBuilder
                    .setContentText(NOTIFICATION_TEXT_SEND)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build()
            notificationManager?.notify(NOTIFICATION_ID, notification)

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

                    /** 送信完了のブロードキャストメッセージ送信 **/
                    sendBroadcastMessage(BROADCAST_ATTEND_SUCCESS)

                } catch (e: Exception) {
                    /** 送信失敗のブロードキャストメッセージ送信 **/
                    sendBroadcastMessage(BROADCAST_ATTEND_FAIL)

                    Log.d(TAG, "sendInfoToServer; Couldn't connect to sk2 server")
                }
            }
        } else {
            /** ビーコンが見つからないときのブロードキャストメッセージ送信 **/
            sendBroadcastMessage(BROADCAST_ATTEND_NO_BEACON)
        }
        return
    }
}
