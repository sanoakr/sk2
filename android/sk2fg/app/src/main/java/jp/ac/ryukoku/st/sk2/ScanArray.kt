package jp.ac.ryukoku.st.sk2

import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.MainActivity.Companion.MAX_SEND_BEACON_NUM
import me.mattak.moment.Moment
import org.jetbrains.anko.AnkoLogger
import java.lang.Math.pow
import java.util.*

////////////////////////////////////////////////////////////////////////////////
/*** iOS Swift
func calculateNewDistance(txCalibratedPower: Int, rssi: Int) -> Double {
    if rssi == 0 {
        return -1
    }
    let ratio = Double(exactly:rssi)! / Double(txCalibratedPower)

    if ratio < 1.0 {
        return pow(10.0, ratio)
    }
    else {
        let accuracy = 0.89976 * pow(ratio, 7.7095) + 0.111
        return accuracy
    }
}
***/

fun getBleDistance(tx: Int, rssi: Int, n: Double = 2.0): Double {
    return pow(10.0, (tx - rssi) / (n * 10))
}
////////////////////////////////////////////////////////////////////////////////
class ScanArray() {
    var datetime = Moment()
    var adArray = ArrayList<Pair<ADStructure, Int>>()

    constructor(moment: Moment, array: ArrayList<Pair<ADStructure, Int>>): this() {
        datetime = moment
        adArray = array
    }
    fun isEmpty(): Boolean = adArray.isEmpty()
    fun isNotEmpty(): Boolean = adArray.isNotEmpty()

    fun count(): Int = adArray.count()
    fun get(i: Int): Pair<ADStructure, Int>? {
        return when (i < count()) {
            true -> adArray[i]
            false -> null
        }
    }
    fun add(scan: Pair<ADStructure, Int>): Int {
        adArray.add(scan)
        return adArray.size
    }
    fun getBeaconsText(label: Boolean = true, time: Boolean = true,
                       uuid: Boolean = true): String {
        val uuLabel = if (label) "UUID=" else ","
        val mjLabel = if (label) "\n\tMajor=" else ","
        val mnLabel = if (label) ", Minor=" else ","
        val dsLabel = if (label) ",\n\tDistance=" else ","
        val enLabel = if (label) "\n" else ""

        var beaconText = StringBuilder()
        if (adArray.isNotEmpty()) {
            if (time)
                beaconText.append(datetime.toString()+"\n")
            adArray.forEach { (b, rssi) ->
                if (b is IBeacon) {
                    if (uuid)
                        beaconText.append("$uuLabel${b.uuid}")
                    beaconText.append("$mjLabel${b.major}$mnLabel${b.minor}" +
                            "$dsLabel${getBleDistance(b.power, rssi)}$enLabel")
                }
            }
        }
        return beaconText.toString()
    }
}
////////////////////////////////////////////////////////////////////////////////
class RecordsData() {
    var record = ArrayList<Record>()

    constructor(data: String): this() {
        if (data.isNotBlank()) {
            // split echo record
            data.split('\n').forEach { r ->
                record.add(Record(r))
            }
        }
    }
    fun get(i: Int): Record = record.get(i)
    fun get(i: Int, j: Int): Triple<Int, Int, Double> = get(i).get(j)
    fun count(): Int = record.count()
}
////////////////////////////////////////
class Record(): AnkoLogger {
    var uid: String? = null
    var type: String? = null
    var datetime: String? = null // without the day of week
    var data = ArrayList<ArrayList<Double>>()

    init { // initialize data array of array  (3 x MAX_SEND_BEACON_NUM)
        val triple = arrayListOf(0.0, 0.0, 0.0)
        for (i in 1..MAX_SEND_BEACON_NUM)
            data.add(triple)
    }

    fun get(i: Int): Triple<Int, Int, Double>
            = Triple(data[i][0].toInt(), data[i][1].toInt(), data[i][2])
    fun count(): Int = data.count()
    fun getMajor(i: Int): Int = data[i][0].toInt()
    fun getMinor(i: Int): Int = data[i][1].toInt()
    fun getDistance(i: Int): Double = data[i][2]

    constructor(str: String): this() {
        if (str.isNotBlank()) {
            // split each data
            val dataList = str.split(',')
            val dataSize = dataList.count()

            if (dataSize >= 3) {
                uid = dataList[0].trim()
                type = dataList[1].trim()
                datetime = dataList[2].trim()

                for (i in 3..(dataSize - 1)) {
                    val n = i / 3     // round
                    val m = i % 3
                    val doubleVal = dataList[i].toDoubleOrNull()
                    if (doubleVal != null)
                        data[n][m] = doubleVal
                    else
                        data[n][m] = 0.0
                }
            }
        }
    }
}

