package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.localQueue
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout

/** ////////////////////////////////////////////////////////////////////////////// **/
/** ローカル記録用の Fragment **/
class RecordLocalFragment : Fragment() {
    private lateinit var recordList: ListView

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
                            val recAdapter = RecordLocalAdapter()
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
                            val recAdapter = RecordLocalAdapter()
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
/** UI **/
class RecordLocalAdapter: BaseAdapter() {
    val list = Sk2Globals.localQueue

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        val (datetime, type, beacons) = getItem(i)

        return with(parent!!.context) {
            /** ////////////////////////////////////////////////////////////////////////////// **/
            verticalLayout {
                /** ////////////////////////////////////////////////////////////////////////////// **/
                linearLayout {
                    padding = dip(4)
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(addWeekday(datetime)) {
                        textSize = Sk2Globals.TEXT_SIZE_LARGE
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(type.toString()) {
                        textSize = Sk2Globals.TEXT_SIZE_LARGE
                        //textColor = Color.BLACK
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }
                /** ////////////////////////////////////////////////////////////////////////////// **/
                for (ix in 0..2) {
                    if (ix < beacons.count() &&  beacons[ix].count() == beacons[ix].filterNotNull().count() ) {
                        /** ////////////////////////////////////////////////////////////////////////////// **/
                        linearLayout {
                            padding = dip(3)
                            ////////////////////////////////////////
                            textView("Majour=${beacons[ix][1]}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(60) }
                            ////////////////////////////////////////
                            textView("Minor=${beacons[ix][2]}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(60) }
                            ////////////////////////////////////////
                            val tx = beacons[ix][3] as Double; val rssi = beacons[ix][4] as Double
                            val dist = getBleDistance(tx.toInt(), rssi.toInt())
                            textView("Distance=$dist") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(4) }
                        }
                    }
                }
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getItem(position: Int): Triple<String, Char, List<List<Any>>> {
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