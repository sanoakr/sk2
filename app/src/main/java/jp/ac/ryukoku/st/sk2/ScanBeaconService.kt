package jp.ac.ryukoku.st.sk2

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap
import org.jetbrains.anko.*

////////////////////////////////////////////////////////////////////////////////
class ScanBeaconService : Service(), BootstrapNotifier, AnkoLogger {
    ////////////////////////////////////////
    private lateinit var bgPowerSaver: BackgroundPowerSaver
    private var btManager: BeaconManager? = null

    companion object {
        val UNQID = "sk2"
        val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        //val ALTBEACON_FORMAT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        //val EDDYSTONE_FORMAT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
        //val UUID = "6F42B781-D072-4E20-A07A-53F6A1C7CB59"
    }
    ////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        bgPowerSaver = BackgroundPowerSaver(this)

        val sk2 = this.application as Sk2Globals
        val interval = sk2.beaconIntervalSec * 1000 // msec
        try {
            btManager = BeaconManager.getInstanceForApplication(this)
            btManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
            btManager?.foregroundBetweenScanPeriod = interval
            btManager?.backgroundBetweenScanPeriod = interval
            //val identifier = Identifier.parse(UUID)
            val region = Region(UNQID, null, null, null)
            RegionBootstrap(this, region)

            btManager?.addRangeNotifier { beacons, _ ->
                for (beacon in beacons) {
                    info("UUID:${beacon.id1}, major:${beacon.id2}, minor:${beacon.id3}, " +
                            "Distance:${beacon.distance} meters, RSSI:${beacon.rssi}, TxPower:${beacon.txPower}")
                }
            }
        } catch (e: RemoteException ) { e.printStackTrace() }
    }
    ////////////////////////////////////////
    private fun sendMessage(msg: String) {
        val broadcast = Intent()
        broadcast.putExtra("btmessage", msg)
        broadcast.action = "BEACON"
        baseContext.sendBroadcast(broadcast)
    }
    ////////////////////////////////////////
    override fun didEnterRegion(region: Region) {
        toast("ビーコン領域に入りました")
        sendMessage("ENTER iBeacon Region")
        startActivity<MainActivity>()
        try {
            btManager?.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    ////////////////////////////////////////
    override fun didExitRegion(region: Region) {
        toast("ビーコン領域から出ました")
        try {
            btManager?.stopRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    ////////////////////////////////////////
    override fun didDetermineStateForRegion(i: Int, region: Region) {
        info("Determine State: ${Integer.toString(i)}")
    }
    ////////////////////////////////////////////////////////////////////////////////
    override fun onBind(p0: Intent?): IBinder? {
        info("on Bind")
        return null
    }
    ////////////////////////////////////////
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("on StartCommand")
        return super.onStartCommand(intent, flags, startId)
    }
    ////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        info("on Destroy")
        stopSelf()
    }
}
