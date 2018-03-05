package jp.ac.ryukoku.st.sk2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.*


class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("出席記録：龍大理工学部出欠システム")
        RecordActivityUi().setContentView(this)
    }
}

class RecordActivityUi: AnkoComponent<RecordActivity> {
    override fun createView(ui: AnkoContext<RecordActivity>) = with(ui) {

        verticalLayout {
            //webView {
            //    loadUrl("https://sk2.st.ryukoku.ac.jp/index.html")
            //}
        }
    }
}