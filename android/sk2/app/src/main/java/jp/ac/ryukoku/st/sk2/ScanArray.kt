package jp.ac.ryukoku.st.sk2

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.VALID_IBEACON_UUID
import me.mattak.moment.Moment
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.*

/** ////////////////////////////////////////////////////////////////////////////// **/
/** Beacon Data **/
class StatBeacon(u: String, ma: Int, mi: Int, pw: Int, rs: Int) {
    var uuid:String = u
    var major: Int = ma
    var minor: Int = mi
    var power: Int = pw
    var rssi: Int = rs
}
/** ////////////////////////////////////////////////////////////////////////////// **/
/*** BLEスキャン結果用データクラス ***/
class ScanArray() {
    var datetime = Moment()
    private var adArray = ArrayList<Pair<ADStructure, Int>>()

    /** Secondary initializer **/
    constructor(results: List<ScanResult>) : this() {
        datetime = Moment()

        results.forEach { r ->
            val bytes = r.scanRecord?.bytes
            val rssi = r.rssi
            val structures = ADPayloadParser.getInstance().parse(bytes)
            structures.forEach { s ->
                if (s is IBeacon && (s.uuid.toString() in VALID_IBEACON_UUID)) {
                    adArray.add(Pair(s, rssi))
                }
            }
        }
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
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** 統計出力 Rssi の上下を捨てて、TX, Rssi の平均値を計算 **/
    /** Map(UUID -> Map((Major, Minor) -> (Tx, Rssi))) **/
    fun getStatisticalList(): List<StatBeacon> {
        val map = mutableMapOf<String, MutableList<Pair<Int, Int>>>()

        /** Beacon ごとの信号リスト **/
        adArray.forEach { (b, rssi) ->
            if (b is IBeacon) {
                val uuidStr = "${b.uuid},${b.major},${b.minor}"

                if (map.containsKey(uuidStr)) {
                    map[uuidStr]?.add(Pair(b.power, rssi))
                } else {
                    val signalList = mutableListOf(Pair(b.power, rssi))
                    map[uuidStr] = signalList
                }
            }
        }
        /** RSSI の最大と最小を除いて平均 **/
        val list = mutableListOf<StatBeacon>()
        map.forEach { (k, v) ->

            if (v.count() != 0) {

                val (min: Int?, max: Int?) = if (map.count() > 5)
                    Pair(v.minBy { it.second }?.second, v.maxBy { it.second }?.second)
                else
                    Pair(Int.MIN_VALUE, Int.MAX_VALUE)

                var count = 0
                var pAvg: Double = 0.0
                var rAvg: Double = 0.0

                v.forEach { (power, rssi) ->
                    if (rssi != max || rssi != min) {
                        pAvg += power
                        rAvg += rssi
                        count++
                    }
                }
                pAvg /= count; rAvg /= count

                val (uuid, major, minor) = k.split(',')
                list.add(StatBeacon(uuid, major.toInt(), minor.toInt(), pAvg.toInt(), rAvg.toInt()))
            }
        }
        /** sortby distance **/
        list.sortedByDescending { it.rssi }
        return list
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** ビーコン情報をテキストで **/
    fun getBeaconsText(label: Boolean = true, time: Boolean = true, statistic: Boolean = false,
                       uuid: Boolean = true, signal: Boolean = false, ios: Boolean = false,
                       map: MutableMap<Triple<String, Int, Int>, Pair<String, String>> = mutableMapOf()): String {
        /** ラベル設定 **/
        val uuLabel = if (label) "UUID=" else ","
        val mjLabel = if (label) "\n\tMajor=" else ","
        val mnLabel = if (label) ", Minor=" else ","
        val dsLabel = if (label) "\n\tDistance=" else ","
        val enLabel = if (label) "\n" else ""

        val beaconText = StringBuilder()
        if (adArray.isNotEmpty()) {
            /** 取得日時を表示？ **/
            if (time)
                beaconText.append(datetime.toString() + "\n")
            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** 統計処理 **/
            if (statistic) {
                val list = getStatisticalList()
                list.forEach { e ->
                    val mUuid = e.uuid
                    val mMajor = e.major
                    val mMinor = e.minor
                    val mTx = e.power
                    val mRssi = e.rssi

                    if (uuid)
                    /** UUID を表示？ **/
                        beaconText.append("$uuLabel$mUuid")

                    /** 主要情報 **/
                    val distance = "%.6f".format(getBleDistance(mTx, mRssi))
                    beaconText.append("$mjLabel$mMajor$mnLabel$mMinor" +
                            "$dsLabel$distance$enLabel")

                    if (label) {
                        /** ラベル付きのときのみ **/
                        if (map.isNotEmpty())
                            beaconText.append("\tName=${map[Triple(mUuid, mMajor, mMinor)]?.first}\n")
                        if (signal)
                            beaconText.append("\tTxPower=$mTx, RSSI=$mRssi\n")
                        if (ios) {
                            /** iOS 対応の距離？とRanging を追加 **/
                            val (dist, ranging) = iOSgetBleDistance(mTx, mRssi)
                            beaconText.append("\tiOS Distance?=${"%.6f".format(dist)}," +
                                    "\n\tiOS Ranging=$ranging\n")
                        }
                    }
                }

            }
            /** ////////////////////////////////////////////////////////////////////////////// **/
            /** そのまま **/
            else {
                adArray.forEach { (b, rssi) ->
                    /** ビーコンは iBeacon に限定 **/
                    if (b is IBeacon) {
                        if (uuid) /** UUID を表示？ **/
                            beaconText.append("$uuLabel${b.uuid}")

                        /** 主要情報 **/
                        val distance = "%.6f".format(getBleDistance(b.power, rssi))
                        beaconText.append("$mjLabel${b.major}$mnLabel${b.minor}" +
                                "$dsLabel$distance$enLabel")

                        if (label) {/** ラベル付きのときのみ **/
                            if (map.isNotEmpty())
                                beaconText.append("\tName=${map[Triple(b.uuid.toString(), b.major, b.minor)]?.first}\n")
                            if (signal)
                                beaconText.append("\tTxPower=${b.power}, RSSI=$rssi\n")
                            if (ios) {
                                /** iOS 対応の距離？とRanging を追加 **/
                                val (dist, ranging) = iOSgetBleDistance(b.power, rssi)
                                beaconText.append("\tiOS Distance?=${"%.6f".format(dist)}" +
                                        "\n\tiOS Ranging=$ranging\n")
                            }

                        }
                    }
                }
            }
        }
        return beaconText.toString()
    }
}
