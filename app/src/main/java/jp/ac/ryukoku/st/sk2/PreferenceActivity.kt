package jp.ac.ryukoku.st.sk2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
class PreferenceActivity : AppCompatActivity() {

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("設定：龍大理工学部出欠システム")
        PreferenceActivityUi().setContentView(this)
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
            }.lparams {
                topMargin = dip(24); width = matchParent
            }
        }
    }
}
