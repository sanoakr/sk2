package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.pref
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocketFactory

/** ////////////////////////////////////////////////////////////////////////////// **/
/** サーバ記録用の Fragment **/
class RecordFragment : Fragment() {
    lateinit var recordList: ListView

    companion object {
        const val LISTVIEW = 1
        const val LOCAL = 2
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        /** ////////////////////////////////////////////////////////////////////////////// **/
        return UI {
            relativeLayout {
                padding = dip(4)
                /** ////////////////////////////////////////////////////////////////////////////// **/
                /** PULLスワイプでデータを再取得してViewを更新 **/
                swipeRefreshLayout {
                    onRefresh {
                        doAsync {
                            val recAdapter = RecordAdapter()
                            uiThread {
                                recordList.adapter = recAdapter
                                isRefreshing = false /** Refresh 完了を通知 **/
                            }
                        }
                    }
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    /** データ表示用 ListView **/
                    recordList = listView {
                        id = LISTVIEW
                        doAsync {
                            val recAdapter = RecordAdapter()
                            uiThread {
                                adapter = recAdapter
                            }
                        }
                    }
                }.lparams { above(LOCAL); alignParentStart(); alignParentEnd() }
            }
        }.view
    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/** サーバから出席データを取得する **/
fun fetchRecord(): RecordsData {
    val uid = pref.getString(Sk2Globals.PREF_UID, "")   // ユーザ
    val key = pref.getString(Sk2Globals.PREF_KEY, "")   // 認証キー

    lateinit var rowRecord: String /** データ受信用 **/
    try {
        /** SSL Socket **/
        val sslSocketFactory = SSLSocketFactory.getDefault()
        val sslsocket = sslSocketFactory.createSocket()

        /** SSL Connect with TimeOut **/
        sslsocket.connect(InetSocketAddress(Sk2Globals.SERVER_HOSTNAME, Sk2Globals.SERVER_INFO_PORT), Sk2Globals.SERVER_TIMEOUT_MILLISEC)

        /** 入出力バッファ **/
        val input = sslsocket.inputStream
        val output = sslsocket.outputStream
        val bufReader = BufferedReader(InputStreamReader(input, "UTF-8"))
        val bufWriter = BufferedWriter(OutputStreamWriter(output, "UTF-8"))

        /** Send message **/
        val message = "$uid,$key"
        bufWriter.write(message)
        bufWriter.flush()

        /** Receive message **/
        rowRecord = bufReader.use(BufferedReader::readText)

    } catch (e: Exception) {
        /** Record への代入時の split(',') で分割しないで全て最初の要素(uid)としてエラー文字列を扱う **/
        rowRecord = "Server Error: " + e.toString().replace(',', ' ')
    }
    return RecordsData(rowRecord) /** 改行で分割されて ArrayList<Record> へパース代入 **/
}

/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI **/
class RecordAdapter: BaseAdapter() {
    val list = fetchRecord()

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        val item = getItem(i)
        return with(parent!!.context) {
            /** ////////////////////////////////////////////////////////////////////////////// **/
            verticalLayout {
                /** ////////////////////////////////////////////////////////////////////////////// **/
                linearLayout {
                    padding = dip(4)
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(addWeekday(item.datetime)) {
                        textSize = Sk2Globals.TEXT_SIZE_Large
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(item.type) {
                        textSize = Sk2Globals.TEXT_SIZE_Large
                        //textColor = Color.BLACK
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }
                /** ////////////////////////////////////////////////////////////////////////////// **/
                for (ix in 0..2) {
                    if (! item.hasNull(ix)) {
                        /** ////////////////////////////////////////////////////////////////////////////// **/
                        linearLayout {
                            padding = dip(3)
                            ////////////////////////////////////////
                            textView("Majour=${item.get(ix).first}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(60) }
                            ////////////////////////////////////////
                            textView("Minor=${item.get(ix).second}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(60) }
                            ////////////////////////////////////////
                            textView("Distance=${item.get(ix).third}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
                        }
                    }
                }
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getItem(position: Int): Record {
        return list.get(position)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getCount(): Int {
        return list.count()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}