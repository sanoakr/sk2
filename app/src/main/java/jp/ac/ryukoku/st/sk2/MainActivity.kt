package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "龍大理工学部出欠システム sk2"
        mainUi.setContentView(this)

        askWifiPermission()

        startService<ScanWifiService>()
        bindWifiScanService()
        //unbindWifiScanService()
        //stopService<ScanWifiService>()
    }
    ////////////////////////////////////////
    private fun askWifiPermission() {
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
            alert("このアプリは無線LANアクセスポイントのスキャンが必要です．ログアウトしますか？") {
                yesButton { Logout() }
                noButton { askWifiPermission() }
            }.show()
        }
    }
    ////////////////////////////////////////
    fun Logout() {
        val sk2 = this.application as Sk2Globals
        sk2.logout(scanWifiConnection)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()

        if (! CheckInfo(mainUi) ) { startActivity(intentFor<LoginActivity>().clearTop()) }

        mainUi.wifiInfo.text = ""

        if (!wifiManager.isWifiEnabled()) {
            mainUi.attToastText ="無線LANをオンにしてください"
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
        } else {
            mainUi.attToastText ="出席！！！"
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states)
        }
    }
    ////////////////////////////////////////
    override fun onBackPressed() { /* nothing to do */ }

    ////////////////////////////////////////
    fun CheckInfo(ui: MainActivityUi): Boolean {
        val sk2 = this.application as Sk2Globals

        val uid = sk2.userMap.getOrDefault("uid", "")
        val gcos = sk2.userMap.getOrDefault("gcos", "")
        val name = sk2.userMap.getOrDefault("name", "")
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
    ////////////////////////////////////////
    fun sendWifi() {
        val sk2 = this.application as Sk2Globals

        if (wifiManager.isWifiEnabled()) {
            bindWifiScanService()
            try {
                val info = scanWifiService?.sendApInfo('M')
                if (sk2.prefMap.getOrDefault("debug", false)) {
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
            userInfo = textView("") {
                textColor = Color.BLACK
            }.lparams {
                alignParentTop(); centerHorizontally()
                topMargin = dip(5)
            }
            attBtn = button("出席") {
                textColor = Color.WHITE
                textSize = 32f
                background = ContextCompat.getDrawable(ctx, R.drawable.button_states)
                onClick {
                    toast(attToastText)
                    ui.owner.sendWifi()
                }
            }.lparams {
                centerHorizontally()
                topMargin = dip(50); width = dip(250); height = dip(250)
            }
            verticalLayout {
                wifiInfo = textView("") {
                    textSize = 12f
                }.lparams {
                    bottomMargin = dip(5); padding = dip(5)
                    width = matchParent; gravity = Gravity.RIGHT
                }

                linearLayout {
                    imageButton {
                        imageResource = R.drawable.ic_settings_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            ui.owner.unbindWifiScanService()
                            startActivity<PreferenceActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(16); width = dip(64); height = dip(64)
                    }

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