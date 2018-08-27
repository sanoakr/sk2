package jp.ac.ryukoku.st.sk2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import org.jetbrains.anko.*

/** ////////////////////////////////////////////////////////////////////////////// **/
class SplashActivity : Activity() {
    private var splashUi = SplashActivityUi()

    private val handler = Handler()
    private val runnable = { startMain() }
    fun startMain() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.statusBarColor = COLOR_NORMAL
        splashUi.setContentView(this)

        handler.postDelayed(runnable, 1000)
    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI構成 via Anko **/
class SplashActivityUi: AnkoComponent<SplashActivity> {
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<SplashActivity>) = with(ui) {
        relativeLayout {
            backgroundColor = COLOR_NORMAL
            /** ////////////////////////////////////////////////////////////////////////////// **/
            textView("$APP_TITLE $APP_NAME") {
                textSize = TEXT_SIZE_LARGE
                textColor = Color.WHITE
            }.lparams{
                centerInParent()
            }
        }
    }
}

