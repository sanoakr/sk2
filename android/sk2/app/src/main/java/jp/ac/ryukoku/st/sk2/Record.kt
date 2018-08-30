package jp.ac.ryukoku.st.sk2

import org.jetbrains.anko.AnkoLogger
import java.util.ArrayList

/** ////////////////////////////////////////////////////////////////////////////// **/
/** サーバからの個別の出席記録を保持するデータクラス ***/
class Record(): AnkoLogger {
    var uid: String? = null
    var type: String? = null
    var datetime: String? = null // without the day of week
    var data = ArrayList<Pair<ArrayList<Double?>, String?>>() // ((Major, Minor, Distance), Name)

    /** Primary initializer **/
    init { // initialize data array of array  (4 x MAX_SEND_BEACON_NUM) used as Triple
        for (i in 1..Sk2Globals.MAX_SEND_BEACON_NUM) {
            val quad: Pair<ArrayList<Double?>, String?> = Pair(arrayListOf<Double?>(null, null, null), null)
            data.add(quad)
        }
    }

    fun count(): Int = data.count()
    /** i 番目のビーコンの Major, Minor, Distance, Name を Pair<Triple, String> として取得 **/
    fun get(i: Int): Pair<Triple<Int?, Int?, Double?>, String?>
            = Pair(Triple(data[i].first[0]?.toInt(), data[i].first[1]?.toInt(), data[i].first[2]), data[i].second)
    /** i 番目の Major **/
    fun getMajor(i: Int): Int? = data[i].first[0]?.toInt()
    /** i 番目の Minor **/
    fun getMinor(i: Int): Int? = data[i].first[1]?.toInt()
    /** i 番目の Distance **/
    fun getDistance(i: Int): Double? = data[i].first[2]
    /** i 番目の Name **/
    fun getName(i: Int): String? = data[i].second

    fun hasNull(i: Int) = (getMajor(i) == null || getMinor(i) == null
            || getDistance(i) == null) // Name は null でも良い

    /** 文字列として取得 **/
    override fun toString(): String {
        val list = mutableListOf<String>()
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
                        data[n].first[m] = doubleVal
                    else
                        data[n].first[m] = 0.0
                }
            }
        }
    }
}

/** ////////////////////////////////////////////////////////////////////////////// **/
/*** サーバからの全記録を Array<Record> として保持するデータクラス ***/
class RecordsData() {
    private var record = ArrayList<Record>()

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
    fun isEmpty(): Boolean = record.isEmpty()
    /** i 番目のレコード **/
    fun get(i: Int): Record = record[i]
    /** i 番目のレコードの j 番目のBeacon情報 ((Major, Minor, Distance), Name) **/
    fun get(i: Int, j: Int): Pair<Triple<Int?, Int?, Double?>, String?> = get(i).get(j)
}
