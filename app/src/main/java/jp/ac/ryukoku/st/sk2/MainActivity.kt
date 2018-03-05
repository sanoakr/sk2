package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("龍大理工学部出欠システム")
        MainActivityUi().setContentView(this)

        if (!wifiManager.isWifiEnabled()) {
            toast("無線LANをオンにしてください")
        }
    }
}

class MainActivityUi: AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

        relativeLayout {

            button("出席") {
                textColor = Color.WHITE
                textSize = 32f
                background = ContextCompat.getDrawable(context, R.drawable.button_states)
                onClick {
                    ctx.toast("出席！！！")
                }
            }.lparams {
                alignParentTop()
                centerHorizontally()
                //gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dip(100)
                width = dip(250)
                height = dip(250)
            }

            linearLayout {

                imageButton {
                    imageResource = R.drawable.ic_settings_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<PreferenceActivity>()
                    }
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                    margin = dip(16)
                    width = dip(64)
                    height = dip(64)
                }

                imageButton {
                    imageResource = R.drawable.ic_history_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<RecordActivity>()
                    }
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                    margin = dip(16)
                    width = dip(64)
                    height = dip(64)
                }

                imageButton {
                    imageResource = R.drawable.ic_live_help_32dp
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<HelpActivity>()
                    }
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                    margin = dip(16)
                    width = dip(64)
                    height = dip(64)
                }

            }.lparams {
                alignParentBottom()
                centerHorizontally()
            }
        }
    }
}