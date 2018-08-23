package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_NORMAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_LOCALRECORD
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout

////////////////////////////////////////////////////////////////////////////////
/*** ローカル記録の表示 ***/
class LocalRecordActivity : AppCompatActivity(), AnkoLogger {
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "$TITLE_LOCALRECORD: $APP_TITLE $APP_NAME"
        LocalRecordActivityUi().setContentView(this)
    }
    /////////////////////////////////////////
    // バックキーではメイン画面に直接戻る
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(intentFor<MainActivity>().clearTop())
        }
        return super.onKeyDown(keyCode, event)
    }
}
////////////////////////////////////////////////////////////////////////////////
/*** UI構成 via Anko ***/
class LocalRecordActivityUi: AnkoComponent<LocalRecordActivity> {
    lateinit var recordList: ListView
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<LocalRecordActivity>) = with(ui) {
        verticalLayout {
            padding = dip(10)
            ////////////////////////////////////////
            // PULLスワイプでデータを再取得してViewを更新
            swipeRefreshLayout {
                onRefresh {
                    doAsync {
                        val recAdapter = LocalRecordAdapter(ui.owner)
                        uiThread {
                            recordList.adapter = recAdapter
                        }
                        isRefreshing = false
                    }
                }
                ////////////////////////////////////////
                recordList = listView {
                    doAsync {
                        val recAdapter = LocalRecordAdapter(ui.owner)
                        uiThread {
                            adapter = recAdapter
                        }
                    }
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class LocalRecordAdapter(val activity: LocalRecordActivity): BaseAdapter() {
    val sk2 = activity.application as Sk2Globals
    private val queue = sk2.localQueue
    ////////////////////////////////////////
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        val item = getItem(i)
        return with(parent!!.context) {
            ////////////////////////////////////////
            verticalLayout {
                ////////////////////////////////////////
                linearLayout {
                    padding = dip(4)
                    ////////////////////////////////////////
                    textView(getWeekDayString(item.datetime)) {
                        textSize = TEXT_SIZE_LARGE
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item.type.toString()) {
                        textSize = TEXT_SIZE_LARGE
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
                        textView("Distance=${item.getDistance(0)} m") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = left; weight = 2f; leftMargin = dip(4) }
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
                        textView("Distance=${item.getDistance(1)} m") {
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
                        textView("Distance=${item.getDistance(2)} m") {
                            textSize = TEXT_SIZE_NORMAL
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
                    }
                }
            }
        }
    }
    ////////////////////////////////////////
    override fun getItem(position: Int): AttendData {
        return queue.getItem(position)
    }
    ////////////////////////////////////////
    override fun getCount(): Int {
        return queue.count()
    }
    ////////////////////////////////////////
    override fun getItemId(position : Int) : Long {
        return position.toLong()
    }
}
