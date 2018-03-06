package jp.ac.ryukoku.st.sk2

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class LoginActivity : AppCompatActivity(), AnkoLogger {
    private val prefName = "st.ryukoku.sk2"
    private val replyFail = "authfail"

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = ("ログイン：龍大理工学部出欠システム")
        val contentView = LoginActivityUi().setContentView(this)

        val androidId = getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        toast(androidId)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()

        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val user = pref.getString("uid", "")
        val gcos = pref.getString("gcos", "")
        val name = pref.getString("name", "")
        val time = pref.getLong("time", 0)
        if (! user.isNullOrBlank()) {
            startActivity<MainActivity>()
        }
    }
    ////////////////////////////////////////
    fun attemptLogin(user: String, passwd: String) {
        if (user.isNullOrBlank()) {
            toast("学籍番号を入力して下さい")
        } else if (passwd.isNullOrBlank()) {
            toast("パスワードを入力して下さい")
        } else if(user.contains('@')) {
            toast("認証IDに @ 以降を含めないで下さい")
        }
        // authentication on sk2 Server
        doAsync {
            val result = authServer(user, passwd)
            uiThread {
                Login(user, result)
            }
        }
    }
    ////////////////////////////////////////
    fun authServer(user: String, passwd: String): String {
        // sk2 server
        val serverHost = "sk2.st.ryukoku.ac.jp"
        val serverPort = 4440
        val timeOut = 5000
        val authWord = "AUTH"
        var result = ""

        try {
            val sslSocketFactory = SSLSocketFactory.getDefault()
            val sslsocket = sslSocketFactory.createSocket()
            //connect with TimeOut
            sslsocket.connect(InetSocketAddress(serverHost, serverPort), timeOut)

            val input = sslsocket.getInputStream()
            val output = sslsocket.getOutputStream()
            val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
            val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))
            // Send message
            val message = "$authWord,$user,$passwd"
            bufWriter.write(message)
            bufWriter.flush()
            // Receive message
            result = bufReader.use(BufferedReader::readText)

        } catch (e: Exception) {
            result = replyFail
        }
        return result
    }

    ////////////////////////////////////////
    fun Login(user: String, result: String) {
        if (result != replyFail) {
            val v = result.split(",")
            val time = System.currentTimeMillis()

            val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val e = pref.edit()

            e.putString("uid", user)
            e.putString("key", v[0])
            e.putString("gcos", v[1])
            e.putString("name", v[2])
            e.putLong("time", time)
            e.apply()

            toast("ログインします")
            startActivity<MainActivity>()

        } else {
            toast("ログインに失敗しました")
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class LoginActivityUi: AnkoComponent<LoginActivity> {

    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {
        verticalLayout {
            padding = dip(16)

            val user = editText {
                hint = "学籍番号ID"
                inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }

            val passwd = editText {
                hint = "パスワード"
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }

            button("ログイン") {
                textColor = Color.WHITE
                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                onClick {
                    ui.owner.attemptLogin(user.text.toString(), passwd.text.toString())
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dip(16)
            }
        }
    }
}
