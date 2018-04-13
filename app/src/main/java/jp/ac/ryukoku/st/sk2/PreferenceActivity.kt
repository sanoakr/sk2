package jp.ac.ryukoku.st.sk2

import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener

////////////////////////////////////////////////////////////////////////////////
class PreferenceActivity: AppCompatActivity(), AnkoLogger {
    private var prefUi = PreferenceActivityUi()

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "設定：龍大理工学部出欠システム sk2"
        prefUi.setContentView(this)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        val sk2 = this.application as Sk2Globals
        sk2.restorePrefData()

        prefUi.swBeacon.isChecked = sk2.prefMap.getOrDefault("beacon", false) as Boolean
        prefUi.swAuto.isChecked = sk2.prefMap.getOrDefault("auto", false) as Boolean
        prefUi.seekIntv.isEnabled = if (prefUi.swAuto.isChecked) true else false
        prefUi.swChangeAP.isChecked = sk2.prefMap.getOrDefault("swtap", false) as Boolean
        prefUi.swChangeAP.isEnabled = if (prefUi.swAuto.isChecked) true else false
        prefUi.swDebug.isChecked = sk2.prefMap.getOrDefault("debug", false) as Boolean
        prefUi.seekTextMinutes = (sk2.prefMap.getOrDefault("autoitv", 0L) as Int)/60
        prefUi.seekMin = if (prefUi.swDebug.isChecked) 1 else 10
        prefUi.seekIntv.progress = prefUi.seekTextMinutes - prefUi.seekMin
        prefUi.debugText.visibility = if (prefUi.swDebug.isChecked) View.VISIBLE else View.INVISIBLE
        prefUi.btSearch.visibility = if (prefUi.swDebug.isChecked) View.VISIBLE else View.INVISIBLE
    }
    ////////////////////////////////////////
    fun setPref(key: String, value: Any) {
        val sk2 = this.application as Sk2Globals
        sk2.prefMap[key] = value
        sk2.savePrefData()
    }
    ////////////////////////////////////////
    fun checkWifi(): Boolean {
        if (wifiManager.isWifiEnabled()) {
            return true
        } else {
            toast("無線LANをオンにしてください")
            prefUi.swAuto.isChecked = false
            return false
        }
    }
    ////////////////////////////////////////
    private fun hasBLE(): Boolean {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    ////////////////////////////////////////
    fun checkBt(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if ( btAdapter == null || !hasBLE() ) {
            toast("このデバイスのBLEアダプタが見つかりません")
            return false
        } else if (! btAdapter.isEnabled) {
            toast("Bluetoothをオンにしてください")
            return false
        }
        return true
    }
    ////////////////////////////////////////////////////////////////////////////////
    class PreferenceActivityUi : AnkoComponent<PreferenceActivity> {
        lateinit var swBeacon: Switch
        lateinit var swAuto: Switch
        lateinit var seekText: TextView
        lateinit var seekIntv: SeekBar
        lateinit var swChangeAP: Switch
        lateinit var swDebug: Switch
        var seekMin = 10
        var seekTextMinutes = 10
        lateinit var debugText: TextView
        lateinit var btSearch: Button
        ////////////////////////////////////////
        override fun createView(ui: AnkoContext<PreferenceActivity>) = with(ui) {
            verticalLayout {
                padding = dip(16)
                ////////////////////////////////////////
                swBeacon = switch {
                    text = "Bluetoothビーコンによる出席記録をオンにする"
                    textSize = 14f
                    onClick {
                        if (isChecked) {
                            if (ui.owner.checkBt()) {
                                ui.owner.setPref("beacon", true)
                                startService<ScanBeaconService>()
                            } else {
                                swBeacon.isChecked = false
                            }
                        } else {
                            ui.owner.setPref("beacon", false)
                            stopService<ScanBeaconService>()
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////
                swAuto = switch {
                    text = "出席記録をバックグラウンドで自動化する"
                    textSize = 14f
                    ////////////////////////////////////////
                    onClick {
                        if (isChecked) {
                            if (ui.owner.checkWifi()) {
                                seekIntv.isEnabled = true
                                swChangeAP.isEnabled = true
                                ui.owner.setPref("auto", true)
                            }
                        } else {
                            seekIntv.isEnabled = false
                            swChangeAP.isEnabled = false
                            ui.owner.setPref("auto", false)
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////
                seekText = textView ("自動記録の間隔： ${seekTextMinutes} 分"){
                    textSize = 14f
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////
                seekIntv = seekBar {
                    max = 90
                    onSeekBarChangeListener {
                        onProgressChanged { _, progress, _ ->
                            seekTextMinutes = seekMin + progress
                            seekText.text = "自動記録の間隔： ${seekTextMinutes} 分"
                            ui.owner.setPref("autoitv", seekTextMinutes*60)
                        }
                    }
                }.lparams {
                    topMargin = dip(12); width = matchParent
                }
                ////////////////////////////////////////
                swChangeAP = switch {
                    text = "最適な龍大無線LANに自動接続する（1分毎）"
                    textSize = 14f
                    ////////////////////////////////////////
                    onClick {
                        if (isChecked) {
                            if (ui.owner.checkWifi()) {
                                ui.owner.setPref("swtap", true)
                            }
                        } else {
                            ui.owner.setPref("swtap", false)
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////

                ////////////////////////////////////////
                button {
                    text = "ログアウト"
                    onClick {
                        val sk2 = ui.owner.application as Sk2Globals
                        sk2.logout(null)
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////
                swDebug = switch {
                    text = "デバッグモード"
                    textSize = 14f
                    onClick {
                        if (isChecked) {
                            seekMin = 1
                            seekIntv.max = 199
                            seekIntv.progress += 9
                            ui.owner.setPref("debug", true)
                            debugText.visibility = View.VISIBLE
                            btSearch.visibility = View.VISIBLE
                        } else {
                            seekMin = 10
                            seekIntv.max = 50
                            seekIntv.progress -= 9
                            ui.owner.setPref("debug", false)
                            debugText.visibility = View.INVISIBLE
                            btSearch.visibility = View.INVISIBLE
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                ////////////////////////////////////////
                debugText = textView ("""
* SSID を ryu-wiress のみに制限
* BSSID と教室情報とのマップを拡充
* key生成にデバイスID(GoogleID)を含める?
"""
                ){
                    textSize = 12f
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                btSearch = button {
                    text = "記録検索ページへ"
                    onClick {
                        browse("https://sk2.st.ryukoku.ac.jp/search.php")
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }

            }
        }
    }
}

