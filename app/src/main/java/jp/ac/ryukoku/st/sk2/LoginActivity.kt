package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.view.Gravity
import android.view.View
import android.widget.Button
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
    private var loginUi = LoginActivityUi()

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = ("ログイン：龍大理工学部出欠システム sk2")
        loginUi.setContentView(this)

        val androidId = getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        toast(androidId)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        val sk2 = this.application as Sk2Globals
        sk2.restoreUserData()

        //loginUi.testBt.visibility = if (sk2.prefMap.getOrDefault("debug", false) as Boolean)
        loginUi.testBt.visibility = if (sk2.prefMap["debug"] as Boolean ?: false)
            View.VISIBLE else View.INVISIBLE

        //val uid = sk2.userMap.getOrDefault("uid", "")
        val uid = sk2.userMap["uid"] ?: ""
        //val gcos = sk2.userMap.getOrDefault("gcos", "")
        //val name = sk2.userMap.getOrDefault("name", "")
        //val time = sk2.userMap.getOrDefault("name", 0L)
        if (uid != "") { startActivity<MainActivity>() }
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
                login(user, result)
            }
        }
    }
    ////////////////////////////////////////
    fun authServer(user: String, passwd: String): String {
        val sk2 = this.application as Sk2Globals
        val serverHost = sk2.serverHost
        val serverPort = sk2.serverPort
        val timeOut = sk2.timeOut
        val authWord = sk2.authWord
        val authFail = sk2.authFail

        lateinit var result: String
        try {
            val sslSocketFactory = SSLSocketFactory.getDefault()
            val sslsocket = sslSocketFactory.createSocket()
            //connect with TimeOut
            sslsocket.connect(InetSocketAddress(serverHost, serverPort), timeOut)

            val input = sslsocket.inputStream
            val output = sslsocket.outputStream
            val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
            val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))
            // Send message
            val message = "$authWord,$user,$passwd"
            bufWriter.write(message)
            bufWriter.flush()
            // Receive message
            result = bufReader.use(BufferedReader::readText)

        } catch (e: Exception) {
            result = authFail
            toast("サーバーに接続できません")
        }
        return result
    }
    ////////////////////////////////////////
    fun login(user: String, result: String) {
        val sk2 = this.application as Sk2Globals
        val authFail = sk2.authFail

        if (result != authFail) {
            val v = result.split(",")
            val time = System.currentTimeMillis()

            val gList = v[1].split(Regex("\\s+")).mapNotNull { it.capitalize() }
            val clrGcos = gList.joinToString(" ")
            val clrName = v[2].replace(Regex("\\s+"), " ")

            sk2.userMap.put("uid", user)
            sk2.userMap.put("key", v[0])
            sk2.userMap.put("gcos", clrGcos)
            sk2.userMap.put("name", clrName)
            sk2.userMap.put("time", time)

            toast("ログインします")
            sk2.saveUserData()

            startActivity<MainActivity>()
        } else {
            toast("ログインに失敗しました")
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class LoginActivityUi: AnkoComponent<LoginActivity> {
    lateinit var testBt: Button
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {
        verticalLayout {
            padding = dip(16)
            ////////////////////////////////////////
            val user = editText {
                hint = "学籍番号ID"
                inputType = TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            ////////////////////////////////////////
            val passwd = editText {
                hint = "パスワード"
                inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }
            ////////////////////////////////////////
            button("ログイン") {
                textColor = Color.WHITE
                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                onClick {
                    ui.owner.attemptLogin(user.text.toString(), passwd.text.toString())
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dip(16)
            }
            ////////////////////////////////////////
            testBt = button("テストログイン") {
                onClick {
                    ui.owner.attemptLogin("testuser", "testuser")
                }
            }.lparams{
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = dip(128)
            }
        }
    }
}
