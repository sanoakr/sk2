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
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_KEY
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.PREF_UID
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_HOSTNAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_INFO_PORT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_PORT
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.SERVER_TIMEOUT_MILLISEC
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_CANT_CONNECT_SERVER
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import java.io.*
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

////////////////////////////////////////////////////////////////////////////////
class RecordActivity : AppCompatActivity() {

    private var recordUi = RecordActivityUi()
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "$TITLE_RECORD: $APP_TITLE $APP_NAME"
        recordUi.setContentView(this)
    }
    ////////////////////////////////////////
    // サーバから出席データを取得する
    fun fetchRecord(): RecordsData {
        val pref = (this.application as Sk2Globals).pref
        val uid = pref.getString(PREF_UID, "")   // ユーザ
        val key = pref.getString(PREF_KEY, "")   // 認証キー

        // データ受信用
        lateinit var rowRecord: String
        try {
            // SSL Socket
            val sslSocketFactory = SSLSocketFactory.getDefault()
            val sslsocket = sslSocketFactory.createSocket()

            // SSL Connect with TimeOut
            sslsocket.connect(InetSocketAddress(SERVER_HOSTNAME, SERVER_INFO_PORT), SERVER_TIMEOUT_MILLISEC)

            // 入出力バッファ
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

            // Record への代入時の split(',') で分割しないで全て最初の要素(uid)としてエラー文字列を扱う
            rowRecord = "Server Error: " + e.toString().replace(',', ' ')
            toast(TOAST_CANT_CONNECT_SERVER)
        }
        return RecordsData(rowRecord) // 改行で分割されて ArrayList<Record> へパース代入
    }
}


////////////////////////////////////////////////////////////////////////////////
// UI構成 via Anko
class RecordActivityUi: AnkoComponent<RecordActivity> {
    lateinit var recordList: ListView
    lateinit var localBt: Button

    companion object {
        const val LISTVIEW = 1
        const val LOCAL = 2
        const val CLEAR = 3

        const val BUTTON_TEXT_LOCAL_RECORD = "Local Record"
        const val BUTTON_TEXT_CLEAR_LOCAL_QUEUE = "Clear Local Queue Data"
    }
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<RecordActivity>) = with(ui) {
        val sk2 = ui.owner.application as Sk2Globals
        relativeLayout {
            padding = dip(4)
            ////////////////////////////////////////
            // PULLスワイプでデータを再取得してViewを更新
            swipeRefreshLayout {
                onRefresh {
                    doAsync {
                        val recAdapter = RecordAdapter(ui.owner)
                        uiThread {
                            recordList.adapter = recAdapter
                            isRefreshing = false // Refresh 完了を通知
                        }
                    }
                }
                ////////////////////////////////////////
                // データ表示用 ListView
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
            // ローカル記録へ
            localBt = button(BUTTON_TEXT_LOCAL_RECORD) {
                id = LOCAL
                textSize = TEXT_SIZE_NORMAL
                onClick {
                    startActivity<LocalRecordActivity>()
                }
            }.lparams { above(CLEAR); alignParentStart(); alignParentEnd() }
            ////////////////////////////////////////
            // ローカル記録のキューをクリアする
            button(BUTTON_TEXT_CLEAR_LOCAL_QUEUE) {
                id = CLEAR
                textSize = TEXT_SIZE_NORMAL
                onClick {
                    sk2.localQueue = Queue<AttendData>(mutableListOf())
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
                    padding = dip(4)
                    ////////////////////////////////////////
                    textView(addWeekday(item.datetime)) {
                        textSize = TEXT_SIZE_LARGE
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item.type) {
                        textSize = TEXT_SIZE_LARGE
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
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(0)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(0)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
                    }
                }
                if (item.count() > 1) {
                    ////////////////////////////////////////
                    linearLayout {
                        padding = dip(3)
                        ////////////////////////////////////////
                        textView("Majour=${item.getMajor(1)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(1)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(1)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
                    }
                }
                if (item.count() > 2) {
                    ////////////////////////////////////////
                    linearLayout {
                        padding = dip(3)
                        ////////////////////////////////////////
                        textView("Majour=${item.getMajor(2)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Minor=${item.getMinor(2)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 1f }
                        ////////////////////////////////////////
                        textView("Distance=${item.getDistance(2)}") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
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
