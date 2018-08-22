package jp.ac.ryukoku.st.sk2

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.*
import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import me.mattak.moment.Moment
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, AnkoLogger {
    ////////////////////////////////////////
    companion object {
        const val MAX_SEND_BEACON_NUM: Int = 10


        const val ATTENDANCE_VIBRATE_MILLISEC: Long = 1        // vibration time

        private val TAG = MainActivity::class.java.simpleName
        // Used in checking for runtime permissions.
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
    ////////////////////////////////////////
    private var mainUi = MainActivityUi()

    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    private var myReceiver: MyReceiver? = null   // The BroadcastReceiver
    var mService: ScanService? = null // Reference to the service
    private var mBound = false                   // Bound state of the service

    private var vibrator: Vibrator? = null

    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ScanService.LocalBinder
            mService = binder.service
            mService?.requestScanUpdates() // always started
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
        myReceiver = MyReceiver()

        sk2 = this.application as Sk2Globals
        pref = sk2.pref

        title = "${sk2.app_title} ${sk2.app_name}"
        mainUi.setContentView(this)

        //if (sk2.getScanRunning()) {
            if (!checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        //}
    }
    ////////////////////////////////////////
    override fun onStart() {
        super.onStart()
        pref.registerOnSharedPreferenceChangeListener(this)

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, ScanService::class.java), mServiceConnection,
                Context.BIND_AUTO_CREATE)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver!!,
                IntentFilter(ScanService.ACTION_BROADCAST))

        // go back to LoginActivity if with invalid user
        if (!checkInfo(mainUi))
            startActivity(intentFor<LoginActivity>().clearTop())

        if (!checkBt()) {
            toast("Bloothoothオンにしてください")
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
        } else {
            mainUi.attBt.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
        }

        if (pref.getBoolean("debug", false)) {
            mainUi.startBt.visibility = View.VISIBLE
            mainUi.stopBt.visibility = View.VISIBLE
            mainUi.scanInfo.visibility = View.VISIBLE
        } else {
            mainUi.startBt.visibility = View.INVISIBLE
            mainUi.stopBt.visibility = View.INVISIBLE
            mainUi.scanInfo.visibility = View.INVISIBLE
        }

        // restore auto setting
        val auto = pref.getBoolean("auto", false)
        mainUi.autoSw.isChecked = auto
        if (auto) mService?.startInterval(pref.getBoolean("debug", false))

        // Vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setButtonsState(sk2.getScanRunning())
    }
    ////////////////////////////////////////
    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver!!)
        super.onPause()
    }
    ////////////////////////////////////////
    override fun onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        //mService?.stopInterval()
        mService?.removeScanUpdates()
        sk2.saveQueue()  // save localQueue to sharedPreference
        super.onDestroy()
    }
    ////////////////////////////////////////
    private fun checkInfo(ui: MainActivityUi): Boolean {
        val uid = pref.getString("uid", "")
        val name = pref.getString("name", "")

        //val time = sk2.userMap.getOrDefault("name", 0L)
        // check key life
        //val now = System.currentTimeMillis()
        //val over = (now - time) > lifetime

        if (uid == "") {
            return false
        } else {
            val utext = " $uid / $name "
            ui.userInfo.text = utext
            if (Regex("^${sk2.testuser}").containsMatchIn(uid)) {
                pref.edit()
                        .putBoolean("debug", true)
                        .apply()
            }
            return true
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
    fun checkPermissions(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, permission)
    }
    ////////////////////////////////////////
    fun requestPermissions(permission: String) {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

        if (shouldProvideRationale) {
            Snackbar.make(
                    //findViewById(R.id.activity_main),
                    this.contentView!!,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
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
                        //findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
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
    fun attendance(type: Char) {
        val lastArray = sk2.lastScan

        // show scan info if in debug mode
        if (pref.getBoolean("debug", false)) {
            mainUi.scanInfo.text = lastArray.getBeaconsText()
        }
        mService?.sendInfoToServer(type)
    }
    ////////////////////////////////////////
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getSerializableExtra(ScanService.EXTRA_BLESCAN) as String
            toast(message)
        }
    }
    ////////////////////////////////////////
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s == sk2.SCAN_RUNNING) {
            setButtonsState(sk2.getScanRunning())
        }
    }
    ////////////////////////////////////////
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
@Suppress("DEPRECATION")
    fun vibrate() {
        if (vibrator != null) {
            if (vibrator!!.hasVibrator()) vibrator!!.vibrate(ATTENDANCE_VIBRATE_MILLISEC)
        }
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


@Suppress("EXPERIMENTAL_FEATURE_WARNING")
////////////////////////////////////////////////////////////////////////////////
class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var userInfo: TextView
    lateinit var scanInfo: TextView
    lateinit var startBt: Button
    lateinit var stopBt: Button
    lateinit var attBt: Button
    lateinit var autoSw: Switch
    val USER = 1; val AUTO = 2; val ATTEND = 3; val MENU = 4;
    val UPDATE = 91;  val SEARCH = 92
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        val sk2 = ui.owner.application as Sk2Globals
        val pref = sk2.pref

        relativeLayout {
            padding = dip(8)
            ////////////////////////////////////////
            userInfo = textView("User Info") {
                id = USER
                textColor = Color.BLACK
                textSize = 14f
            }.lparams {
                alignParentTop(); alignParentStart()
            }
            ////////////////////////////////////////
            autoSw = switch {
                id = AUTO
                text = "Auto"
                textSize = 14f
                onClick {
                    if (isChecked) {
                        ui.owner.mService!!.startInterval(pref.getBoolean("debug", false))
                        toast("Auto ON")
                    } else {
                        ui.owner.mService!!.stopInterval()
                        toast("Auto OFF")
                    }
                    pref.edit()
                            .putBoolean("auto", isChecked)
                            .apply()
                }
            }.lparams {
                alignParentTop(); alignParentEnd()
            }
            ////////////////////////////////////////////////////////////////////////////////
            linearLayout {
                id = UPDATE
                ////////////////////////////////////////
                startBt = button("Start Scan Update") {
                    textSize = 10f
                    onClick {
                        if (!ui.owner.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ui.owner.requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else {
                            ui.owner.mService?.requestScanUpdates()
                        }
                    }
                }
                ////////////////////////////////////////
                stopBt = button("Stop Scan Update") {
                    textSize = 10f
                    onClick {
                        ui.owner.mService?.removeScanUpdates()
                    }
                }
            }.lparams {
                below(USER); centerHorizontally()
            }
            ////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////
            scanInfo = textView("Scan Info") {
                textColor = Color.BLACK
                textSize = 10f
            }.lparams {
                below(UPDATE); alignParentStart()
            }
            ////////////////////////////////////////
            attBt = button("出席") {
                id = ATTEND
                textColor = Color.WHITE
                textSize = 36f
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
                alignParentBottom(); centerHorizontally()
            }
        }
    }
}
