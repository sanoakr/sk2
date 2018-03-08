package jp.ac.ryukoku.st.sk2

import android.content.Context
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import me.mattak.moment.Moment
import org.jetbrains.anko.*
import java.io.*
import java.net.InetSocketAddress
import java.util.*
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class RecordActivity : AppCompatActivity(),AnkoLogger {
    private lateinit var username: String
    private lateinit var key: String
    private val prefName = "st.ryukoku.sk2"

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "出席記録：龍大理工学部出欠システム sk2"
        RecordActivityUi().setContentView(this)

        username = getSharedPreferences(prefName, Context.MODE_PRIVATE).getString("uid", "")
        key = getSharedPreferences(prefName, Context.MODE_PRIVATE).getString("key", "")
    }
    ////////////////////////////////////////
    fun fetchRecord(): List<Map<String, String>> {
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
            val message = "$username,$key"
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
        val keys = listOf("encUid", "type", "datetime", "ssid0", "bssid0", "signal0",
                "ssid1", "bssid1", "signal1", "ssid2", "bssid2", "signal2",
                "ssid3", "bssid3", "signal3", "ssid4", "bssid4", "signal4")
        var record: MutableList<MutableMap<String, String>> = mutableListOf()

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

                        // add weekday?
                        if (keys[i] == "datetime") {
                            var str = vList[i]
                            try {
                                val calendar = Calendar.getInstance()
                                val match = Regex("(\\d+)-(\\d+)-(\\d+)\\s+(\\d+):(\\d+):(\\d+)").find(str)?.groupValues
                                if (match?.size == 7) { // Null makes false
                                    val y = match[1].toInt()
                                    val m = match[2].toInt()
                                    val d = match[3].toInt()
                                    //val th =  match[4].toInt()
                                    //val tm =  match[5].toInt()
                                    //val ts =  match[6].toInt()
                                    calendar.set(y, m, d, 0, 0, 0)
                                    val wday = Moment(calendar.time, TimeZone.getDefault(), Locale.JAPAN).weekdayName
                                    str = vList[i].replace(" ", " $wday ")
                                }
                                else { str = vList[i] }
                            }
                            catch(e: Exception) { str = vList[i] }

                            vMap.put(keys[i], str)
                        }
                        else { vMap.put(keys[i], vList[i]) }
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
    var list : List<Map<String, String>> = activity.fetchRecord()
    ////////////////////////////////////////
    override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
        val item = getItem(i)
        return with(parent!!.context) {
            verticalLayout {
                linearLayout {
                    padding = dip(5)

                    textView(item["type"]) {
                        textSize = 18f
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { width = matchParent; horizontalGravity = left; weight = 1f }

                    textView(item["datetime"]) {
                        textSize = 18f
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }

                linearLayout {
                    padding = dip(3)

                    textView(item["bssid0"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }

                    textView(item["ssid0"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = right }

                    textView("(" + item["signal0"] + "dB)") {
                        textSize = 14f
                    }.lparams { horizontalGravity = right; leftMargin = dip(5) }
                }

                linearLayout {
                    padding = dip(3)

                    textView(item["bssid1"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = left; weight = 1f }

                    textView(item["ssid1"]) {
                        textSize = 14f
                    }.lparams { horizontalGravity = right }

                    textView("(" + item["signal1"] + "dB)") {
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
