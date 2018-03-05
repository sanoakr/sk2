package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.sdk25.coroutines.onClick


class HelpActivity : AppCompatActivity() {

    val sk2url = "https://sk2.st.ryukoku.ac.jp/index.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("ヘルプ")
        HelpActivityUi().setContentView(this)
    }
}

class HelpActivityUi: AnkoComponent<HelpActivity> {
    override fun createView(ui: AnkoContext<HelpActivity>) = with(ui) {

        verticalLayout {
            webView {
            }
        }
    }
}