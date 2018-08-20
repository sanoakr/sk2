package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
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
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(intentFor<MainActivity>().clearTop())
        }
        return super.onKeyDown(keyCode, event)
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
    private val queue = sk2.localQueue
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
                    textView(sk2.getWeekDayString(item.datetime)) {
                        textSize = 14f
                        backgroundColor = Color.WHITE // for Huwai's initAdditionalStyle default Error.
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams { horizontalGravity = left; weight = 1f }
                    ////////////////////////////////////////
                    textView(item.type.toString()) {
                        textSize = 14f
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
                        textView("Distance=${item.getDistance(0)} m") {
                            textSize = 12f
                        }.lparams { horizontalGravity = left; weight = 2f; leftMargin = dip(5) }
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
                        textView("Distance=${item.getDistance(1)} m") {
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
                        textView("Distance=${item.getDistance(2)} m") {
                            textSize = 12f
                        }.lparams { horizontalGravity = right; weight = 2f; leftMargin = dip(5) }
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
