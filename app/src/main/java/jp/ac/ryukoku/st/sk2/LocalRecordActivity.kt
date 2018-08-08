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
class LocalRecordActivity : AppCompatActivity(),AnkoLogger {
    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sk2 = this.application as Sk2Globals
        title = "ローカル記録：${sk2.app_title} ${sk2.app_name}"
        LocalRecordActivityUi().setContentView(this)
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
    val list = (activity.application as Sk2Globals).localQueue
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
                    ////////////////////////////////////////
                    textView(" / ${item["weekday"]}") {
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
    override fun getItem(position: Int): MutableMap<String, String> {
        return list.getItem(position)
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
