package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        setTitle("龍谷大学理工学部出欠システム")
        LoginActivityUi().setContentView(this)
    }
}

class LoginActivityUi: AnkoComponent<LoginActivity> {
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {

        verticalLayout {
            padding = dip(16)

            textView("全学認証ログイン"){
                textSize = 20f
            }.lparams{
                width = matchParent
                topMargin = dip(8)
            }

            val title = editText {
                hint = "全学認証ID"
            }

            val description = editText {
                hint = "パスワード"
            }

            button("ログイン") {
                textColor = Color.WHITE
                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                onClick {
                    ctx.toast("${title.text} : ${description.text}")
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dip(16)
            }
        }
    }
}
