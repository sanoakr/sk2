package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.*
import java.io.*
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class RecordActivity : AppCompatActivity(),AnkoLogger {
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "記録：${sk2.app_title} ${sk2.app_name}"
        RecordActivityUi().setContentView(this)
    }
    ////////////////////////////////////////
    fun fetchRecord(): List<Map<String, String>> {
        val sk2 = this.application as Sk2Globals
        val serverHost = sk2.serverHost
        val serverPort = sk2.serverInfoPort
        val timeOut = sk2.timeOut
        val uid = sk2.userMap["uid"] ?: ""
        val key = sk2.userMap["key"] ?: ""

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
            val message = "$uid,$key"
            bufWriter.write(message)
            bufWriter.flush()
            // Receive message
            rowRecord = bufReader.use(BufferedReader::readText)

        } catch (e: Exception) {
            toast("サーバーに接続できません")
            rowRecord = ",Server Error: " + e.toString()
        }
        return parseRecord(rowRecord)
    }
    ////////////////////////////////////////
    fun parseRecord(data: String): List<Map<String, String>> {
        val keys = listOf("encUid", "type", "datetime",
                "major0", "minor0", "dist0",
                "major1", "minor1", "dist1", "major2", "minor2", "dist2",
                "major3", "minor3", "dist3", "major4", "minor4", "dist4")
        val record: MutableList<MutableMap<String, String>> = mutableListOf()

        if (data.isNullOrBlank()) {
            record.add(mutableMapOf("enUid" to "There is no record"))
        } else {
            val dataList = data.split('\n')

            dataList.forEach { r ->
                if (! r.isNullOrEmpty()) {
                    var vMap = mutableMapOf<String, String>().withDefault { "" }
                    val vList = r.split(',')

                    val keySize = keys.size
                    for (i in vList.indices) {
                        if (!(i < keySize)) break

                        if (keys[i] == "datetime") {
                            val sk2 = this.application as Sk2Globals
                            vMap.put(keys[i], sk2.addWeekday(vList[i]))
                        }
                    }
                    record.add(vMap)
                }
            }
        }
        return record
    }
}
////////////////////////////////////////////////////////////////////////////////
class RecordActivityUi: AnkoComponent<RecordActivity> {
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<RecordActivity>) = with(ui) {
        verticalLayout {
            padding = dip(10)
            ////////////////////////////////////////
            listView {
                doAsync {
                    val recAdapter = RecordAdapter(ui.owner)
                    uiThread {
                        adapter = recAdapter
                    }
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class RecordAdapter(var activity: RecordActivity): BaseAdapter() {
    val list : List<Map<String, String>> = activity.fetchRecord()
    ////////////////////////////////////////
    override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
        val item = getItem(i)
        return with(parent!!.context) {
            ////////////////////////////////////////
            verticalLayout {
                ////////////////////////////////////////
                linearLayout {
                    padding = dip(5)
                    ////////////////////////////////////////
                    textView(item["type"]) {
                        textSize = 18f
                        //textColor = Color.BLACK
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { width = matchParent; horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item["datetime"]) {
                        textSize = 18f
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }
                ////////////////////////////////////////
                linearLayout {
                    padding = dip(3)
                    ////////////////////////////////////////
                    textView(item["major0"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item["minor0"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView("(" + item["dist0"] + " m)") {
                        textSize = 14f
                    }.lparams { horizontalGravity = right; leftMargin = dip(5) }
                }
                ////////////////////////////////////////
                linearLayout {
                    padding = dip(3)
                    ////////////////////////////////////////
                    textView(item["major1"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item["minor1"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView("(" + item["dist1"] + " m)") {
                        textSize = 14f
                    }.lparams { horizontalGravity = right; leftMargin = dip(5) }
                }
                ////////////////////////////////////////
                linearLayout {
                    padding = dip(3)
                    ////////////////////////////////////////
                    textView(item["major2"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item["minor2"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView("(" + item["dist2"] + " m)") {
                        textSize = 14f
                    }.lparams { horizontalGravity = right; leftMargin = dip(5) }
                }
            }
        }
    }
    ////////////////////////////////////////
    override fun getItem(position : Int) : Map<String, String> {
        return list.get(position)
    }
    ////////////////////////////////////////
    override fun getCount() : Int {
        return list.size
    }
    ////////////////////////////////////////
    override fun getItemId(position : Int) : Long {
        return 0L
    }
}
