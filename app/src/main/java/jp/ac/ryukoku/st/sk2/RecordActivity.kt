package jp.ac.ryukoku.st.sk2

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.BaseAdapter
import org.jetbrains.anko.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class RecordActivity : AppCompatActivity() {
    private lateinit var username: String
    private lateinit var key: String

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "出席記録：龍大理工学部出欠システム sk2"
        RecordActivityUi().setContentView(this)

        username = getSharedPreferences("ryukoku.attend", Context.MODE_PRIVATE).getString("uid", "")
        key = getSharedPreferences("ryukoku.attend", Context.MODE_PRIVATE).getString("key", "")
    }
    ////////////////////////////////////////
    fun fetchRecord(user: String, key: String): List<Map<String, Any>> {
        // sk2 info server
        val serverHost = "sk2.st.ryukoku.ac.jp"
        val serverPort = 4441
        val timeOut = 5000
        lateinit var rowRecord: String

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
            val message = "$user,$key"
            bufWriter.write(message)
            bufWriter.flush()
            // Receive message
            rowRecord = bufReader.use(BufferedReader::readText)

        } catch (e: Exception) {
            toast("サーバーに接続できません")
            rowRecord = ""
        }
        return parseRecord(rowRecord)
    }
    ////////////////////////////////////////
    fun parseRecord(data: String): List<Map<String, Any>> {
        var record = mutableListOf(mutableMapOf<String, String>().withDefault{""})

        if (data.isNullOrBlank()) {
            record.add(Map("enUid" to "There is no record"))
        } else {
            val dataList = data.split('\n')

            try {
                dataList.forEach { r ->
                    val vList = r.split(',')
                    var vMap = mutableMapOf<String, String>().withDefault{""}

                    // vList を value として vMap を生成する
                }


            }

        }
        return record
}

////////////////////////////////////////////////////////////////////////////////
class RecordActivityUi: AnkoComponent<RecordActivity> {
    lateinit var recAdapter: RecordAdapter

    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<RecordActivity>) = with(ui) {
        recAdapter = RecordAdapter(owner)

        verticalLayout {
            relativeLayout {
                textView("出席記録").lparams { centerHorizontally() }
            }
        }

        listView {
            adapter = recAdapter
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
class RecordAdapter(var contex: Context): BaseAdapter() {

}
