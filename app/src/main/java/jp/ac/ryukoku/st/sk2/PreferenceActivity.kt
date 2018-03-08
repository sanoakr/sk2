package jp.ac.ryukoku.st.sk2

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Switch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick

////////////////////////////////////////////////////////////////////////////////
class PreferenceActivity : AppCompatActivity() {
    private var prefUi = PreferenceActivityUi()
    val prefName = "st.ryukoku.sk2"

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "設定：龍大理工学部出欠システム sk2"
        prefUi.setContentView(this)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()

        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefUi.swBeacon.isChecked = pref.getBoolean("beacon", false)
        prefUi.swAuto.isChecked = pref.getBoolean("auto", false)
        prefUi.swDebug.isChecked = pref.getBoolean("debug", false)
    }
        ////////////////////////////////////////
    fun Logout() {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val e = pref.edit()
        // Clear Preferences
        e.putString("uid", "")
        e.putString("key", "")
        e.putString("gcos", "")
        e.putString("name", "")
        e.putLong("time", 0)
        e.apply()

        startActivity(intentFor<LoginActivity>().clearTop())
    }
    ////////////////////////////////////////
    fun setPref(key: String, value: Boolean) {
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val e = pref.edit()
        e.putBoolean(key, value)
        e.apply()
    }
    ////////////////////////////////////////
    fun checkBT(): Boolean {
        val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (btAdapter == null) {
            toast("このデバイスのBluetoothアダプタが見つかりません")
            prefUi.swBeacon.isChecked = false
            return false
        } else if (! btAdapter.isEnabled ) {
            toast("Bluetoothをオンにしてください")
            prefUi.swBeacon.isChecked = false
            return false
        } else {
            return true
        }
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
                    text = "Bluetooth ビーコンによる出席記録をオンにする"
                    textSize = 14f
                    onClick {
                        if (isChecked) {
                            if (ui.owner.checkBT()) {
                                ui.owner.setPref("beacon", true)
                            }
                        } else {
                            ui.owner.setPref("beacon", false)
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
                            ui.owner.setPref("auto", true)

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
                        ui.owner.Logout()
                    }
                }.lparams {
                    topMargin = dip(24); width = matchParent
                }
            }
        }
    }
}

