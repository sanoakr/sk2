package jp.ac.ryukoku.st.sk2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_HELP_URI
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_HELP
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
/*** ヘルプ表示 ***/
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "$TITLE_HELP: $APP_TITLE $APP_NAME"
        HelpActivityUi().setContentView(this)
    }
}
////////////////////////////////////////////////////////////////////////////////
class HelpActivityUi: AnkoComponent<HelpActivity> {
    override fun createView(ui: AnkoContext<HelpActivity>) = with(ui) {
        verticalLayout {
            webView {
                loadUrl(SERVER_HELP_URI) // sk2 のヘルプページの URI
            }
        }
    }
}