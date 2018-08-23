package jp.ac.ryukoku.st.sk2

import android.util.Log
import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.MAX_SEND_BEACON_NUM
import me.mattak.moment.Moment
import org.jetbrains.anko.AnkoLogger
import java.util.*

////////////////////////////////////////////////////////////////////////////////
/*** BLEスキャン結果用データクラス ***/
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
    // ビーコン情報をテキストで
    fun getBeaconsText(label: Boolean = true, time: Boolean = true,
                       uuid: Boolean = true, signal: Boolean = false, ios: Boolean = false): String {
        // ラベル設定
        val uuLabel = if (label) "UUID=" else ","
        val mjLabel = if (label) "\n\tMajor=" else ","
        val mnLabel = if (label) ", Minor=" else ","
        val dsLabel = if (label) ",\n\tDistance=" else ","
        val enLabel = if (label) "\n" else ""

        var beaconText = StringBuilder()
        if (adArray.isNotEmpty()) {
            if (time) // 取得日時を表示？
                beaconText.append(datetime.toString()+"\n")

            adArray.forEach { (b, rssi) ->
                // ビーコンは iBeacon に限定
                if (b is IBeacon) {
                    if (uuid) // UUID を表示？
                        beaconText.append("$uuLabel${b.uuid}")

                    // 主要情報
                    beaconText.append("$mjLabel${b.major}$mnLabel${b.minor}" +
                            "$dsLabel${getBleDistance(b.power, rssi)}$enLabel")

                    if (label) {// ラベル付きのときのみ
                        if (signal)
                            beaconText.append("\tTxPower=${b.power}, RSSI=$rssi\n")
                        if (ios) { //iOS 対応の距離？とRanging を追加
                            val (dist, ranging) = iOSgetBleDistance(b.power, rssi)
                            beaconText.append("\tiOS Distance?=$dist, Ranging=$ranging\n")
                        }

                    }


                }
            }
        }
        return beaconText.toString()
    }
}
////////////////////////////////////////////////////////////////////////////////
/*** サーバからの全記録を Array<Record> として保持するデータクラス ***/
class RecordsData() {
    var record = ArrayList<Record>()

    constructor(data: String): this() {
        // data が空でなければ改行で分割して、それぞれを Record とする
        if (data.isNotBlank()) {
            data.split('\n').forEach { r ->
                record.add(Record(r))
                Log.d("XXX", r)
            }
        }
    }
    fun count(): Int = record.count()
    // i 番目のレコード
    fun get(i: Int): Record = record.get(i)
    // i 番目のレコードの j 番目のBeacon情報 (Major, Minor, Distance)
    fun get(i: Int, j: Int): Triple<Int, Int, Double> = get(i).get(j)
}
////////////////////////////////////////
/** サーバからの個別の出席記録を保持するデータクラス ***/
class Record(): AnkoLogger {
    var uid: String? = null
    var type: String? = null
    var datetime: String? = null // without the day of week
    var data = ArrayList<ArrayList<Double>>() // (Major, Minor, Distance) の Array

    // Primary initializer
    init { // initialize data array of array  (3 x MAX_SEND_BEACON_NUM) used as Triple
        val triple = arrayListOf(0.0, 0.0, 0.0)
        for (i in 1..MAX_SEND_BEACON_NUM)
            data.add(triple)
    }

    fun count(): Int = data.count()
    // i 番目のビーコンの Major, Minor, Distance と Triple として取得
    fun get(i: Int): Triple<Int, Int, Double>
            = Triple(data[i][0].toInt(), data[i][1].toInt(), data[i][2])
    // i 番目の Major
    fun getMajor(i: Int): Int = data[i][0].toInt()
    // i 番目の Minor
    fun getMinor(i: Int): Int = data[i][1].toInt()
    // i 番目の Distance
    fun getDistance(i: Int): Double = data[i][2]

    // Secondary initializer
    // 文字列をパースして保存
    constructor(str: String): this() {
        if (str.isNotBlank()) {
            // ',' で分割
            val dataList = str.split(',')
            // いくつの要素に分割された？
            val dataSize = dataList.count()

            if (dataSize >= 3) {
                uid = dataList[0].trim()      // ユーザID
                type = dataList[1].trim()     // データタイプ A/M
                datetime = dataList[2].trim() // 記録日時

                // 以降のデータは(Major, Minor, Distance)の3組とする
                ////// 半端なところで終わった場合の処理ができていません
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

