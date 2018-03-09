package jp.ac.ryukoku.st.sk2

import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

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

        prefUi.swBeacon.isChecked = sk2.prefMap.getOrDefault("beacon", false)
        prefUi.swAuto.isChecked = sk2.prefMap.getOrDefault("auto", false)
        prefUi.swDebug.isChecked = sk2.prefMap.getOrDefault("debug", false)
    }
    ////////////////////////////////////////
    fun setPref(key: String, value: Boolean) {
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
        } else if (! btAdapter?.isEnabled) {
            toast("Bluetoothをオンにしてください")
            return false
        }
        return true
    }
    ////////////////////////////////////////////////////////////////////////////////
    class PreferenceActivityUi : AnkoComponent<PreferenceActivity> {
        lateinit var swBeacon: Switch
        lateinit var swAuto: Switch
        lateinit var swDebug: Switch
        ////////////////////////////////////////
        override fun createView(ui: AnkoContext<PreferenceActivity>) = with(ui) {
            verticalLayout {
                padding = dip(16)

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
                swAuto = switch {
                    text = "出席記録をバックグラウンドで自動化する"
                    textSize = 14f
                    onClick {
                        if (isChecked) {
                            if (ui.owner.checkWifi()) {
                                ui.owner.setPref("auto", true)
                            }
                        } else {
                            ui.owner.setPref("auto", false)
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                swDebug = switch {
                    text = "デバッグモード"
                    textSize = 14f
                    onClick {
                        if (isChecked) {
                            ui.owner.setPref("debug", true)
                        } else {
                            ui.owner.setPref("debug", false)
                        }
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
                button {
                    text = "ログアウト"
                    onClick {
                        val sk2 = ui.owner.application as Sk2Globals
                        sk2.logout(null)
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
            }
        }
    }
}

