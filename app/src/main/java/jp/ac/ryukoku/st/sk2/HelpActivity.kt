package jp.ac.ryukoku.st.sk2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "ヘルプ：龍大理工学部出欠システム sk2"
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