package jp.ac.ryukoku.st.sk2

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import java.lang.reflect.Type

/** ////////////////////////////////////////////////////////////////////////////// **/
class Sk2Globals: Application() {
    /** ////////////////////////////////////////////////////////////////////////////// **/
    companion object {
        const val APP_NAME = "sk2"
        const val APP_TITLE = "龍大理工学部出欠システム"

        const val TITLE_LOGIN = "ログイン"
        const val TITLE_RECORD = "出席記録"
        const val TITLE_RECORD_TAB_SERVER = "on Server"
        const val TITLE_RECORD_TAB_LOCAL = "on Local"
        const val TITLE_HELP = "ヘルプ"

        /*** SharedPreferences の名前 ***/
        const val SHARED_PREFERENCES_NAME = "st.ryukoku.sk2"
        /*** SharedPreference Keys ***/
        const val PREF_UID = "uid"            // ユーザーID
        const val PREF_KEY = "key"            // サーバ認証キー
        const val PREF_USER_NAME = "name"     // ユーザー名
        const val PREF_LOGIN_TIME = "time"    // ログイン時刻: System.currentTimeMillis(): Long
        const val PREF_DEBUG = "debug"        // デバッグ
        const val PREF_AUTO = "auto"          // 自動送信
        const val PREF_LOCAL_QUEUE = "queue"  // ローカルキュー保存用
        /*** BLE Scan の実行自体を保存する SharedPreference のキー ***/
        const val SCAN_RUNNING = "scan_running"
        /*** login 情報の有効期限 ***/
        const val LOGIN_TIME_DAY_UNIT_MILLSEC: Long = 24*60*60*1000 // 1 days
        const val LOGIN_EXPIRY_PERIOD_DAYS: Long = 150

        /*** SK2 サーバ ***/
        const val SERVER_HOSTNAME = "sk2.st.ryukoku.ac.jp"
        const val SERVER_PORT = 4440                        // 認証・データ登録用ポート
        const val SERVER_INFO_PORT = 4441                   // 出席データ取得用ポート
        const val SERVER_TIMEOUT_MILLISEC = 5000
        const val SERVER_HELP_URI = "https://sk2.st.ryukoku.ac.jp/index.html"

        /*** サーバ コマンドキー ***/
        const val SERVER_COMMAND_AUTH = "AUTH"
        const val SERVER_REPLY_AUTH_FAIL = "authfail"
        const val SERVER_REPLY_FAIL = "fail"
        const val NAME_START_TESTUSER = "testuser"         // デバッグユーザー名の開始文字

        /*** View テキストサイズ ***/
        const val TEXT_SIZE_NORMAL = 10f
        const val TEXT_SIZE_LARGE = 12f
        const val TEXT_SIZE_ATTEND = 36f                    // 出席ボタン

        /*** ScanService Extras ***/
        const val SCANSERVICE_EXTRA_SEND = "send_to_server"
        const val SCANSERVICE_EXTRA_AUTO = "auto_interval"
        const val SCANSERVICE_EXTRA_ALARM = "from_alarm"

        /*** Broadcast Extras ***/
        const val ACTION_BROADCAST = "$APP_NAME.broadcast"
        const val EXTRA_BLESCAN = "$APP_NAME.scan"
        const val EXTRA_TOAST = "$APP_NAME.toast"
        /*** Broadcast メッセージ ***/
        const val BROADCAST_ATTEND_SEND = "出席"
        const val BROADCAST_ATTEND_NO_BEACON = "ビーコンが見つかりません"
        const val BROADCAST_ATTEND_NO_VALITTIME = "現在の時刻には送信できません"

        /*** Nortification ***/
        const val NOTIFICATION_ID: Int = 123
        const val CHANNEL_ID = "channel_123"
        const val EXTRA_STARTED_FROM_NOTIFICATION = "$APP_NAME.started_from_notification"
        /*** Nortification メッセージ ***/
        const val NOTIFICATION_TITLE_TEXT = "sk2 サーバへの出席データ送信"
        const val NOTIFICATION_ACTION_LAUNCE_TEXT = "sk2 アプリを起動"
        //const val NOTIFICATION_STOP_SCAN_TEXT = "BLE スキャンを停止"
        const val NOTIFICATION_CHANNEL_NAME_TEXT = "Sk2 BLE Scanner & Attendance Foreground Service"

        /*** Timer ***/
        // BLE Scan インターバル
        const val SCAN_PERIOD_IN_MILLISEC: Long = 3000                   // 3 sec.
        // 自動記録のインターバル
        const val SCAN_INTERVAL_IN_MILLISEC: Long = 10*60*1000           // 10 min.
        const val SCAN_INTERVAL_IN_MILLISEC_DEBUG: Long = 60*1000        // 1 min.

        /** Local Queue Lenght **/
        const val LOCAL_QUEUE_MAX_LENGTH: Int = 100

        /*** Toast メッセージ ***/
        const val TEXT_OK = "OK"
        const val TEXT_SETTINGS = "設定"
        // Login
        const val TOAST_LOGIN_ATTEMPT_UID = "学籍番号を入力して下さい"
        const val TOAST_LOGIN_ATTEMPT_PASSWD = "パスワードを入力して下さい"
        const val TOAST_LOGIN_ATTEMPT_ATMARK = "認証IDに @ 以降を含めないで下さい"
        const val TOAST_LOGIN_SUCCESS = "ログインします"
        const val TOAST_LOGIN_FAIL = "ログインに失敗しました"
        // Check BLE
        const val TOAST_CHECK_BLE_NON = "この端末のBLEアダプタが見つかりません"
        const val TOAST_CHECK_BLE_OFF = "Bluetoothをオンにしてください"
        // Location Permittion
        const val LOCATION_PERMISSION_REQUEST_MESSAGE = "位置情報へのアクセス権が必要です"
        const val LOCATION_PERMISSION_DENIED_MESSAGE = "位置情報へのアクセスが許可されませんでした"
        // Server
        const val TOAST_CANT_CONNECT_SERVER = "サーバに接続できません"

        /*** パーミッション変更時のリクエストコード（任意の整数でよい） ***/
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        /*** サーバ上の1レコードあたりのBeaconデータの上限 ***/
        const val MAX_SEND_BEACON_NUM: Int = 3

        /*** 出席ボタンを押したときのバイブレーション継続時間 msec; 0 にするとエラー ***/
        const val ATTENDANCE_VIBRATE_MILLISEC: Long = 1

        /*** 送信できるのは FROM時0分から TO時59分まで ***/
        const val PERMISSION_SENDING_TIME_FROM: Int = 8
        const val PERMISSION_SENDING_TIME_TO: Int = 20

        /*** 受信する iBeacon のUUID、BLE ***/
        val VALID_IBEACON_UUID: List<String> = listOf(
                "ebf59ccc-21f2-4558-9488-00f2b388e5e6", // ru-wifi
                "00000000-87b3-1001-b000-001c4d975326"  // sekimoto's
        )
        //val UUID_MASK = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"

        /**  SharedPreferences **/
        lateinit var pref: SharedPreferences
        /**  出席情報のローカル記録用キュー **/
        lateinit var localQueue: Queue<Triple<String,Char,List<List<Any>>>>
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreate() {
        super.onCreate()
        localQueue = Queue(mutableListOf(), LOCAL_QUEUE_MAX_LENGTH)
        pref = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        // ローカルキューをリストア
        restoreQueue()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** ローカルキューを Json String に変換して SharedPreferences に保存 ***/
    fun saveQueue(clear: Boolean = false) {
        if (clear) localQueue = Queue()

        val gson = Gson()
        val jsonString = gson.toJson(localQueue)

        pref.edit()
                .putString(PREF_LOCAL_QUEUE, jsonString as String)
                .apply()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** SharedPreferences からローカルキューの情報をリストア ***/
    fun restoreQueue() {
        val gson = Gson()
        val jsonString = pref.getString(PREF_LOCAL_QUEUE,
                gson.toJson(Queue<Triple<String,Char,List<List<Any>>>>(mutableListOf())))
        val type: Type = object: TypeToken<Queue<Triple<String, Char, Collection<Collection<Any>>>>>(){}.type
        localQueue = gson.fromJson<Queue<Triple<String, Char, List<List<Any>>>>>(jsonString, type)

    }
    /** ////////////////////////////////////////////////////////////////////////////// **/    /*** ログアウト時にユーザ情報を持つ全てのSharedPreferenceをクリア ***/
    fun logout() {
        pref.edit()
                .putString(PREF_UID, "")
                .putString(PREF_KEY, "")
                .putString(PREF_USER_NAME, "")
                .putLong(PREF_LOGIN_TIME, 0L)
                .putBoolean(PREF_DEBUG, false)
                .putBoolean(PREF_AUTO, false)
                .apply()

        // Clear Local Queue
        saveQueue(clear = true)
        // back to the Login Activity
        startActivity(intentFor<LoginActivity>().clearTop())
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE Scan が実行中か？ ***/
    fun getScanRunning(): Boolean {
        return pref.getBoolean(SCAN_RUNNING, false)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE Scan の状態を設定 ***/
    fun setScanRunning(running: Boolean){
        pref.edit()
                .putBoolean(SCAN_RUNNING, running)
                .apply()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Auto Alarm が実行中か？ ***/
    fun getAutoRunning(): Boolean {
        return pref.getBoolean(PREF_AUTO, false)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Auto Alarm の状態を設定 ***/
    fun setAutoRunning(running: Boolean){
        pref.edit()
                .putBoolean(PREF_AUTO, running)
                .apply()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE デバイスがあるか？ ***/
    private fun hasBLE(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE の有無とON/OFF をチェック ***/
    fun checkBt(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (btAdapter == null || !hasBLE()) {
            toast(TOAST_CHECK_BLE_NON)  // BLE がない
            return false
        } else if (!btAdapter.isEnabled) {
            toast(TOAST_CHECK_BLE_OFF)  // BLE が OFF
            return false
        }
        return true
    }

}


