package jp.ac.ryukoku.st.sk2

import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import me.mattak.moment.Moment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*
/** ////////////////////////////////////////////////////////////////////////////// **/
/*** FIFO Queue: ローカルログ記録用 ***/
class Queue <T>(list: MutableList<T>, size: Int = 100) {
    private var items: MutableList<T> = list
    private val maxsize = size        // Queue の最大長

    fun isEmpty():Boolean = items.isEmpty()
    fun count():Int = items.count()
    fun getItem(postion: Int):T = items[postion]
    override fun toString() = items.toString()
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun push(element: T){ // 先頭に PUSH
        if (element != null) {
            items.add(0, element)
            if (count() > maxsize) {
                pop()
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun pop(): T? { // 最後から POP
        if (isEmpty()){
            return null
        } else {
            return items.removeAt(items.lastIndex)
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun peek():T?{ // 最後の要素を PEEK (消さない)
        return items.last()
    }
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/*** 出席データ用データクラス ***/
class AttendData(attDatetime: Moment, attType: Char, sArray: ScanArray) {
    var datetime = attDatetime  /** データ提出日時（スキャン日時ではない）**/
    var type = attType            // 'M'anual or 'A'uto
    var scanArray = sArray    // Moment() + ArrayList< Pair<ADStructure, Int> >

    fun count(): Int = scanArray.count()
    fun get(i: Int): Pair<ADStructure, Int>? = scanArray.get(i)
    fun getADStructre(i: Int): ADStructure? = scanArray.get(i)?.first
    fun getRssi(i: Int): Int? = scanArray.get(i)?.second

    fun getUuid(i: Int): UUID? {
        val ads = getADStructre(i) as IBeacon?
        return ads?.uuid
    }
    fun getMajor(i: Int): Int? {
        val ads = getADStructre(i) as IBeacon?
        return ads?.major
    }
    fun getMinor(i: Int): Int? {
        val ads = getADStructre(i) as IBeacon?
        return ads?.minor
    }
    fun getDistance(i: Int): Double? {
        val ads = getADStructre(i) as IBeacon?
        val tx = ads?.power
        val rssi = getRssi(i)
        return if(tx != null && rssi != null)
            getBleDistance(tx, rssi)
        else
            null
    }
}