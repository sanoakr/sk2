package jp.ac.ryukoku.st.sk2

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.os.*
import java.io.*
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class MainActivity : AppCompatActivity(), AnkoLogger {
    private val PERMISSIONS_REQUEST_COARSE_LOCATION = 456
    private var mainUi = MainActivityUi()
    private var latest: String? = null
    //private var latest: MutableMap<String, String> = mutableMapOf()

    ////////////////////////////////////////
    //var scanService: ScanService? = null
    //private var isBound = false
    /*
    private val scanConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ScanService.ScanBinder
            scanService = binder.inService
            isBound = true
        }
        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
        }
    }*/
    ////////////////////////////////////////
    private val handler = Handler()
    private val timer = Runnable { interval() }
    var period: Long = 10*60*1000 // initialize 10min
    private fun interval() {
        sendServer('A')
        handler.postDelayed(timer, period)
    }
    fun startInterval(sec: Long) {
        period = sec *1000
        handler.postDelayed(timer, period)
    }
    fun stopInterval() { handler.removeCallbacks(timer) }
    ////////////////////////////////////////
    private val receiver = UpdateReceiver()
    private val filter = IntentFilter()

    ////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "${sk2.app_title} ${sk2.app_name}"
        mainUi.setContentView(this)
        ////////////////////////////////////////
        filter.addAction("BEACON")
        registerReceiver(receiver, filter)
        if (! isServiceWorking(ScanService::class.java)) {
            if (Build.VERSION.SDK_INT >= 26) {
                this.startForegroundService(Intent(this, ScanService::class.java))
            } else {
                startService<ScanService>()
            }
        }
        ////////////////////////////////////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permission, PERMISSIONS_REQUEST_COARSE_LOCATION)
        }
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
            if ((sk2.prefMap["auto"] ?: false) as Boolean) {
                val sendItv = ((sk2.prefMap["autoitv"] ?: sk2._autoitv) as Int).toLong()
                startInterval(sendItv)
                mainUi.attBtn.text = "AUTO"
                mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_red)
            } else {
                stopInterval()
                mainUi.attBtn.text = "出席"
                mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_states_blue)
            }
            ////////////////////////////////////////
        }
    }
    ////////////////////////////////////////
    private inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent?.extras
            latest = extras?.getString("scaninfo")
            info(latest)
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
    fun sendServer(marker: Char) {
        var toastMsg = "スキャン情報がありません"
        val data = latest

        if (data.isNullOrEmpty()) {
            doAsync { uiThread {toast(toastMsg)} }
            mainUi.scanInfo.text = "no Scan Messages"
            return
        }
        val sk2 = this.application as Sk2Globals
        val user = (sk2.userMap["uid"] ?: "") as String
        val info = "${user},${marker}," + data
        mainUi.scanInfo.text = data
        sk2.localQueue.push(info)

        doAsync {
            try {
                val sslSocketFactory = SSLSocketFactory.getDefault()
                val sslsocket = sslSocketFactory.createSocket()
                sslsocket.connect(InetSocketAddress(sk2.serverHost, sk2.serverPort), sk2.timeOut)

                val input = sslsocket.inputStream
                val output = sslsocket.outputStream
                val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
                val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

                // Send message
                bufWriter.write(info)
                bufWriter.flush()
                // Receive message
                val recMessage = bufReader.use(BufferedReader::readText)
                toastMsg = if (recMessage == sk2.recFail) {
                    "データが記録できません"
                } else {
                    "データを記録しました"
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                toastMsg = "サーバに接続できません"
            }
            uiThread { toast(toastMsg) }
        }
        return
    }
    ////////////////////////////////////////////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        val sk2 = this.application as Sk2Globals
        sk2.prefMap["auto"] = false
        sk2.savePrefData()
        unregisterReceiver(receiver)
        stopService<ScanService>()
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
    private fun isServiceWorking(clazz: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { clazz.name == it.service.className }
    }
    ////////////////////////////////////////
    private fun hasBLE(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    ////////////////////////////////////////
    private fun checkBLE(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if ( btAdapter == null || !hasBLE() ) {
            toast("BLEアダプタが見つかりません")
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
                    ui.owner.sendServer('M')
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
                    width = matchParent; gravity = Gravity.END
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
