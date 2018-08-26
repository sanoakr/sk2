package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_DENIED_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOCATION_PERMISSION_REQUEST_MESSAGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.REQUEST_PERMISSIONS_REQUEST_CODE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_OK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SETTINGS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_ATTEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.VALID_IBEACON_UUID
import me.mattak.moment.Moment
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.content.*
import android.os.Vibrator
import android.support.v4.content.LocalBroadcastManager
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.ACTION_BROADCAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_BLESCAN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.EXTRA_TOAST
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NAME_START_TESTUSER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_DEBUG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_USER_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_ALARM
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_AUTO
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCANSERVICE_EXTRA_SEND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SCAN_RUNNING


/** ////////////////////////////////////////////////////////////////////////////// **/
/** Sk2 Main Activity **/
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, AnkoLogger {
    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences
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

        title = "$APP_TITLE $APP_NAME"
        mainUi.setContentView(this)

        /** (ゆるい)位置情報へのパーミッションをリクエスト **/
        if (!checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        /** Doze mode での実行許可を要求 (> 23M) **/
        requestDozeIgnore()

        /** BLE のチェック **/
        if (!sk2.checkBt()) {
            // ダメならボタンをグレーアウト
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
            toast(Sk2Globals.TOAST_CHECK_BLE_OFF)
        } else {
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
        }

        /** SharedPreference の変更を通知するリスナー **/
        pref.registerOnSharedPreferenceChangeListener(this)

        /** Scan result Broadcast Reciever **/
        scanReceiver = ScanReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(scanReceiver!!, IntentFilter(ACTION_BROADCAST))

        /** Create & Start BLE Scanner **/
        mScanner = BluetoothLeScannerCompat.getScanner()
        scanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .setUseHardwareBatchingIfSupported(false)
                .build()

        val builder = ScanFilter.Builder()
        // Apple Manufacture ID 0x004c
        builder.setManufacturerData(0x004c, byteArrayOf())
        scanFilters = arrayListOf( builder.build() )
        // フィルターが空だとバックグラウンドで止まる?
        //scanFilters = ArrayList() // ArrayList<ScanFilter>() // フィルタは空 == 全て受け取る

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
        val auto = pref.getBoolean(PREF_AUTO, false)
        mainUi.autoSw.isChecked = auto

        if (auto)
            toggleService(auto=true, send=false)
        else
            toggleService(auto=false, send=false)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onDestroy() {
        mScanner.stopScan(mScanCallback)
        // ローカルキューを保存
        sk2.saveQueue()

        super.onDestroy()
    }
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
        val scanArray = ScanArray()
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
                    info("${s.uuid}, ${s.major}, ${s.minor}, ${dist}")
                }
            }
        }
        mainUi.scanInfo.text = scanArray.getBeaconsText(signal = true, ios = true)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /**  ScanServer を起動する: send はサーバ送信 or スキャンのみ、 auto は自動送信ON or OFF **/
    fun toggleService(send: Boolean = true, auto: Boolean = true) {

        // BLE Scan が実行中でない
        if (! sk2.getScanRunning()) {
            val intent = Intent(this, ScanService::class.java)
            intent.putExtra(SCANSERVICE_EXTRA_SEND, send)
            intent.putExtra(SCANSERVICE_EXTRA_AUTO, auto)
            intent.putExtra(SCANSERVICE_EXTRA_ALARM, false)

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
                toast(message!!)
            /** scanResult は Queue 登録 **/
            // デバッグモードのときは(記録される)最新スキャンを表示
            if (pref.getBoolean(PREF_DEBUG, false) && ! scanResult.isNullOrEmpty())
                mainUi.scanInfo.text = scanResult
        }
    }

    /** ////////////////////////////////////////////////////////////////////////////// **/
    /*** SharedPreferences が変化したときのコールバック ***/
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == PREF_AUTO || s == SCAN_RUNNING)
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
    fun requestDozeIgnore() {
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
    fun checkPermissions(permission: String): Boolean {
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
            if (grantResults.size <= 0) {
                info("User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                /** startService(false) **/
            } else {
                // Permission denied.
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
    lateinit var attBt: Button
    lateinit var autoSw: Switch

    companion object {
        const val USER = 1
        const val AUTO = 2
        const val ATTEND = 3
        const val MENU = 4

        const val BUTTON_TEXT_AUTO = "Auto"
        const val TOAST_MAIN_AUTO_ON = "Auto ON"
        const val TOAST_MAIN_AUTO_OFF = "Auto OFF"
    }

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        val sk2 = ui.owner.application as Sk2Globals
        val pref = Sk2Globals.pref

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
                        /**ui.owner.mService!!.startInterval(pref.getBoolean(PREF_DEBUG, false))**/
                        ui.owner.toggleService(auto = true, send = false)
                        toast(TOAST_MAIN_AUTO_ON)
                    } else {
                        /**ui.owner.mService!!.stopInterval()**/
                        ui.owner.toggleService(auto = false, send = false)
                        toast(TOAST_MAIN_AUTO_OFF)
                    }
                    pref.edit()
                            .putBoolean(PREF_AUTO, isChecked)
                            .apply()
                }
            }.lparams {
                alignParentTop(); alignParentEnd()
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
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
                    ui.owner.toggleService(send = true, auto = false)
                }
            }.lparams {
                width = dip(200); height = dip(200); margin = dip(30)
                /*below(UPDATE);*/ above(MENU); centerHorizontally(); centerVertically()
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            linearLayout {
                id = MENU
                ////////////////////////////////////////
                imageButton {
                    imageResource = R.drawable.ic_history_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<RecordPagerActivity>()
                        /** startActivity<RecordActivity>()**/
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
                /*above(UPDATE);*/ alignParentBottom(); centerHorizontally()
            }
        }
    }
}