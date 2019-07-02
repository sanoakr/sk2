package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_DENIED_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_REQUEST_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.REQUEST_PERMISSIONS_REQUEST_CODE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_OK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SETTINGS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_ATTEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk25.coroutines.onClick
import android.content.*
import android.os.Vibrator
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.Gravity
import android.view.View
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_VERSION_CODE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_VERSION_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_MARGIN_ATTEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_MARGIN_MENU
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_SIZE_ATTEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_SIZE_MENU
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_ATTEND_FALSE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_ATTEND_TRUE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_EXIT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_HELP
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_LOG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_CANCEL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_MSG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_OK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.MAX_COUNT_NOBEACON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NAME_DEMOUSER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NAME_START_TESTUSER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_USER_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_ALARM
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_INFO_NOT_FOUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_PERIOD_IN_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SWITCH_TEXT_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_Large
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_TINY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_MAIN_AUTO_OFF
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_MAIN_AUTO_ON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.apNameMap
import me.mattak.moment.Moment
import org.jetbrains.anko.sdk27.coroutines.onClick

/** ////////////////////////////////////////////////////////////////////////////// **/
/** Sk2 Main Activity **/
class MainActivity : FragmentActivity(), SharedPreferences.OnSharedPreferenceChangeListener, AnkoLogger {
    companion object {
        lateinit var sk2: Sk2Globals
        lateinit var pref: SharedPreferences

        var countNoBeacon = 0
    }

    private var mainUi = MainActivityUi()

    /*** BLE Scanner ***/
    private lateinit var mScanner: BluetoothLeScannerCompat
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilters: ArrayList<ScanFilter>
    /** Scan result Broadcast Reciever **/
    private var scanReceiver: ScanReceiver? = null
    /** バイブレータ **/
    private var vibrator: Vibrator? = null

