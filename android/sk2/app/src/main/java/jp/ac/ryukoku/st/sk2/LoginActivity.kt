package jp.ac.ryukoku.st.sk2

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.view.Gravity
import android.view.View
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_LOGIN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.HINT_PASSWD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.HINT_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGIN_EXPIRY_PERIOD_DAYS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGIN_TIME_DAY_UNIT_MILLSEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_KEY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_LOGIN_TIME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_USER_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_COMMAND_AUTH
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_HOSTNAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_PORT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_REPLY_AUTH_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_REPLY_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_TIMEOUT_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_Large
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_LOGIN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_CANT_CONNECT_SERVER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_ATMARK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_PASSWD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_SUCCESS
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

/** ////////////////////////////////////////////////////////////////////////////// **/
class LoginActivity : Activity() {
    companion object {
        lateinit var sk2: Sk2Globals
        lateinit var pref: SharedPreferences
    }
    private var loginUi = LoginActivityUi()
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sk2 = this.application as Sk2Globals
        pref = Sk2Globals.pref

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = COLOR_BACKGROUND
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        }
        loginUi.setContentView(this)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onResume() {
        super.onResume()
        /** ユーザIDが空でなければ、**/
        if (pref.getString(PREF_UID, "").isNotBlank()) {
            /** 期限を確認して **/
            val today: Long = System.currentTimeMillis() / LOGIN_TIME_DAY_UNIT_MILLSEC
            if ( today - pref.getLong(PREF_LOGIN_TIME, 0L)/ LOGIN_TIME_DAY_UNIT_MILLSEC < LOGIN_EXPIRY_PERIOD_DAYS)
            /** そのままメインアクティビティへ **/
                startMain()
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** サーバでログイン認証 **/
    fun attemptLogin(user: String, passwd: String) {
        when {
            user.isBlank() -> toast(TOAST_LOGIN_ATTEMPT_UID)
            passwd.isBlank() -> toast(TOAST_LOGIN_ATTEMPT_PASSWD)
            user.contains('@') -> toast(TOAST_LOGIN_ATTEMPT_ATMARK)
        }
        /** sk2 サーバで認証してログイン **/
        doAsync {
            val result = authServer(user, passwd)
            uiThread {
                login(user, result)
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** サーバ認証 **/
    private fun authServer(user: String, passwd: String): String {

        lateinit var result: String /** 認証結果受信用 **/
        try {
            /** SSL Socket **/
            val sslSocketFactory = SSLSocketFactory.getDefault()
            val sslsocket = sslSocketFactory.createSocket()

            /** SSL Connect with TimeOut **/
            sslsocket.connect(InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT), SERVER_TIMEOUT_MILLISEC)

            /**  入出力バッファ **/
            val input = sslsocket.inputStream
            val output = sslsocket.outputStream
            val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
            val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

            /** Send message **/
            val message = "$SERVER_COMMAND_AUTH,$user,$passwd"
            bufWriter.write(message)
            bufWriter.flush()

            /** Receive message **/
            result = bufReader.use(BufferedReader::readText)

        } catch (e: Exception) {
            /** サーバ接続時にエラーが出たら Toast 表示だけ **/
            result = SERVER_REPLY_FAIL
            toast(TOAST_CANT_CONNECT_SERVER)
        }
        return result
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** ログインする **/
    private fun login(user: String, result: String) {
        if (result != SERVER_REPLY_AUTH_FAIL) { // サーバからの返信が失敗でなければ
            /** サーバ返信を ',' で分割 **/
            val v: List<String> = result.split(",")
            val time: Long = System.currentTimeMillis() // 現在時刻
            /** ユーザ名の空白は全て半角スペース一つに圧縮 **/
            val clrName: String = v[2].replace(Regex("\\s+"), " ")

            pref.edit() // SharedPreference に保存
                    .putString(PREF_UID, user)
                    .putString(PREF_KEY, v[0])
                    .putString(PREF_USER_NAME, clrName)
                    .putLong(PREF_LOGIN_TIME, time)     // not in use
                    .apply()

            toast(TOAST_LOGIN_SUCCESS)
            /** メイン画面へ **/
            startMain()
        } else {
            // in fail
            toast(TOAST_LOGIN_FAIL)
        }
    }
    private fun startMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
//@Suppress("EXPERIMENTAL_FEATURE_WARNING")
/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI構成 via Anko **/
class LoginActivityUi: AnkoComponent<LoginActivity> {
    companion object {
        const val TITLE = 1
        const val LABEL = 2
        const val USER = 3
        const val PASS = 4
        const val LOGIN = 5
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {
        relativeLayout {
            padding = dip(16)
            backgroundColor = Sk2Globals.COLOR_BACKGROUND
            /** ////////////////////////////////////////////////////////////////////////////// **/
            textView("$APP_TITLE $APP_NAME") {
                id = TITLE
                textSize = TEXT_SIZE_LARGE
                textColor = Color.BLACK
            }.lparams{
                centerHorizontally(); alignParentTop()
                topMargin = dip(100); bottomMargin = dip(50)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            textView("$HINT_UID / $HINT_PASSWD") {
                id = LABEL
                textSize = TEXT_SIZE_Large
                textColor = Color.BLACK
            }.lparams{
                below(TITLE); leftMargin = dip(4)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            val user = editText {
                id = USER
                textSize = TEXT_SIZE_LARGE
                hint = HINT_UID
                inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }.lparams { below(LABEL); width = matchParent }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            val passwd = editText {
                id = PASS
                textSize = TEXT_SIZE_LARGE
                hint = HINT_PASSWD
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }.lparams { below(USER); width = matchParent }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            button(BUTTON_TEXT_LOGIN) {
                id = LOGIN
                textColor = Color.WHITE
                textSize = TEXT_SIZE_LARGE
                padding = dip(0)
                backgroundColor = COLOR_NORMAL
                onClick {
                    /** remove user's leading and trailing blanks **/
                    ui.owner.attemptLogin(user.text.toString().trim(), passwd.text.toString())
                }
            }.lparams {
                below(PASS); centerHorizontally(); width = matchParent
                topMargin = dip(50)
            }
        }
    }
}

