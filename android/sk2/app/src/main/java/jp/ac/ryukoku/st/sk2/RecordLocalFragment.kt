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
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TOAST_LOG_NO_RECORDS
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.apNameMap
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
                                if (recAdapter.list.isEmpty())
                                    toast(TOAST_LOG_NO_RECORDS)
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
class RecordLocalAdapter: BaseAdapter(), AnkoLogger {
    val list = Sk2Globals.localQueue

    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        info(list)
        val (datetime, type, beacons) = getItem(i)

        return with(parent!!.context) {
            /** ////////////////////////////////////////////////////////////////////////////// **/
            verticalLayout {
                /** ////////////////////////////////////////////////////////////////////////////// **/
                linearLayout {
                    padding = dip(4)
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(addWeekday(datetime)) {
                        textSize = Sk2Globals.TEXT_SIZE_Large
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    /** ////////////////////////////////////////////////////////////////////////////// **/
                    textView(type.toString()) {
                        textSize = Sk2Globals.TEXT_SIZE_Large
                        //textColor = Color.BLACK
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = right }
                }.lparams { width = matchParent }
                /** ////////////////////////////////////////////////////////////////////////////// **/
                for (ix in 0..2) {
                    if (ix < beacons.count()) {
                        val uuid = beacons[ix].uuid
                        val major = beacons[ix].major
                        val minor = beacons[ix].minor
                        /** ////////////////////////////////////////////////////////////////////////////// **/
                        linearLayout {
                            padding = dip(3)
                            ////////////////////////////////////////
                            textView("Name=${apNameMap[Triple(uuid, major, minor)]}") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(150) }
                            ////////////////////////////////////////
                            textView("Major=$major") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(50) }
                            ////////////////////////////////////////
                            textView("Minor=$minor") {
                                textSize = Sk2Globals.TEXT_SIZE_NORMAL
                            }.lparams { horizontalGravity = left; width = dip(50) }
                            ////////////////////////////////////////
                            //Log.e("CAST to Double from", "${beacons[ix][3]} ${beacons[ix][4]}")
                            val tx: Int = beacons[ix].power
                            val rssi: Int = beacons[ix].rssi
                            val dist = "%.6f".format(getBleDistance(tx, rssi))
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
    override fun getItem(position: Int): Triple<String, Char, List<StatBeacon>> {
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