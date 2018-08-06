package jp.ac.ryukoku.st.sk2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "ヘルプ：${sk2.app_title} ${sk2.app_name}"
        HelpActivityUi().setContentView(this)
    }
}

////////////////////////////////////////////////////////////////////////////////
class HelpActivityUi: AnkoComponent<HelpActivity> {
    override fun createView(ui: AnkoContext<HelpActivity>) = with(ui) {

        verticalLayout {
            webView {
                loadUrl("https://sk2.st.ryukoku.ac.jp/index.html")
            }
        }
    }
}