package jp.ac.ryukoku.st.sk2

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.*
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ATTENDANCE_VIBRATE_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_DENIED_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_REQUEST_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NAME_START_TESTUSER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_USER_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.REQUEST_PERMISSIONS_REQUEST_CODE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_RUNNING
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_OK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SETTINGS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_ATTEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_CHECK_BLE_OFF
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS



////////////////////////////////////////////////////////////////////////////////
/*** メイン画面: これがアクティブな間は ScanService は通常の Background、ここから外れると Foreground ***/
// Oreo 以前からの移行のためにこうなったが、ずっと Foreground のままで切り替える必要ない？
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, AnkoLogger {

    private var mainUi = MainActivityUi()

    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    // Broadcast Reciever
    private var myReceiver: MyReceiver? = null
    // ScanService のリファレンス
    var mService: ScanService? = null
    // ScanService へのバインド状態
    private var mBound = false
    // バイブレータ
    private var vibrator: Vibrator? = null

    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ScanService.LocalBinder
            mService = binder.service
            mService?.requestScanUpdates() // BLE scan is always started
            mBound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myReceiver = MyReceiver()  // Broadcast レシーバ

        sk2 = this.application as Sk2Globals
        pref = sk2.pref

        title = "$APP_TITLE $APP_NAME"
        mainUi.setContentView(this)

        // (ゆるい)位置情報へのパーミッションをリクエスト
        if (!checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        // Doze mode での実行許可を要求 (> 23M)
        requestDozeIgnore()
    }
    ////////////////////////////////////////
    override fun onStart() {
        super.onStart()
        // SharedPreference の変更を通知するリスナー
        pref.registerOnSharedPreferenceChangeListener(this)

        // ScanService へバインド with AUTO_CREATE
        bindService(Intent(this, ScanService::class.java), mServiceConnection,
                Context.BIND_AUTO_CREATE)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        // ScanService からのブロードキャストレシーバ
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver!!,
                IntentFilter(ACTION_BROADCAST))

        // ユーザ情報を確認、デバッグモード設定、空なら Login へ
        if (!checkInfo(mainUi))
            startActivity(intentFor<LoginActivity>().clearTop())

        // BLE のチェック
        if (!sk2.checkBt()) {
            // ダメならボタンをグレーアウト
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
            toast(TOAST_CHECK_BLE_OFF)
        } else {
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
        }

        // デバッグモードの表示設定
        if (pref.getBoolean(PREF_DEBUG, false)) {
            //mainUi.startBt.visibility = View.VISIBLE
            //mainUi.stopBt.visibility = View.VISIBLE
            mainUi.scanInfo.visibility = View.VISIBLE
        } else {
            //mainUi.startBt.visibility = View.INVISIBLE
            //mainUi.stopBt.visibility = View.INVISIBLE
            mainUi.scanInfo.visibility = View.INVISIBLE
        }

        // 自動モードの設定
        val auto = pref.getBoolean(PREF_AUTO, false)
        mainUi.autoSw.isChecked = auto
        if (auto)
            mService?.startInterval(pref.getBoolean(PREF_DEBUG, false))

        // バイブレーション
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // ScanService ON/OFF ボタンの状態設定
        setButtonsState(sk2.getScanRunning())
    }
    ////////////////////////////////////////
    override fun onPause() {
        // Broadcast Reciever を停止
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver!!)
        super.onPause()
    }
    ////////////////////////////////////////
    override fun onStop() {
        // ScanService の Bind を外す
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        // SharedPreferences のチェンジリスナーを止める
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        //mService?.stopInterval()
        // サービスを停止
        mService?.removeScanUpdates()
        // ローカルキューを保存
        sk2.saveQueue()
        super.onDestroy()
    }
    ////////////////////////////////////////
    /*** ユーザー情報のチェック ***/
    private fun checkInfo(ui: MainActivityUi): Boolean {
        val uid = pref.getString(PREF_UID, "")
        val name = pref.getString(PREF_USER_NAME, "")
        /***
        val time = sk2.userMap.getOrDefault("name", 0L)
        // check key life
        val now = System.currentTimeMillis()
        val over = (now - time) > lifetime
        ***/

        return if (uid == "") { // 空ならダメ
            false
        } else {
            // 画面上部のユーザ情報を設定
            val utext = " $uid / $name"
            ui.userInfo.text = utext

            // 前方一致でテストユーザー(デバッグモード)とする
            if (Regex("^$NAME_START_TESTUSER").containsMatchIn(uid)) {
                pref.edit()
                        .putBoolean(PREF_DEBUG, true)
                        .apply()
            }
            true // O.K.
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    /*** Doze パーミッションをチェック ***/
    fun requestDozeIgnore() {
        // above Android Marshmallow(23) requires a REQUEST_IGNORE_BATTERY_OPTIMIZATIONS Permission
        // for running services under the Doze mode.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = this.getSystemService(PowerManager::class.java)
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                val intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    /*** パーミッションをチェック ***/
    fun checkPermissions(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, permission)
    }
    ////////////////////////////////////////
    /*** パーミッションをリクエスト ***/
    fun requestPermissions(permission: String) {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

        if (shouldProvideRationale) {
            Snackbar.make(
                    this.contentView!!,
                    LOCATION_PERMISSION_REQUEST_MESSAGE,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(TEXT_OK) {
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(permission), REQUEST_PERMISSIONS_REQUEST_CODE)
                    }.show()
        } else {
            info("Requesting permission: $permission")
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(permission), REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }
    ////////////////////////////////////////
    /*** パーミッションリクエストからの結果を処理 ***/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        info("onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                info("User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService!!.requestScanUpdates()
            } else {
                // Permission denied.
                setButtonsState(false)
                Snackbar.make(
                        this.contentView!!,
                        LOCATION_PERMISSION_DENIED_MESSAGE,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(TEXT_SETTINGS) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }.show()
            }
        }
    }
    ////////////////////////////////////////
    /*** Type を引数とした出席記録用： ScanService.sendInfoToServer() を Bind 経由で叩く ***/
    fun attendance(type: Char) {
        mService?.sendInfoToServer(type)
    }
    ////////////////////////////////////////
    /*** ScanService からの Broadcast Receiver ***/
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message= intent.getSerializableExtra(EXTRA_TOAST) as String?
            val lastscan = intent.getSerializableExtra(EXTRA_BLESCAN) as String?

            // Toast 表示
            if (! message.isNullOrEmpty())
                toast(message!!)
            // デバッグモードのときは(記録される)最新スキャンを表示
            if (pref.getBoolean(PREF_DEBUG, false) && ! lastscan.isNullOrEmpty())
                mainUi.scanInfo.text = lastscan
        }
    }
    ////////////////////////////////////////
    /*** SharedPreferences が変化したときのコールバック ***/
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s == SCAN_RUNNING) {
            setButtonsState(sk2.getScanRunning())
        }
    }
    ////////////////////////////////////////
    /*** スキャンON/OFFボタンの状態設定（デバック時のみ表示） ***/
    private fun setButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            mainUi.startBt.isEnabled = false
            mainUi.stopBt.isEnabled = true
        } else {
            mainUi.startBt.isEnabled = true
            mainUi.stopBt.isEnabled = false
        }
    }
    ////////////////////////////////////////
    /*** バイブレーションを鳴らす ***/
    @Suppress("DEPRECATION")
    fun vibrate() {
        if (vibrator != null) {
            if (vibrator!!.hasVibrator()) vibrator!!.vibrate(ATTENDANCE_VIBRATE_MILLISEC)
        }
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
////////////////////////////////////////////////////////////////////////////////
/*** UI構成 via Anko ***/
class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var userInfo: TextView
    lateinit var scanInfo: TextView
    lateinit var startBt: Button
    lateinit var stopBt: Button
    lateinit var attBt: Button
    lateinit var autoSw: Switch

    companion object {
        val USER = 1; val AUTO = 2; val ATTEND = 3; val MENU = 4;
        val UPDATE = 91;  val SEARCH = 92

        const val BUTTON_TEXT_AUTO = "Auto"
        const val TOAST_MAIN_AUTO_ON = "Auto ON"
        const val TOAST_MAIN_AUTO_OFF = "Auto OFF"
    }
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        val sk2 = ui.owner.application as Sk2Globals
        val pref = sk2.pref

        relativeLayout {
            padding = dip(4)
            ////////////////////////////////////////
            userInfo = textView("User Info") {
                id = USER
                textColor = Color.BLACK
                textSize = TEXT_SIZE_LARGE
            }.lparams {
                alignParentTop(); alignParentStart()
            }
            ////////////////////////////////////////
            autoSw = switch {
                id = AUTO
                text = BUTTON_TEXT_AUTO
                textSize = TEXT_SIZE_LARGE
                onClick {
                    if (isChecked) {
                        ui.owner.mService!!.startInterval(pref.getBoolean(PREF_DEBUG, false))
                        toast(TOAST_MAIN_AUTO_ON)
                    } else {
                        ui.owner.mService!!.stopInterval()
                        toast(TOAST_MAIN_AUTO_OFF)
                    }
                    pref.edit()
                            .putBoolean(PREF_AUTO, isChecked)
                            .apply()
                }
            }.lparams {
                alignParentTop(); alignParentEnd()
            }
            ////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////
            scanInfo = textView("Scan Info") {
                textColor = Color.BLACK
                textSize = TEXT_SIZE_NORMAL
            }.lparams {
                below(USER); alignParentStart(); margin = dip(8)
            }
            ////////////////////////////////////////
            attBt = button("出席") {
                id = ATTEND
                textColor = Color.WHITE
                textSize = TEXT_SIZE_ATTEND
                background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
                allCaps = false
                onClick {
                    ui.owner.vibrate()
                    ui.owner.attendance('M')
                }
            }.lparams {
                width = dip(200); height = dip(200)//; margin = dip(50);
                /*below(UPDATE);*/ above(MENU); centerHorizontally(); centerVertically()
            }
            ////////////////////////////////////////////////////////////////////////////////
            linearLayout {
                id = MENU
                ////////////////////////////////////////
                imageButton {
                    imageResource = R.drawable.ic_history_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<RecordActivity>()
                    }
                }.lparams { width = dip(48); height = dip(48); margin = dip(8) }
                ////////////////////////////////////////
                imageButton {
                    imageResource = R.drawable.ic_live_help_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<HelpActivity>()
                    }
                }.lparams { width = dip(48); height = dip(48); margin = dip(8) }
                ////////////////////////////////////////
                imageButton {
                    imageResource = R.drawable.ic_logout_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        sk2.logout()
                    }
                }.lparams { width = dip(48); height = dip(48); margin = dip(8) }
            }.lparams {
                above(UPDATE); centerHorizontally()
            }
            ////////////////////////////////////////////////////////////////////////////////
            linearLayout {
                id = UPDATE
                ////////////////////////////////////////
                startBt = button("Start Scan Update") {
                    textSize = TEXT_SIZE_NORMAL
                    onClick {
                        if (!ui.owner.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ui.owner.requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else {
                            ui.owner.mService?.requestScanUpdates()
                        }
                    }
                }.lparams { weight = 1f }
                ////////////////////////////////////////
                stopBt = button("Stop Scan Update") {
                    textSize = TEXT_SIZE_NORMAL
                    onClick {
                        ui.owner.mService?.removeScanUpdates()
                    }
                }.lparams { weight = 1f }
            }.lparams {
                alignParentBottom(); centerHorizontally(); width = matchParent
            }
        }
    }
}
