package jp.ac.ryukoku.st.sk2

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.*
import java.lang.reflect.Type

/** ////////////////////////////////////////////////////////////////////////////// **/
class Sk2Globals: Application(), AnkoLogger {
    /** ////////////////////////////////////////////////////////////////////////////// **/
    companion object {
        const val APP_NAME = "sk2"
        const val APP_TITLE = "龍大理工学部出欠システム"

        const val TITLE_RECORD = "出席ログ"
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
        const val PREF_ROOM_JSON = "room_json"// AP情報 JSON
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
        const val NAME_DEMOUSER = "$NAME_START_TESTUSER-demo"

        /*** View カラー ***/
        var COLOR_BACKGROUND = Color.parseColor("#ecf0f1")
        var COLOR_BACKGROUND_TITLE = Color.parseColor("#eeeeee")
        var COLOR_NORMAL = Color.parseColor("#3498db")
        //var COLOR_ACTIVE = Color.parseColor("#1abc9c")
        //var COLOR_ONDOWN = Color.parseColor("#2980b9")
        //var COLOR_DISABLE = Color.parseColor("#bdc3c7")
        /*** View テキストサイズ ***/
        const val TEXT_SIZE_TINY = 8f
        const val TEXT_SIZE_NORMAL = 10f
        const val TEXT_SIZE_Large = 14f
        const val TEXT_SIZE_LARGE = 16f
        const val TEXT_SIZE_ATTEND = 28f                    // 出席ボタン

        /** LoginActivity Button サイズ & テキスト **/
        const val BUTTON_TEXT_LOGIN = "ログイン"
        const val HINT_UID = "全学統合認証ID"
        const val HINT_PASSWD = "パスワード"

        /** MainActivity Button サイズ & テキスト **/
        const val BUTTON_SIZE_ATTEND = 200
        const val BUTTON_MARGIN_ATTEND = 50
        const val BUTTON_TEXT_ATTEND_TRUE = "出席"
        const val BUTTON_TEXT_ATTEND_FALSE = "利用不可"
        const val BUTTON_SIZE_MENU = 60
        const val BUTTON_TEXT_LOG = "LOG"
        const val BUTTON_TEXT_HELP = "HELP"
        const val BUTTON_TEXT_EXIT = "LOGOUT"

        const val BUTTON_MARGIN_MENU = 20
        const val SWITCH_TEXT_AUTO = "AUTO"

        /*** ScanService Extras ***/
        const val SCANSERVICE_EXTRA_SEND = "send_to_server"
                const val SCANSERVICE_EXTRA_ALARM = "from_alarm"

        /*** Broadcast Extras ***/
        const val ACTION_BROADCAST = "$APP_NAME.broadcast"
        const val EXTRA_BLESCAN = "$APP_NAME.scan"
        const val EXTRA_TOAST = "$APP_NAME.toast"
        /*** Broadcast メッセージ ***/
        const val BROADCAST_ATTEND_SCAN = "スキャンを開始します"
        const val BROADCAST_ATTEND_TRY = "ビーコン情報を送信します"
        const val BROADCAST_ATTEND_SUCCESS = "送信完了！"
        const val BROADCAST_ATTEND_FAIL = "送信に失敗しました"
        const val BROADCAST_ATTEND_NO_BEACON = "ビーコンが見つかりません"
        const val BROADCAST_ATTEND_NO_VALITTIME = "現在の時刻には送信できません"
        /** Debug Info Message **/
        const val SCAN_INFO_NOT_FOUND = "Any Beacons not found."

        /** Logout Dialog **/
        const val LOGOUT_DIALOG_TITLE = "ログアウト"
        const val LOGOUT_DIALOG_MSG = "ログアウトしますか？"
        const val LOGOUT_DIALOG_OK = "ログアウト"
        const val LOGOUT_DIALOG_CANCEL = "キャンセル"

        /*** Nortification ***/
        const val NOTIFICATION_ID: Int = 1
        const val CHANNEL_ID = "channel_sk2"
        const val CHANNEL_DESCRIPTION = "Sk2 Silent Notification"
        /*** Nortification メッセージ ***/
        const val NOTIFICATION_TITLE_TEXT = "sk2"
        const val NOTIFICATION_TEXT_STARTFORGROUND = "Foreground サービスを開始"
        const val NOTIFICATION_TEXT_SEND = "出席データを送信"

        /*** Timer ***/
        // BLE Scan する時間長
        const val SCAN_PERIOD_IN_MILLISEC: Long = 1000
        const val MAX_COUNT_NOBEACON = 10
        // 自動記録のインターバル
        const val AUTO_SEND_INTERVAL_IN_MILLISEC: Long = 10*60*1000
        const val AUTO_SEND_INTERVAL_IN_MILLISEC_DEBUG: Long = 1*60*1000
        /** Auto Interval Alarm **/
        // リクエストコード
        const val ALARM_REQUEST_CODE_AUTO = 0

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
        // Auto Switch
        const val TOAST_MAIN_AUTO_ON = "Auto ON"
        const val TOAST_MAIN_AUTO_OFF = "Auto OFF"
        // Check BLE
        const val TOAST_CHECK_BLE_NON = "この端末のBLEアダプタが見つかりません"
        const val TOAST_CHECK_BLE_OFF = "Bluetoothをオンにしてください"
        // Location Permittion
        const val LOCATION_PERMISSION_REQUEST_MESSAGE = "位置情報へのアクセス権が必要です"
        const val LOCATION_PERMISSION_DENIED_MESSAGE = "位置情報へのアクセスが許可されませんでした"
        // Server
        const val TOAST_CANT_CONNECT_SERVER = "サーバに接続できません"
        // Log Records
        const val TOAST_LOG_NO_LOCAL_RECORDS = "ローカルログはありません。"
        const val TOAST_LOG_NO_SERVER_RECORDS = "サーバログはありません。"

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
        /*** AP Map の Key ***/
        const val APMAP_KEY_BEACON = "beaconIdParams"
        const val APMAP_KEY_BEACON_UUID = "uuid"
        const val APMAP_KEY_BEACON_MAJOR = "major"
        const val APMAP_KEY_BEACON_MINOR = "minor"
        const val APMAP_KEY_NAME = "name"

        /** プライバシーポリシー **/
        const val PRIVACY_POLICY_TITLE = "プライバシーポリシー"
        var PRIVACY_POLICY_TEXT = """

龍谷大学（以下「本学」）理工学部（以下「当学部」）は、当学部が提供するスマートフォン用アプリケーション「理工出席」（以下「本アプリ」）および本アプリによって提供するサービス（以下「本サービス」）を通じて利用者の位置情報をご提供いただきます。当学部は、本サービスの円滑な提供を実施させていただくために、本学が定めた龍谷大学プライバシーポリシーに基づき、本プライバシーポリシーを定め、利用者の情報の保護に努めます。

なお、利用者が本アプリによる情報提供を希望されない場合は、利用者自身の判断により、位置情報の提供を拒否することができます。この場合、本アプリおよび本サービスを利用になれない場合があります。

1. 本アプリにより本学部が取得する情報と利用目的

 当学部は、本アプリおよび本サービスの提供等にあたり、次の利用目的の達成に必要な範囲で下記に記載する情報をアプリケーション経由で自動的に取得および利用者の操作によって取得し、取扱います。

 ● 本アプリの認証時に当学部サーバに通知される全学統合認証ID
   ・ 利用目的
      本サービス提供の際に利用者を識別するため
   ・ 取得方法
      利用者によるログイン操作または、本アプリによる自動取得

 ● 利用端末のBluetooth機能を利用した、本学キャンパスおよび関連施設に限定した位置情報及び、その取得時刻

   ・ 利用目的
      本サービス提供の際、位置情報を確認し、利用者の講義等への出欠情報を取得するため
      利用者の位置情報を利用者個人が特定されないかたちで、本学の運営・教育・研究に利用するため
   ・ 取得方法
      利用者による取得送信操作及び本アプリによる定期的な自動取得
      ※ 位置情報は本アプリの起動後、利用者が出席情報の送信ボタンを押下したタイミングまたは、本アプリの自動送信機能をオンにした場合およそ10分間に1回、本学キャンパスおよびその関連施設内で、授業実施時間に限り取得します。

2. 本学部が取得する情報の加工および第三者提供

 当学部は、前条において取得した情報を、本サービスの提供に係る当学部のシステムへ取得・蓄積・転送し、前条の利用目的に利用します。
当学部は、前項の規定に関わらず、本アプリが取得する情報を次の各号のいずれかに該当すると認める場合は、本人の権利利益に最大限の配慮を払いつつ、個人情報を第三者に提供する場合があります。

 (1) 本人から同意を得た場合。
 (2) 法令に基づく場合。
 (3) 人の生命、身体又は財産の保護のために必要がある場合であって、本人の同意を得ることが困難である場合。
 (4) 公衆衛生の向上又は児童の健全な育成の推進のために特に必要がある場合であって、本人の同意を得ることが困難である場合。
 (5) 国の機関若しくは地方公共団体又はその委託を受けた者が、法令の定める事務を遂行することに協力する必要がある場合であって、本人の同意を得ることによりその遂行に支障を及ぼすおそれがある場合。
 (6) 取得した位置情報等を、本学部が利用者本人を特定できる情報を含まない、総体的かつ統計的なデータに加工した場合。

3. 本学部が取得する情報の取得・蓄積・利用に関する同意

 利用者は本アプリをインストールする際に本プライバシーポリシーを確認頂き、本アプリおよび本サービスに関する内容を理解した上でご利用ください。

4. 本学部が取得する情報の取得停止等

 本サービスでは、第1条に定める規定に基づき、取得した利用者情報の内容に関し、本学部の教学上の利用者本人に資する必要性がなくなった後に、照会・訂正・削除等を申請することができます。
本アプリおよび本サービスは、利用者が、本アプリが利用端末から削除（アンインストール）された場合、利用者情報は全て端末より直ちに削除されます。

5. 本学部が取得する情報の取り扱いに関する問い合わせ窓口

 本アプリおよび本サービスにおける本学部が取得する情報の取扱いに関して、ご意見・ご要望がございましたら、下記窓口までご連絡ください。

   窓口名称：龍谷大学理工学部教務課
   お問い合わせ方法：
     電話：077-543-7730
     電子メール： rikou@ad.ryukoku.ac.jp
   受付時間：平日午前9時～午後5時

6. 本プライバシーポリシーの変更

 本学部は、法令の変更等に伴い、本プライバシーポリシーを変更することがあります。
本学部は、本アプリのバージョンアップに伴って、本学部が取得する情報の取得項目の変更や追加、利用目的の変更、第三者提供等について変更がある場合には、本アプリ内で通知し、重要なものについてはインストール前もしくはインストール時にあらためて同意を取得させていただきます。
""".trimMargin()

        const val PRIVACY_POLICY_YES_TEXT = "同意します"
        const val PRIVACY_POLICY_NO_TEXT = "同意しません"

        /**  SharedPreferences **/
        lateinit var pref: SharedPreferences
        /**  出席情報のローカル記録用キュー **/
        lateinit var localQueue: Queue<Triple<String,Char,List<StatBeacon>>>
        /**  AP情報の Map **/
        lateinit var apInfos: List<Map<String, Any>>
        lateinit var apNameMap: MutableMap<Triple<String, Int, Int>, String>
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
    /** メイン画面へ **/
    fun startMain() {
        /** ユーザIDが空でなければ && 教室AP情報を持っていれば **/
        if (pref.getString(PREF_UID, "").isNotBlank() && pref.getString(PREF_ROOM_JSON, "").isNotBlank()) {
            /** 期限を確認して **/
            val today: Long = System.currentTimeMillis() / LOGIN_TIME_DAY_UNIT_MILLSEC
            if (today - pref.getLong(PREF_LOGIN_TIME, 0L) / LOGIN_TIME_DAY_UNIT_MILLSEC < LOGIN_EXPIRY_PERIOD_DAYS) {
                /** AP Map を作成 **/
                readApMap()
                /** そのままメインアクティビティへ **/
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** サーバからの AP JSON ファイルを List<Map> にして、さらに (Major, Minor) => Name の Map をつくる **/
    fun readApMap() {
        /** JSON から List<Map> **/
        val gson = Gson()
        val jsonString = pref.getString(PREF_ROOM_JSON, gson.toJson(mapOf<String, Any>()))
        if (jsonString != null) {
            val type: Type = object : TypeToken<List<Map<String, Any>>>() {}.type
            apInfos = gson.fromJson(jsonString, type)
        }
        /** List<Map> から (Major, Minor) => Name **/
        apNameMap = mutableMapOf()
        apInfos.forEach { ap ->
            if (ap.containsKey(APMAP_KEY_BEACON) && ap.containsKey(APMAP_KEY_NAME)) {
                val beacon = ap[APMAP_KEY_BEACON] as Map<String, String>
                val uuid = beacon[APMAP_KEY_BEACON_UUID] as String
                val major = (beacon[APMAP_KEY_BEACON_MAJOR] as Double).toInt()
                val minor = (beacon[APMAP_KEY_BEACON_MINOR] as Double).toInt()
                apNameMap[Triple(uuid, major, minor)] = ap[APMAP_KEY_NAME] as String
            }
        }
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
                gson.toJson(Queue<Triple<String,Char,List<StatBeacon>>>(mutableListOf())))
        val type: Type = object: TypeToken<Queue<Triple<String, Char, Collection<StatBeacon>>>>(){}.type
        val queue: Queue<Triple<String, Char,List<StatBeacon>>> = gson.fromJson<Queue<Triple<String, Char, List<StatBeacon>>>>(jsonString, type)

        /** remove recodes null data contained **/
        localQueue.clear()
        queue.items.forEach {r ->
            if (r.toList().any { e -> e != null })
                localQueue.push(r)
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** ログアウト時にユーザ情報を持つ全てのSharedPreferenceをクリア ***/
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
    /*** Auto Interval Alarm をセット ***/
    fun setAlarmService() {
        val intent = Intent(this, ScanService::class.java)
        /** Alarm からは、サーバ送信 + 自動継続 **/
        intent.putExtra(SCANSERVICE_EXTRA_SEND, true)
        intent.putExtra(SCANSERVICE_EXTRA_ALARM, true)

        val pendingIntent= PendingIntent.getService(this, ALARM_REQUEST_CODE_AUTO,
                intent, PendingIntent.FLAG_UPDATE_CURRENT) /** Update extra data **/

        // check debug
        val interval = if (pref.getBoolean(PREF_DEBUG, false)) AUTO_SEND_INTERVAL_IN_MILLISEC_DEBUG
        else AUTO_SEND_INTERVAL_IN_MILLISEC
        // interval
        val startMillis = System.currentTimeMillis() + interval

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        /** Oreo 以降なら Doze から抜ける **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent)

        setAutoRunning(true)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Auto Interval Alarm を解除 ***/
    fun stopAlarmService() {
        val indent = Intent(this, ScanService::class.java)
        val pendingIntent = PendingIntent.getService(this, ALARM_REQUEST_CODE_AUTO,
                indent, PendingIntent.FLAG_UPDATE_CURRENT)

        /** アラームを解除する **/
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        setAutoRunning(false)
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



