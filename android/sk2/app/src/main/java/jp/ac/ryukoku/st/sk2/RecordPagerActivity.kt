package jp.ac.ryukoku.st.sk2

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.COLOR_BACKGROUND_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TEXT_SIZE_LARGE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD_TAB_LOCAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD_TAB_SERVER
import org.jetbrains.anko.*
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.viewPager

/** ////////////////////////////////////////////////////////////////////////////// **/
/** TabView 入りの 出席 Records **/
class RecordPagerActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recordPagerUi = RecordPagerUi()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = COLOR_BACKGROUND
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        }
        recordPagerUi.setContentView(this)
        recordPagerUi.showPages(supportFragmentManager)
        /** Tab Title **/
        recordPagerUi.setTabText(listOf(
                TITLE_RECORD_TAB_SERVER,
                TITLE_RECORD_TAB_LOCAL
        ))

    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/** UI **/
class RecordPagerUi : AnkoComponent<RecordPagerActivity> {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    companion object {
        const val VIEWPAGER = 1
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<RecordPagerActivity>): View = with(ui) {
        verticalLayout {
            backgroundColor = COLOR_BACKGROUND
            ////////////////////////////////////////
            textView(TITLE_RECORD) {
                textColor = Color.BLACK
                textSize = TEXT_SIZE_LARGE
                backgroundColor = COLOR_BACKGROUND_TITLE
                topPadding = dip(10); bottomPadding = dip(10)
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams {
                width = matchParent; bottomMargin = dip(10)
            }
            ////////////////////////////////////////
            tabLayout = tabLayout {
                tabMode = TabLayout.MODE_FIXED
                tabGravity = TabLayout.GRAVITY_FILL
                //setSelectedTabIndicatorColor(getColor(ctx, R.color.colorPrimary))
            }.lparams(width = matchParent, height = wrapContent)

            viewPager = viewPager {
                id = VIEWPAGER
                offscreenPageLimit = 2
            }.lparams(width = matchParent, height = matchParent)

            tabLayout.setupWithViewPager(viewPager)
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun showPages(fm: FragmentManager) {
        viewPager.adapter = RecordPageAdapter(fm)
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun setTabText(titles: List<String>) {
        for ((i, v) in titles.withIndex())
            tabLayout.getTabAt(i)?.text = v
    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/** Flagment Adapter **/
class RecordPageAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> RecordFragment()
            1 -> RecordLocalFragment()
            else -> null
        }
    }
    override fun getCount(): Int {
        return 2
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return "Page " + (position + 1)
    }
}
