package jp.ac.ryukoku.st.sk2

import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import me.mattak.moment.Moment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

class Queue <T>(list: MutableList<T>, size: Int = 100): AnkoLogger {
    private var items: MutableList<T> = list
    private val maxsize = size

    fun isEmpty():Boolean = items.isEmpty()
    fun count():Int = items.count()
    fun getItem(postion: Int):T = items[postion]
    fun getList(): MutableList<T> = items

    override fun toString() = items.toString()

    fun push(element: T){
        info(count())
        items.forEach { e ->
            if (e is AttendData) {
                info("AttendData")
            } else
                info("Not an AttendData")
        }
        if (element != null) {
            items.add(0, element)
            if (count() > maxsize) {
                pop()
            }
        }
        //info(count())
    }
    fun pop():T?{
        if (isEmpty()){
            return null
        } else {
            return items.removeAt(items.lastIndex)
        }
    }
    fun peek():T?{
        return items.last()
    }
}
///////////////////////////////////////
class AttendData(attDatetime: Moment, attType: Char, sArray: ScanArray) {
    var datetime = attDatetime
    var type = attType
    var scanArray = sArray

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