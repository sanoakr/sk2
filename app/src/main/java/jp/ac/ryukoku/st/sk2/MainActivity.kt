package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.content.Intent

////////////////////////////////////////////////////////////////////////////////
class MainActivity : AppCompatActivity(), AnkoLogger {
    private val PERMISSIONS_REQUEST_COARSE_LOCATION = 456
    private var mainUi = MainActivityUi()

    ////////////////////////////////////////
    var scanService: ScanService? = null
    private var isBound = false

    private val scanConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ScanService.ScanBinder
            scanService = binder.inService
            isBound = true
        }
        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
        }
    }
    ////////////////////////////////////////
    //private val receiver = UpdateReceiver()
    //private val filter = IntentFilter()

    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "${sk2.app_title} ${sk2.app_name}"
        mainUi.setContentView(this)

        ////////////////////////////////////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permission, PERMISSIONS_REQUEST_COARSE_LOCATION)
        }
        if (! isServiceWorking(ScanService::class.java)) {
            startService<ScanService>()
            Thread.sleep(100)
        }
        //bindScanService()
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        if (! checkInfo(mainUi) ) { startActivity(intentFor<LoginActivity>().clearTop()) }

        val sk2 = this.application as Sk2Globals
        mainUi.scanInfo.visibility = if ((sk2.prefMap["debug"] ?: false) as Boolean)
            View.VISIBLE else View.INVISIBLE

        if (!checkBLE()) {
            toast("Bloothoothオンにしてください")
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
        } else {
            //mainUi.attToastText = "出席！！"
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
            //mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_red)
            ////////////////////////////////////////
            if ((sk2.prefMap["auto"] ?: false) as Boolean) {
                val scanitv = ((sk2.prefMap["autoitv"] ?: sk2._autoitv) as Int).toLong()
                scanService?.startInterval(scanitv)
                mainUi.attBtn.text = "AUTO"
            } else {
                scanService?.stopInterval()
                mainUi.attBtn.text = "出席"
            }
            ////////////////////////////////////////
            //bindScanService()
        }
    }
    ////////////////////////////////////////
    override fun onBackPressed() { /* DO NOTHING */ }
    ////////////////////////////////////////
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    info("ACCESS_COARSE_LOCATION Permitted.")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("機能の制限")
                    builder.setMessage("位置情報へのアクセスが許可されるまでBLEビーコンへのアクセスは制限されます。")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }
    ////////////////////////////////////////
    //private fun logout() {
    //    (this.application as Sk2Globals).logout(scanConnection)
    //}
    ////////////////////////////////////////
    private fun checkInfo(ui: MainActivityUi): Boolean {
        val sk2 = this.application as Sk2Globals
        val uid = (sk2.userMap["uid"] ?: "") as String
        val gcos = (sk2.userMap["gcos"] ?: "") as String
        val name = (sk2.userMap["name"] ?: "") as String

        //val time = sk2.userMap.getOrDefault("name", 0L)
        // check key life
        //val now = System.currentTimeMillis()
        //val over = (now - time) > lifetime

        if ( uid == "") {
            return false
        } else {
            val utext = " $uid / $gcos / $name"
            ui.userInfo.text = utext
            return true
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    fun sendInfo(marker: Char) {
        val sk2 = this.application as Sk2Globals

        if (checkBLE()) {
            //while (! isBound) {
                bindScanService()
            //    Thread.sleep(100) }
            try {
                val info = scanService?.sendInfo(marker)

                if ((sk2.prefMap["debug"] ?: false) as Boolean) {
                    if (info.isNullOrBlank()) {
                        mainUi.scanInfo.text = "Couldn't find Beacons"
                        toast("ビーコンが見つかりません。")
                    } else {
                        mainUi.scanInfo.text = info
                        toast("出席！！")
                    }
                }
            } catch (e: RemoteException) {
                toast("サービスに接続できません")
                e.printStackTrace()
            }
        }
        //unbindScanService()
    }
    ////////////////////////////////////////
    private fun bindScanService() {
        if (! isBound && isServiceWorking(ScanService::class.java)) {
            val scanIntent = Intent(ctx, ScanService::class.java)
            bindService(scanIntent, scanConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }
    ////////////////////////////////////////
    private fun unbindScanService() {
        if (isBound && isServiceWorking(ScanService::class.java)) {
            unbindService(scanConnection)
            isBound = false
        }
    }
    ////////////////////////////////////////
    private fun isServiceWorking(clazz: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { clazz.name == it.service.className }
    }
    ////////////////////////////////////////////////////////////////////////////////
    /*
    private inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent?.extras
            val msg = extras?.getString("btmessage")
            toast("ビーコン記録を送信します")
            sendInfo('B')
        }
    }*/
    ////////////////////////////////////////
    override fun onPause() {
        super.onPause()
        scanService?.stopInterval()
        unbindScanService()
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        val sk2 = this.application as Sk2Globals
        sk2.prefMap["auto"] = false
        sk2.savePrefData()
        unbindScanService()
        stopService<ScanService>()
        //BluetoothAdapter.getDefaultAdapter().disable()
    }
    ////////////////////////////////////////
    private fun hasBLE(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    ////////////////////////////////////////
    private fun checkBLE(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if ( btAdapter == null || !hasBLE() ) {
            toast("このデバイスのBLEアダプタが見つかりません")
            return false
        } else if (! btAdapter.isEnabled) {
            toast("Bluetoothをオンにしてください")
            val REQUEST_ENABLE_BT = 1
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return false
        }
        return true
    }
}
////////////////////////////////////////////////////////////////////////////////
class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var attBtn: Button
    lateinit var scanInfo: TextView
    lateinit var userInfo: TextView
    //var attToastText = "出席！！"

    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        relativeLayout {
            ////////////////////////////////////////
            userInfo = textView("") {
                textColor = Color.BLACK
                textSize = 18f
            }.lparams {
                alignParentTop(); centerHorizontally()
                topMargin = dip(5)
            }
            ////////////////////////////////////////
            attBtn = button("出席") {
                textColor = Color.WHITE
                textSize = 36f
                background = ContextCompat.getDrawable(ctx, R.drawable.button_states_red)
                allCaps = false
                onClick {
                    //toast(attToastText)
                    ui.owner.sendInfo('M')
                }
            }.lparams {
                centerHorizontally(); centerVertically()
                topMargin = dip(50); width = dip(250); height = dip(250)
            }
            ////////////////////////////////////////
            verticalLayout {
                ////////////////////////////////////////
                scanInfo = textView("") {
                    textSize = 12f
                }.lparams {
                    bottomMargin = dip(5); padding = dip(5)
                    width = matchParent; gravity = Gravity.RIGHT
                }
                ////////////////////////////////////////
                linearLayout {
                    ////////////////////////////////////////
                    imageButton {
                        imageResource = R.drawable.ic_settings_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<PreferenceActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(8); width = dip(64); height = dip(64)
                    }
                    ////////////////////////////////////////
                    imageButton {
                        imageResource = R.drawable.ic_history_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<RecordActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(8); width = dip(64); height = dip(64)
                    }
                    ////////////////////////////////////////
                    imageButton {
                        imageResource = R.drawable.ic_history_local_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<LocalRecordActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(8); width = dip(64); height = dip(64)
                    }
                    ////////////////////////////////////////
                    imageButton {
                        imageResource = R.drawable.ic_live_help_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<HelpActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(8); width = dip(64); height = dip(64)
                    }

                }.lparams { width = wrapContent; gravity = Gravity.CENTER_HORIZONTAL }
            }.lparams {
                width = matchParent; alignParentBottom(); centerHorizontally()
            }
        }
    }
}
