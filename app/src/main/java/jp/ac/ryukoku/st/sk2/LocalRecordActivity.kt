package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
class LocalRecordActivity : AppCompatActivity(), AnkoLogger {
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "ローカル記録：${sk2.app_title} ${sk2.app_name}"
        LocalRecordActivityUi().setContentView(this)
    }
    ////////////////////////////////////////
    fun parseQueue(qdata: List<String>): List<Map<String, String>> {
        val keys = listOf("user", "type", "datetime",
                "major0", "minor0", "dist0",
                "major1", "minor1", "dist1", "major2", "minor2", "dist2",
                "major3", "minor3", "dist3", "major4", "minor4", "dist4")
        val record: MutableList<MutableMap<String, String>> = mutableListOf()

        qdata.forEach { r ->
            val vMap = mutableMapOf<String, String>().withDefault { "" }
            val vList = r.split(',')

            val keySize = keys.size
            for (i in vList.indices) {
                if (!(i < keySize)) break
                var value = vList[i]
                if (keys[i] == "datetime") {
                    val sk2 = this.application as Sk2Globals
                    value = sk2.addWeekday(vList[i])
                }
                vMap.put(keys[i], value)
            }
            record.add(vMap)
        }
        return record
    }
}
////////////////////////////////////////////////////////////////////////////////
class LocalRecordActivityUi: AnkoComponent<LocalRecordActivity> {
    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<LocalRecordActivity>) = with(ui) {
        verticalLayout {
            padding = dip(10)
            ////////////////////////////////////////
            listView {
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
////////////////////////////////////////////////////////////////////////////////
class LocalRecordAdapter(val activity: LocalRecordActivity): BaseAdapter() {
    val sk2 = activity.application as Sk2Globals
    val list: List<Map<String, String>> = activity.parseQueue(sk2.localQueue.getList())
    ////////////////////////////////////////
    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
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
                    textView(item["majou1"]) {
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
                    }.lparams { horizontalGravity = right; weight = 1f }
                    ////////////////////////////////////////
                    textView("(" + item["dist2"] + " m)") {
                        textSize = 14f
                    }.lparams { horizontalGravity = right; leftMargin = dip(5) }
                }
            }
        }
    }
    ////////////////////////////////////////
    override fun getItem(position: Int): Map<String, String> {
        return list.get(position)
    }
    ////////////////////////////////////////
    override fun getCount(): Int {
        return list.count()
    }
    ////////////////////////////////////////
    override fun getItemId(position : Int) : Long {
        return 0L
    }
}
