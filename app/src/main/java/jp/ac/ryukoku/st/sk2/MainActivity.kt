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

    }
}

class MainActivityUi: AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

        verticalLayout {
            padding = dip(16)

            button("出席") {
                textColor = Color.WHITE
                textSize = 32f
                background = ContextCompat.getDrawable(context, R.drawable.button_states)
                onClick {
                    ctx.toast("出席！！！")
                }
            }.lparams {
                gravity = Gravity.CENTER_HORIZONTAL
                margin = dip(64)
                width = dip(256)
                height = dip(256)
            }

            linearLayout {

                button("?") {
                    textColor = Color.WHITE
                    textSize = 24f
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        browse("https://sk2.st.ryukoku.ac.jp/index.html")
                    }
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                    margin = dip(32)
                    width = dip(64)
                    height = dip(64)
                }

                button("?") {
                    textColor = Color.WHITE
                    textSize = 24f
                    background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                    onClick {
                        startActivity<HelpActivity>()
                    }
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                    margin = dip(32)
                    width = dip(64)
                    height = dip(64)
                }
            }.lparams { gravity = Gravity.CENTER_HORIZONTAL }
        }
    }
}