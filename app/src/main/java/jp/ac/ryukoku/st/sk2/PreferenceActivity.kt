package jp.ac.ryukoku.st.sk2

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.RadioGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick

////////////////////////////////////////////////////////////////////////////////
class PreferenceActivity : AppCompatActivity() {

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "設定：龍大理工学部出欠システム sk2"
        PreferenceActivityUi().setContentView(this)
    }
    ////////////////////////////////////////
    fun Logout() {
        val prefName = "st.ryukoku.sk2"
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val e = pref.edit()
        // Clear Preferences
        e.putString("uid", "")
        e.putString("key", "")
        e.putString("gcos","")
        e.putString("name","")
        e.putLong("time", 0)
        e.apply()

        startActivity(intentFor<LoginActivity>().clearTop())
    }
}

////////////////////////////////////////////////////////////////////////////////
class PreferenceActivityUi: AnkoComponent<PreferenceActivity> {

    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<PreferenceActivity>) = with(ui) {
        verticalLayout {
            padding = dip(16)

            switch {
                text = "Bluetooth ビーコンによる出席記録をオンにする"
                textSize = 14f
            }.lparams {
                topMargin = dip(24); width = matchParent
            }
            switch {
                text = "出席記録をバックグラウンドで自動化する"
                textSize = 14f
            }.lparams {
                topMargin = dip(24); width = matchParent
            }
            switch {
                text = "デバッグモード"
                textSize = 14f
                onClick {
                    toast("Debug ON")
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
