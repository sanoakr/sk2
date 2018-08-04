package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

////////////////////////////////////////////////////////////////////////////////
class MainActivity : AppCompatActivity(), AnkoLogger {
    private val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0
    private var mainUi = MainActivityUi()

    ////////////////////////////////////////
    var scanWifiService: ScanWifiService? = null
    private var isWifiBound = false

    private val scanWifiConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ScanWifiService.ScanWifiBinder
            scanWifiService = binder.inService
            isWifiBound = true
        }
        override fun onServiceDisconnected(className: ComponentName) {
            isWifiBound = false
        }
    }
    ////////////////////////////////////////
    private val receiver = UpdateReceiver()
    private val filter = IntentFilter()

    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "龍大理工学部出欠システム sk2"
        mainUi.setContentView(this)

        startService<ScanWifiService>()
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        if (! CheckInfo(mainUi) ) { startActivity(intentFor<LoginActivity>().clearTop()) }

        /*
        bindWifiScanService()
        */

        val sk2 = this.application as Sk2Globals
        //mainUi.wifiInfo.visibility = if (sk2.prefMap.getOrDefault("debug", false) as Boolean)
        mainUi.wifiInfo.visibility = if (sk2.prefMap["debug"] as Boolean ?: false)
            View.VISIBLE else View.INVISIBLE
        if (!wifiManager.isWifiEnabled()) {
            mainUi.attToastText ="無線LANをオンにしてください"
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
        } else {
            mainUi.attToastText = "出席！！！"
            ////////////////////////////////////////
            if (sk2.prefMap["beacon"] as Boolean ?: false) {
                filter.addAction("BEACON")
                registerReceiver(receiver, filter)
                startService<ScanBeaconService>()
                mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
            } else {
                stopService<ScanBeaconService>()
                mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_red)
            }
            ////////////////////////////////////////
            if (sk2.prefMap["auto"] as Boolean ?: false) {
                scanWifiService?.startInterval((sk2.prefMap["autoitv"] as Int ?: sk2._autoitv).toLong(),
                        sk2.prefMap["swtap"] as Boolean ?: false)
                mainUi.attBtn.text = "AUTO"
            } else {
                scanWifiService?.stopInterval()
                mainUi.attBtn.text = "出席"
            }
            ////////////////////////////////////////
                    }
    }
    ////////////////////////////////////////
    override fun onBackPressed() { /* DO NOTHING */ }
    ////////////////////////////////////////
    private fun askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permission, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION)
                return
            }
        }
    }
    ////////////////////////////////////////
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 許可された場合
        } else {
            alert("このアプリは無線LANとBluetoothビーコンによる位置情報を利用します") {
                yesButton { askPermission() }
                noButton { Logout() }
            }.show()
        }
    }
    ////////////////////////////////////////
    fun Logout() {
        val sk2 = this.application as Sk2Globals
        sk2.logout(scanWifiConnection)
    }
    ////////////////////////////////////////
    fun CheckInfo(ui: MainActivityUi): Boolean {
        val sk2 = this.application as Sk2Globals

        val uid = sk2.userMap["uid"] ?: ""
        val gcos = sk2.userMap["gcos"] ?: ""
        val name = sk2.userMap["name"] ?: ""
        //val uid = sk2.userMap.getOrDefault("uid", "")
        //val gcos = sk2.userMap.getOrDefault("gcos", "")
        //val name = sk2.userMap.getOrDefault("name", "")

        //val time = sk2.userMap.getOrDefault("name", 0L)
        // check key life
        //val now = System.currentTimeMillis()
        //val over = (now - time) > lifetime

        if ( uid == "") {
            return false
        } else {
            ui.userInfo.text = " $uid / $gcos / $name"
            return true
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    fun sendWifi(marker: Char) {
        val sk2 = this.application as Sk2Globals

        if (wifiManager.isWifiEnabled()) {
            bindWifiScanService()
            try {
                val info = scanWifiService?.sendApInfo(marker)
                if (sk2.prefMap["debug"] as Boolean ?: false) {
                //if (sk2.prefMap.getOrDefault("debug", false) as Boolean) {
                    mainUi.wifiInfo.text = info
                }
            } catch (e: RemoteException) {
                toast("サービスに接続できません")
                e.printStackTrace()
            }
        } else {
            toast("無線LANをオンにしてください")
        }
        //unbindWifiScanService()
    }
    ////////////////////////////////////////
    private fun bindWifiScanService() {
        if (! isWifiBound) {
            val scanWifiIntent = Intent(ctx, ScanWifiService::class.java)
            bindService(scanWifiIntent, scanWifiConnection, Context.BIND_AUTO_CREATE)
            isWifiBound = true
        }
    }
    ////////////////////////////////////////
    fun unbindWifiScanService() {
        if (isWifiBound) {
            unbindService(scanWifiConnection)
            isWifiBound = false
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////
    private inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent?.extras
            val msg = extras?.getString("btmessage")
            toast("ビーコン記録を送信します")
            sendWifi('B')
        }
    }

    override fun onPause() {
        super.onPause()
        scanWifiService?.stopInterval()
        unbindWifiScanService()
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        val sk2 = this.application as Sk2Globals
        sk2.prefMap["beacon"] = false
        sk2.prefMap["auto"] = false
        sk2.savePrefData()
        stopService<ScanBeaconService>()
        unbindWifiScanService()
        stopService<ScanWifiService>()
    }
}
////////////////////////////////////////////////////////////////////////////////
class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var attBtn: Button
    lateinit var wifiInfo: TextView
    lateinit var userInfo: TextView
    var attToastText = "出席！！！"

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
                    toast(attToastText)
                    ui.owner.sendWifi('M')
                }
            }.lparams {
                centerHorizontally()
                topMargin = dip(50); width = dip(250); height = dip(250)
            }
            ////////////////////////////////////////
            verticalLayout {
                ////////////////////////////////////////
                wifiInfo = textView("") {
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
                        margin = dip(16); width = dip(64); height = dip(64)
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
                        margin = dip(16); width = dip(64); height = dip(64)
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
                        margin = dip(16); width = dip(64); height = dip(64)
                    }

                }.lparams { width = wrapContent; gravity = Gravity.CENTER_HORIZONTAL }
            }.lparams {
                width = matchParent; alignParentBottom(); centerHorizontally()
            }
        }
    }
}
