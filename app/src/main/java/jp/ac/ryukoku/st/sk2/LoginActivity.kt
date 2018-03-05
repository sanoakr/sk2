package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

////////////////////////////////////////////////////////////////////////////////
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        setTitle("ログイン：龍大理工学部出欠システム")
        LoginActivityUi().setContentView(this)

        val android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
        toast(android_id)
    }
}

////////////////////////////////////////////////////////////////////////////////
class LoginActivityUi: AnkoComponent<LoginActivity> {
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {

        verticalLayout {
            padding = dip(16)

            val title = editText {
                hint = "学籍番号ID"
                inputType = TYPE_CLASS_TEXT
            }

            val description = editText {
                hint = "パスワード"
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }

            button("ログイン") {
                textColor = Color.WHITE
                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                onClick {
                    toast("${title.text} : ${description.text}")
                    startActivity<MainActivity>()
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dip(16)
            }
        }
    }
}
