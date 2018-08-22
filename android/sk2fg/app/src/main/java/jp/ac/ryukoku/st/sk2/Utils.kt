package jp.ac.ryukoku.st.sk2

import com.neovisionaries.bluetooth.ble.advertising.ADStructure
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import me.mattak.moment.Moment
import java.lang.Math.pow
import java.util.*

////////////////////////////////////////
fun getWeekDayString(moment: Moment): String {
    return moment.format("yyyy-MM-dd E HH:mm:ss ZZZZ")
}
////////////////////////////////////////
fun addWeekday(dt: String?): String {
    var dwt = if (dt != null) dt else ""

    try {
        val calendar = Calendar.getInstance()
        val match = Regex("(\\d+)-(\\d+)-(\\d+)\\s+(\\d+):(\\d+):(\\d+)").find(dwt)?.groupValues
        if (match?.size == 7) { // Null makes false
            val y = match[1].toInt()
            val m = match[2].toInt()-1
            val d = match[3].toInt()
            calendar.set(y, m, d, 0, 0, 0)
            val wday = Moment(calendar.time, TimeZone.getDefault(), Locale.JAPAN).weekdayName
            dwt = dwt.replace(" ", " $wday ")
        }
    }
    catch(e: Exception) { }

    return dwt
}
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
