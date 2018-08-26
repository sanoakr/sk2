package jp.ac.ryukoku.st.sk2

import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.view.Gravity
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
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
class LoginActivity : AppCompatActivity() {
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

        title = "$TITLE_LOGIN: $APP_TITLE $APP_NAME"
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
                startActivity<MainActivity>()
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
            startActivity<MainActivity>()
        } else {
            // in fail
            toast(TOAST_LOGIN_FAIL)
        }
    }
}
//@Suppress("EXPERIMENTAL_FEATURE_WARNING")
/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI構成 via Anko **/
class LoginActivityUi: AnkoComponent<LoginActivity> {
    companion object {
        const val HINT_UID = "学籍番号ID"
        const val HINT_PASSWD = "パスワード"

        const val BUTTON_TEXT_LOGIN = "Login"
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {
        verticalLayout {
            padding = dip(16)
            /** ////////////////////////////////////////////////////////////////////////////// **/
            val user = editText {
                hint = HINT_UID
                inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            val passwd = editText {
                hint = HINT_PASSWD
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            button(BUTTON_TEXT_LOGIN) {
                textColor = Color.WHITE
                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                onClick {
                    /** remove user's leading and trailing blanks **/
                    ui.owner.attemptLogin(user.text.toString().trim(), passwd.text.toString())
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dip(16)
            }
        }
    }
}

