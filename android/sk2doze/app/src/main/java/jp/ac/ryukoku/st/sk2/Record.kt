package jp.ac.ryukoku.st.sk2

import org.jetbrains.anko.AnkoLogger
import java.util.ArrayList

/** ////////////////////////////////////////////////////////////////////////////// **/
/** サーバからの個別の出席記録を保持するデータクラス ***/
class Record(): AnkoLogger {
    var uid: String? = null
    var type: String? = null
    var datetime: String? = null // without the day of week
    var data = ArrayList<ArrayList<Double?>>() // (Major, Minor, Distance) の Array

    /** Primary initializer **/
    init { // initialize data array of array  (3 x MAX_SEND_BEACON_NUM) used as Triple
        for (i in 1..Sk2Globals.MAX_SEND_BEACON_NUM) {
            val triple = arrayListOf<Double?>(null, null, null)
            data.add(triple)
        }
    }

    fun count(): Int = data.count()
    /** i 番目のビーコンの Major, Minor, Distance と Triple として取得 **/
    fun get(i: Int): Triple<Int?, Int?, Double?>
            = Triple(data[i][0]?.toInt(), data[i][1]?.toInt(), data[i][2])
    /** i 番目の Major **/
    fun getMajor(i: Int): Int? = data[i][0]?.toInt()
    /** i 番目の Minor **/
    fun getMinor(i: Int): Int? = data[i][1]?.toInt()
    /** i 番目の Distance **/
    fun getDistance(i: Int): Double? = data[i][2]

    fun hasNull(i: Int) = (getMajor(i) == null || getMinor(i) == null || getDistance(i) == null)

    /** 文字列として取得 **/
    override fun toString(): String {
        var list = mutableListOf<String>()
        list.add(uid.toString()); list.add(type.toString()); list.add(datetime.toString())
        data.forEach { d -> list.add(d.toString()) }
        return data.joinToString()
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /** Secondary initializer **/
    constructor(str: String): this() {    /** 文字列をパースして保存 **/
        if (str.isNotBlank()) {
            // ',' で分割
            val dataList = str.split(',')
            // いくつの要素に分割された？
            val dataSize = dataList.count()

            if (dataSize >= 3) {
                uid = dataList[0].trim()      // ユーザID
                type = dataList[1].trim()     // データタイプ A/M
                datetime = dataList[2].trim() // 記録日時
                //warn("uid="+uid)
                //warn("type="+type)
                //warn("date="+datetime)
                // 以降のデータは(Major, Minor, Distance)の3組とする
                ////// 半端なところで終わった場合の処理ができていません
                for (i in 3..(dataSize - 1)) {
                    val n = i / 3 - 1    // round
                    val m = i % 3
                    val doubleVal = dataList[i].toDoubleOrNull()
                    //warn("$i $n $m $doubleVal")
                    if (doubleVal != null)
                        data[n][m] = doubleVal
                    else
                        data[n][m] = 0.0
                }
            }
        }
    }
}

/** ////////////////////////////////////////////////////////////////////////////// **/
/*** サーバからの全記録を Array<Record> として保持するデータクラス ***/
class RecordsData() {
    var record = ArrayList<Record>()

    /** Secondary initializer **/
    constructor(data: String): this() {
        // data が空でなければ改行で分割して、それぞれを Record とする
        if (data.isNotBlank()) {
            data.split('\n').forEach { r ->
                //Log.d("Split", r)
                record.add(Record(r))
            }
        }
    }
    fun count(): Int = record.count()
    /** i 番目のレコード **/
    fun get(i: Int): Record = record.get(i)
    /** i 番目のレコードの j 番目のBeacon情報 (Major, Minor, Distance) **/
    fun get(i: Int, j: Int): Triple<Int?, Int?, Double?> = get(i).get(j)
}