    /** ////////////////////////////////////////////////////////////////////////////// **/
    /********************************************************************/
    /** /////////////////////////////////////// **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sk2 = this.application as Sk2Globals
        pref = Sk2Globals.pref

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = COLOR_BACKGROUND
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        }
        mainUi.setContentView(this)

        /** (ゆるい)位置情報へのパーミッションをリクエスト **/
        if (!checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        /** Doze mode での実行許可を要求 (> 23M) **/
        requestDozeIgnore()

        /** BLE のチェック **/
        if (!sk2.checkBt() && pref.getString(PREF_UID, "") != NAME_DEMOUSER) {
            // ダメならボタンをグレーアウト
            mainUi.attBt.background = ContextCompat.getDrawable(this, R.drawable.button_states_disabled)
            mainUi.attBt.text = BUTTON_TEXT_ATTEND_FALSE
            toast(Sk2Globals.TOAST_CHECK_BLE_OFF)
        } else {
            mainUi.attBt.background = ContextCompat.getDrawable(this, R.drawable.button_states_blue)
            mainUi.attBt.text = BUTTON_TEXT_ATTEND_TRUE
        }

        /** SharedPreference の変更を通知するリスナー **/
        pref.registerOnSharedPreferenceChangeListener(this)

        /** Broadcast Reciever from ScanService**/
        scanReceiver = ScanReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(scanReceiver!!, IntentFilter(ACTION_BROADCAST))

        /** Create & Start BLE Scanner if DEBUG **/
        if (pref.getBoolean(PREF_DEBUG, false)) {
            mScanner = BluetoothLeScannerCompat.getScanner()
            scanSettings = ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(SCAN_PERIOD_IN_MILLISEC)
                    .setUseHardwareBatchingIfSupported(false)
                    .build()
            scanFilters = ArrayList() // ArrayList<ScanFilter>() // フィルタは空 == 全て受け取る
        }
        /** バイブレーション **/
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        /** Scanning フラグをリセット **/
        sk2.setScanRunning(false)
        //mScanner.startScan(scanFilters, scanSettings, mScanCallback)

        /** Local Queue をリストア **/
        sk2.restoreQueue()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onResume() {
        super.onResume()
        /** ユーザ情報を確認、デバッグモード設定、空なら Login へ **/
        if (!checkInfo(mainUi))
            startActivity(intentFor<LoginActivity>().clearTop())

        /** 自動モードの設定 **/
        val auto = sk2.getAutoRunning()
        mainUi.autoSw.isChecked = auto
        /** Alarm Service **/
        if (auto) sk2.setAlarmService() else sk2.stopAlarmService()
        /** BLE Scan 開始 **/
        if (sk2.checkBt()) {
            try {
                mScanner.startScan(scanFilters, scanSettings, mScanCallback)
            } catch (e: Exception) {
                warn("BLE Scanner has been already started")
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onDestroy() {
        mScanner.stopScan(mScanCallback)
        // ローカルキューを保存
        sk2.saveQueue()

        super.onDestroy()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** Disable Back Key **/
    override fun onBackPressed() {}
    /** ////////////////////////////////////////////////////////////////////////////// **/
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
            ui.userInfo.text = if (uid == NAME_DEMOUSER)
                "Demo User / $name"
            else
                "$uid / $name"

            // 前方一致でテストユーザー(デバッグモード)とする
            if (Regex("^$NAME_START_TESTUSER").containsMatchIn(uid)
                    && uid != NAME_DEMOUSER) {
                pref.edit()
                        .putBoolean(PREF_DEBUG, true)
                        .apply()
            }
            true // O.K.
        }
    }
    /** /////////////////////////////////////// **/
    /** BLE スキャナのコールバック **/
    private val mScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onReceivedBleScan(listOf(result))
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            onReceivedBleScan(results)
        }
        override fun onScanFailed(errorCode: Int) {
            warn("ScanFailed: $errorCode")
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** BLE スキャン受診時のコールバック ***/
    private fun onReceivedBleScan(results: List<ScanResult>) {
        // ビーコン情報をパースして ScanArray() に保存
        val scanArray = ScanArray(results)

        /*** 送信可能時刻をチェック ***/
        val curDatetime = Moment() // 現在時刻
        // debug モードでなく、’..TIME_FROM'時から’..TIME_TO'時の間でない
        val invalidTime = ( (! pref.getBoolean(PREF_DEBUG, false))
                && (curDatetime.hour < Sk2Globals.PERMISSION_SENDING_TIME_FROM
                        || curDatetime.hour > Sk2Globals.PERMISSION_SENDING_TIME_TO))

        // ビーコンチェックしてスキャン情報とカウンタを更新
        if (scanArray.count() > 0) {
            mainUi.scanInfo.text = scanArray.getBeaconsText(signal = true, map = apNameMap)
            countNoBeacon = 0
        } else {
            mainUi.scanInfo.text = SCAN_INFO_NOT_FOUND
            countNoBeacon++
        }
        // ビーコンなしが続いたらボタン表示をオフ
        if (invalidTime || countNoBeacon > MAX_COUNT_NOBEACON) {
            mainUi.attBt.background = ContextCompat.getDrawable(this, R.drawable.button_states_disabled)
            mainUi.attBt.text = BUTTON_TEXT_ATTEND_FALSE
        }
        else {
            mainUi.attBt.background = ContextCompat.getDrawable(this, R.drawable.button_states_blue)
            mainUi.attBt.text = BUTTON_TEXT_ATTEND_TRUE
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /**  ScanServer を起動する: send はサーバ送信 or スキャンのみ **/
    fun toggleService(send: Boolean = true) {

        // BLE Scan が実行中でない
        if (! sk2.getScanRunning()) {
            val intent = Intent(this, ScanService::class.java)
            intent.putExtra(SCANSERVICE_EXTRA_SEND, send)
            intent.putExtra(SCANSERVICE_EXTRA_ALARM, false) // Always false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                this.startForegroundService(intent)
            else
                this.startService(intent)
        } else {
            warn("Scan Running")
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** ScanService からの Broadcast Receiver ***/
    private inner class ScanReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message= intent.getSerializableExtra(EXTRA_TOAST) as String?
            val scanResult = intent.getSerializableExtra(EXTRA_BLESCAN) as String?

            /** message は Toast 表示 **/
            if (! message.isNullOrEmpty())
                toast(message)
            /** scanResult は SendInfo へ表示 **/
            if (pref.getBoolean(PREF_DEBUG, false) && ! scanResult.isNullOrEmpty())
                mainUi.sendInfo.text = scanResult
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** SharedPreferences が変化したときのコールバック ***/
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == PREF_AUTO) // || s == SCAN_RUNNING)
            setAutoSwitchState()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Auto Switch の状態設定 ***/
    private fun setAutoSwitchState() {
        mainUi.autoSw.isChecked = sk2.getAutoRunning()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** バイブレーションを鳴らす ***/
    @Suppress("DEPRECATION")
    fun vibrate() {
        if (vibrator != null) {
            if (vibrator!!.hasVibrator()) vibrator!!.vibrate(Sk2Globals.ATTENDANCE_VIBRATE_MILLISEC)
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** Doze Ignore パーミッションをチェックして許可を要求 ***/
    private fun requestDozeIgnore() {
        // above Android Marshmallow(23) requires a REQUEST_IGNORE_BATTERY_OPTIMIZATIONS Permission
        // for running services under the Doze mode.
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (! pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** パーミッションをチェック ***/
    private fun checkPermissions(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, permission)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** パーミッションをリクエスト ***/
    private fun requestPermissions(permission: String) {
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
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** パーミッションリクエストからの結果を処理 ***/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        info("onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    info("User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                }
                else -> // Permission denied.
                    /** setButtonsState(false) **/
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
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
/** ////////////////////////////////////////////////////////////////////////////// **/
/*** UI構成 via Anko ***/
    class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var userInfo: TextView
    lateinit var scanInfo: TextView
    lateinit var sendInfo: TextView
    lateinit var attBt: Button
    lateinit var autoSw: Switch
    lateinit var versionInfo: TextView

    companion object {
        const val TITLE = 1
        const val USER = 2
        const val AUTO = 3
        const val SCAN = 4
        const val ATTEND = 5
        const val MENU = 6
        const val LOG = 61
        const val HELP = 62
        const val EXIT = 63
        const val VERSION = 99
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        val sk2 = ui.owner.application as Sk2Globals

        relativeLayout {
            backgroundColor = COLOR_BACKGROUND
            ////////////////////////////////////////
            textView("$APP_TITLE $APP_NAME") {
                id = TITLE
                textColor = Color.BLACK
                textSize = TEXT_SIZE_LARGE
                backgroundColor = COLOR_BACKGROUND_TITLE
                topPadding = dip(10); bottomPadding = dip(10)
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams { alignParentTop(); alignParentStart(); alignParentEnd()
                centerHorizontally(); bottomMargin = dip(10)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            userInfo = textView("User Info") {
                id = USER
                textColor = Color.BLACK
                textSize = TEXT_SIZE_Large
            }.lparams { below(TITLE); alignParentStart(); leftMargin = dip(5) }
            ////////////////////////////////////////
            autoSw = switch {
                id = AUTO
                text = SWITCH_TEXT_AUTO
                textSize = TEXT_SIZE_Large
                onClick {
                    if (isChecked) {
                        sk2.setAlarmService()
                        toast(TOAST_MAIN_AUTO_ON)
                    } else {
                        sk2.stopAlarmService()
                        toast(TOAST_MAIN_AUTO_OFF)
                    }
                    sk2.setAutoRunning(isChecked)
                }
            }.lparams { below(TITLE);  alignParentEnd() ; baselineOf(USER)}
            /** ////////////////////////////////////////////////////////////////////////////// **/
            scanInfo = textView {
                id = SCAN
                textColor = Color.BLACK
                textSize = TEXT_SIZE_TINY
            }.lparams {
                below(USER); alignParentStart()
                topMargin = dip(10); leftMargin = dip(10)
            }
            ////////////////////////////////////////
            sendInfo = textView {
                textColor = Color.BLACK
                textSize = TEXT_SIZE_TINY
            }.lparams {
                //below(TOP); alignParentEnd(); margin = dip(8)
            }.lparams {
                below(AUTO); alignParentEnd(); baselineOf(SCAN)
                topMargin = dip(10); rightMargin = dip(10)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            attBt = button(BUTTON_TEXT_ATTEND_TRUE) {
                id = ATTEND
                textColor = Color.WHITE
                textSize = TEXT_SIZE_ATTEND
                background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
                allCaps = false
                onClick {
                    ui.owner.vibrate()
                    ui.owner.toggleService(send=true)
                }
            }.lparams {
                width = dip(BUTTON_SIZE_ATTEND); height = dip(BUTTON_SIZE_ATTEND)
                margin = dip(BUTTON_MARGIN_ATTEND)
                above(MENU); centerInParent()
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            linearLayout {
                id = MENU
                ////////////////////////////////////////
                relativeLayout {
                    imageButton {
                        id = LOG
                        imageResource = R.drawable.ic_action_record
                        background = ContextCompat.getDrawable(context, R.drawable.button_menu)
                        onClick {
                            startActivity<RecordPagerActivity>()
                        }
                    }.lparams {
                        width = dip(BUTTON_SIZE_MENU); height = dip(BUTTON_SIZE_MENU)
                        horizontalMargin = dip(BUTTON_MARGIN_MENU)
                    }
                    textView(BUTTON_TEXT_LOG) {
                        labelFor = LOG
                        textSize = TEXT_SIZE_NORMAL

                    }.lparams { below(LOG); centerHorizontally() }
                }
                ////////////////////////////////////////
                relativeLayout {
                    imageButton {
                        id = HELP
                        imageResource = R.drawable.ic_action_help
                        background = ContextCompat.getDrawable(context, R.drawable.button_menu)
                        onClick {
                            startActivity<HelpActivity>()
                        }
                    }.lparams {
                        width = dip(BUTTON_SIZE_MENU); height = dip(BUTTON_SIZE_MENU)
                       horizontalMargin = dip(BUTTON_MARGIN_MENU)
                    }
                    textView(BUTTON_TEXT_HELP) {
                        labelFor = HELP
                        textSize = TEXT_SIZE_NORMAL

                    }.lparams { below(HELP); centerHorizontally() }
                }
                ////////////////////////////////////////
                relativeLayout {
                    imageButton {
                        id = EXIT
                        imageResource = R.drawable.ic_action_exit
                        background = ContextCompat.getDrawable(context, R.drawable.button_menu)
                        onClick {
                            alert(LOGOUT_DIALOG_MSG, LOGOUT_DIALOG_TITLE) {
                                positiveButton(LOGOUT_DIALOG_OK) { _ -> sk2.readyTologout()
                                    // back to the Login Activity
                                    ui.owner.startActivity(intentFor<LoginActivity>().clearTop())
                                }
                                negativeButton(LOGOUT_DIALOG_CANCEL) { _ -> }
                            }.show()
                        }
                    }.lparams {
                        width = dip(BUTTON_SIZE_MENU); height = dip(BUTTON_SIZE_MENU)
                        horizontalMargin = dip(BUTTON_MARGIN_MENU)
                    }
                    textView(BUTTON_TEXT_EXIT) {
                        labelFor = EXIT
                        textSize = TEXT_SIZE_NORMAL
                    }.lparams { below(EXIT); centerHorizontally() }
                }
            }.lparams {
                above(VERSION); centerHorizontally(); bottomMargin = dip(8)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            versionInfo = textView("Version $APP_VERSION_NAME ($APP_VERSION_CODE)") {
                id = VERSION
                textColor = Color.BLACK
                textSize = TEXT_SIZE_TINY
            }.lparams {
                alignParentBottom(); alignParentEnd(); bottomMargin = dip(4); rightMargin = dip(4)
            }
        }
    }
}