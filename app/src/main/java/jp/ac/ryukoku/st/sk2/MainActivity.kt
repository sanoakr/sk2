package jp.ac.ryukoku.st.sk2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

////////////////////////////////////////////////////////////////////////////////
class MainActivity : AppCompatActivity() {
    private var mainUi = MainActivityUi()
    private var username = "no user"

    ////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("龍大理工学部出欠システム")
        mainUi.setContentView(this)
    }
    ////////////////////////////////////////
    override fun onResume() {
        super.onResume()

        if (! SetUserInfo(mainUi) ) {
            startActivity(intentFor<LoginActivity>().clearTop())
        }
        if (!wifiManager.isWifiEnabled()) {
            mainUi.attToastText ="無線LANをオンにしてください"
            mainUi.attBtn.background = ContextCompat.getDrawable(ctx, R.drawable.button_disabled)
        } else {
            mainUi.attToastText ="出席！！！"
        }
    }
    ////////////////////////////////////////
    fun SetUserInfo(ui: MainActivityUi): Boolean {
        val prefName = "st.ryukoku.sk2"

        // get uid & set title
        val pref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        username = pref.getString("uid", "no user")
        val gcos = pref.getString("gcos", "")
        val name = pref.getString("name", "")
        val time = pref.getLong("time", 0)

        // check key life
        //val now = System.currentTimeMillis()
        //val over = (now - time) > lifetime

        // go to login if have not
        if ( username.isNullOrEmpty() || username == "no user") {
            return false
        } else {
            ui.userInfo.text = " $username / $gcos / $name"
            return true
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
class MainActivityUi: AnkoComponent<MainActivity> {
    lateinit var attBtn: Button
    lateinit var wifiInfo: TextView
    lateinit var userInfo: TextView
    var attToastText = "出席！！！"

    ////////////////////////////////////////
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        relativeLayout {
            userInfo = textView("") {
                textColor = Color.BLACK
            }.lparams {
                alignParentTop(); centerHorizontally()
                topMargin = dip(5)
            }
            attBtn = button("出席") {
                textColor = Color.WHITE
                textSize = 32f
                background = ContextCompat.getDrawable(ctx, R.drawable.button_states)
                onClick {
                    toast(attToastText)
                    //ui.owner.testfun(ui, "renew text")
                }
            }.lparams {
                centerHorizontally()
                topMargin = dip(50); width = dip(250); height = dip(250)
            }
            verticalLayout {
                wifiInfo = textView("") {
                    textSize = 10f
                }.lparams {
                    bottomMargin = dip(10)
                    width = matchParent; gravity = Gravity.RIGHT
                }

                linearLayout {
                    imageButton {
                        imageResource = R.drawable.ic_settings_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<PreferenceActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(16); width = dip(64); height = dip(64)
                    }

                    imageButton {
                        imageResource = R.drawable.ic_history_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<RecordActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(16); width = dip(64); height = dip(64)
                    }

                    imageButton {
                        imageResource = R.drawable.ic_live_help_32dp
                        background = ContextCompat.getDrawable(context, R.drawable.button_circle)
                        onClick {
                            startActivity<HelpActivity>()
                        }
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL
                        margin = dip(16); width = dip(64); height = dip(64)
                    }

                }
            }.lparams {
                alignParentBottom(); centerHorizontally()
            }
        }
    }
}