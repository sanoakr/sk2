package jp.ac.ryukoku.st.sk2

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import java.io.*
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class RecordActivity : AppCompatActivity(),AnkoLogger {
    lateinit var sk2: Sk2Globals
    lateinit var pref: SharedPreferences

    private var recordUi = RecordActivityUi()
    lateinit var mSwipe: SwipeRefreshLayout
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sk2 = this.application as Sk2Globals
        pref = sk2.pref
        title = "記録：${sk2.app_title} ${sk2.app_name}"

        recordUi.setContentView(this)

    }
    ////////////////////////////////////////
    fun fetchRecord(): RecordsData {
        val serverHost = sk2.serverHost
        val serverPort = sk2.serverInfoPort
        val timeOut = sk2.timeOut
        val uid = pref.getString("uid", "")
        val key = pref.getString("key", "")

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
        return RecordsData(rowRecord)
    }
}
////////////////////////////////////////////////////////////////////////////////
class RecordActivityUi: AnkoComponent<RecordActivity> {
    lateinit var recordList: ListView
    lateinit var localBt: Button
    val LISTVIEW = 1; val LOCAL = 2
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<RecordActivity>) = with(ui) {
        relativeLayout {
            padding = dip(8)
            ////////////////////////////////////////
            swipeRefreshLayout {
                onRefresh {
                    doAsync {
                        val recAdapter = RecordAdapter(ui.owner)
                        uiThread {
                            recordList.adapter = recAdapter
                        }
                        isRefreshing = false
                    }
                }
                ////////////////////////////////////////
                recordList = listView {
                    id = LISTVIEW
                    doAsync {
                        val recAdapter = RecordAdapter(ui.owner)
                        uiThread {
                            adapter = recAdapter
                        }
                    }
                }
            }.lparams { above(LOCAL); alignParentStart(); alignParentEnd() }
            ////////////////////////////////////////
            localBt = button("Local Records") {
                id = LOCAL
                onClick {
                    startActivity<LocalRecordActivity>()
                }
            }.lparams { alignParentBottom(); alignParentStart(); alignParentEnd() }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class RecordAdapter(var activity: RecordActivity): BaseAdapter() {
    val sk2 = activity.application as Sk2Globals
    val list = activity.fetchRecord()
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
                    textView(addWeekday(item.datetime)) {
                        textSize = 14f
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item.type) {
                        textSize = 14f
                        //textColor = Color.BLACK
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }
                if (item.count() > 0) {
                    ////////////////////////////////////////
                    linearLayout {
                        padding = dip(3)
                        ////////////////////////////////////////
                        textView("Majour=${item.getMajor(0)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(0)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(0)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(5) }
                    }
                }
                if (item.count() > 1) {
                    ////////////////////////////////////////
                    linearLayout {
                        padding = dip(3)
                        ////////////////////////////////////////
                        textView("Majour=${item.getMajor(1)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(1)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(1)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(5) }
                    }
                }
                if (item.count() > 2) {
                    ////////////////////////////////////////
                    linearLayout {
                        padding = dip(3)
                        ////////////////////////////////////////
                        textView("Majour=${item.getMajor(2)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(2)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(2)}") {
                            textSize = 12f
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(5) }
                    }
                }
            }
        }
    }
    ////////////////////////////////////////
    override fun getItem(position : Int): Record {
        return list.get(position)
    }
    ////////////////////////////////////////
    override fun getCount() : Int {
        return list.count()
    }
    ////////////////////////////////////////
    override fun getItemId(position : Int) : Long {
        return position.toLong()
    }
}
