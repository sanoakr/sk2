package jp.ac.ryukoku.st.sk2

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType.*
import android.util.Log
import android.view.Gravity
import android.view.View
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.BUTTON_TEXT_LOGIN
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_HINT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_NORMAL_LIGHT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.HINT_PASSWD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.HINT_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.NAME_START_TESTUSER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_KEY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_LOGIN_TIME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_ROOM_JSON
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_USER_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PRIVACY_POLICY_NO_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PRIVACY_POLICY_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PRIVACY_POLICY_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PRIVACY_POLICY_YES_TEXT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_COMMAND_AUTH
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_HOSTNAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_PORT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_REPLY_AUTH_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_REPLY_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_TIMEOUT_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_Large
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_CANT_CONNECT_SERVER
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_ATMARK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_PASSWD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_ATTEMPT_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_FAIL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOGIN_SUCCESS
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.nio.charset.Charset
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
        sk2.startMain()

        /** auto login for testing **/
        //attemptLogin("testuser", "testuser")
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** Disable Back Key **/
    override fun onBackPressed() {}
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** サーバでログイン認証 **/
    fun attemptLogin(user: String, passwd: String) {
        when {
            user.isBlank() -> toast(TOAST_LOGIN_ATTEMPT_UID).setGravity(Gravity.CENTER, 0, 0)
            ! user.startsWith(NAME_START_TESTUSER) && passwd.isBlank() -> toast(TOAST_LOGIN_ATTEMPT_PASSWD).setGravity(Gravity.CENTER, 0, 0)
            user.contains('@') -> toast(TOAST_LOGIN_ATTEMPT_ATMARK).setGravity(Gravity.CENTER, 0, 0)
        }
        /** sk2 サーバで認証してログイン **/
        doAsync {
            val result = authServer(user, passwd)

            uiThread {
                if (result == SERVER_REPLY_FAIL)
                    toast(TOAST_CANT_CONNECT_SERVER).setGravity(Gravity.CENTER, 0, 0)
                else
                    login(user, result)
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** サーバ認証 **/
    private fun authServer(user: String, passwd: String): String {
        var result: String /** 認証結果受信用 **/
        //var resultByte: ByteArray
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
            /*
            var line: String?
            do {
                line = bufReader.readLine()
                Log.d("XXX", line)
                result += line
            } while (line != null)
            */
            //resultByte = input.readBytes()
            //result = String(resultByte, Charsets.UTF_8)
            result = bufReader.use { it.readText() }
            //Log.d("XXX", result.last().toString())
            //Log.d("XXX", resultByte.size.toString())

        } catch (e: Exception) {
            /** サーバ接続時にエラーが出たら Toast メッセージを返す **/
            result = SERVER_REPLY_FAIL
        }
        return result
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** ログインする **/
    private fun login(user: String, result: String) {
        if (result != SERVER_REPLY_AUTH_FAIL) { // サーバからの返信が失敗でなければ
            /** サーバ返信を ',' で分割 **/
            val v: List<String> = result.split(",", limit = 4)
            val time: Long = System.currentTimeMillis() // 現在時刻
            /** ユーザ名の空白は全て半角スペース一つに圧縮 **/
            val clrName: String = v[2].replace(Regex("\\s+"), " ")
            val json = v[3]
            //Log.d("XXX", json.last().toString())

            pref.edit() // SharedPreference に保存
                    .putString(PREF_UID, user)
                    .putString(PREF_KEY, v[0])
                    .putString(PREF_USER_NAME, clrName)
                    .putLong(PREF_LOGIN_TIME, time)     // not in use
                    .putString(PREF_ROOM_JSON, json)
                    .apply()
            //Log.d("XXX", pref.getString(PREF_ROOM_JSON, "Non").last().toString())
            toast(TOAST_LOGIN_SUCCESS).setGravity(Gravity.CENTER, 0, 0)
            /** メイン画面へ **/
            sk2.startMain()
        } else {
            // in fail
            toast(TOAST_LOGIN_FAIL).setGravity(Gravity.CENTER, 0, 0)
        }
    }
}
//@Suppress("EXPERIMENTAL_FEATURE_WARNING")
/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI構成 via Anko **/
class LoginActivityUi: AnkoComponent<LoginActivity> {
    companion object {
        const val TITLE = 1
        const val LABEL = 2
        //const val USER = 3
        //const val PASS = 4
        //const val LOGIN = 5
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {
        relativeLayout {
            padding = dip(16)
            backgroundColor = COLOR_BACKGROUND
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
                id = R.id.username
                //id = USER
                textSize = TEXT_SIZE_LARGE
                hint = HINT_UID
                hintTextColor = COLOR_HINT
                inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }.lparams { below(LABEL); width = matchParent; height = dip(50) }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            val passwd = editText {
                id = R.id.password
                //id = PASS
                textSize = TEXT_SIZE_LARGE
                hint = HINT_PASSWD
                hintTextColor = COLOR_HINT
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }.lparams { below(R.id.username); width = matchParent; height = dip(50) }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            button(BUTTON_TEXT_LOGIN) {
                id = R.id.login
                //id = LOGIN
                textColor = Color.WHITE
                textSize = TEXT_SIZE_LARGE
                padding = dip(0)
                backgroundColor = COLOR_NORMAL_LIGHT
                //backgroundColor = COLOR_NORMAL
                onClick {
                    alert {
                        positiveButton(PRIVACY_POLICY_YES_TEXT) { _ ->
                            ui.owner.attemptLogin(user.text.toString().trim(), passwd.text.toString())
                        }
                        negativeButton(PRIVACY_POLICY_NO_TEXT) { _ -> }
                        customView {
                            verticalLayout {
                                textView(PRIVACY_POLICY_TITLE) {
                                    textSize = TEXT_SIZE_LARGE
                                    textColor = Color.WHITE
                                    backgroundColor = COLOR_NORMAL_LIGHT
                                    padding = dip(4)
                                }.lparams {
                                    width = matchParent
                                }
                                scrollView {
                                    textView(PRIVACY_POLICY_TEXT) {
                                        textSize = TEXT_SIZE_NORMAL
                                        padding = dip(4)
                                    }
                                }
                            }
                        }
                    }.show()
                    //ui.owner.attemptLogin(user.text.toString().trim(), passwd.text.toString())
                }
            }.lparams {
                below(R.id.password); centerHorizontally(); width = matchParent
                topMargin = dip(30)
            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            textView("Version ${Sk2Globals.APP_VERSION_NAME} (${Sk2Globals.APP_VERSION_CODE})") {
                id = MainActivityUi.VERSION
                textColor = Color.BLACK
                textSize = Sk2Globals.TEXT_SIZE_TINY
            }.lparams {
                alignParentBottom(); alignParentEnd()
            }
        }
    }
}

