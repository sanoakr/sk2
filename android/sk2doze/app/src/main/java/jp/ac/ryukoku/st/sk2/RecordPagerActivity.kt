package jp.ac.ryukoku.st.sk2

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_NAME
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.APP_TITLE
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD_TAB_LOCAL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.TITLE_RECORD_TAB_SERVER
import org.jetbrains.anko.*
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.viewPager

/** ////////////////////////////////////////////////////////////////////////////// **/
class RecordPagerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "$TITLE_RECORD: $APP_TITLE $APP_NAME"

        val recordPagerUi = RecordPagerUi()
        recordPagerUi.setContentView(this)
        recordPagerUi.showPages(supportFragmentManager)
        recordPagerUi.setTabText(listOf(
                TITLE_RECORD_TAB_SERVER,
                TITLE_RECORD_TAB_LOCAL
        ))

    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
class RecordPagerUi : AnkoComponent<RecordPagerActivity> {
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    companion object {
        const val VIEWPAGER = 1
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    override fun createView(ui: AnkoContext<RecordPagerActivity>): View = with(ui) {
        verticalLayout {
            tabLayout = tabLayout {
                tabMode = TabLayout.MODE_FIXED
                tabGravity = TabLayout.GRAVITY_FILL
                //setSelectedTabIndicatorColor(getColor(ctx, R.color.colorPrimary))
            }.lparams(width = matchParent, height = wrapContent)

            viewPager = viewPager {
                id = Companion.VIEWPAGER
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
class RecordPageAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> RecordFragment()
            1 -> RecordFragment()
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
