//
//  ViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
import NetworkExtension
import SystemConfiguration.CaptiveNetwork
import CoreLocation
import AudioToolbox

class ViewController: UIViewController, CLLocationManagerDelegate {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	var Connection = Connection3()
	var timerPostInterval = Timer()
	var backgroundTaskIdentifier: UIBackgroundTaskIdentifier = 0
	
	// UI
	var logViewBtn: UIBarButtonItem!
	var leftBarButton: UIBarButtonItem!
	var rightBarButton: UIBarButtonItem!
	var attendBtn: UIButton!
	var debugText : UITextView!
	var labelUser : UILabel!
	var labelBeacon : UILabel!
	
	// iBeacon
	var myLocationManager : CLLocationManager!
	var myBeaconRegion : CLBeaconRegion!
	var beaconUuids : NSMutableArray!   //iBeaconのUUID配列
	var beaconDetails : Array<String>!  //iBeaconの値配列
	var beaconFlg = false   //iBeaconの取得フラグ
	var debugMode:Int = 0   //デバッグモードのフラグ
	
	// UserDefaultの生成
	let myUserDefault:UserDefaults = UserDefaults()
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		// デバイスの固有ID取得
//		print("DeviceID: \(String(describing: UIDevice.current.identifierForVendor))")
		
		// UI定義
		let btnWidth = Int(self.view.frame.width / 3)
		
		// 登録されているUserDefaultから設定値を呼び出す
		let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
		let user = myUserDefault.string(forKey: "user")
		let engName = myUserDefault.string(forKey: "engName")
		let jpnName = myUserDefault.string(forKey: "jpnName")
		//        let key = myUserDefault.string(forKey: "key")
		let userInfo = user! + " / " + engName! + " / " + jpnName!
		
		// 検証用ユーザーの場合はdebugモードにする
		if(user == appDelegate.debugUser) {
			debugMode = 1
		}
		self.view.backgroundColor = appDelegate.backgroundColor // 背景色をセット
		
		// --------------------------------------------------------------------------------------------------------------------------
		// userを生成
		labelUser = UILabel(frame: CGRect(x:0, y:70, width:self.view.frame.width, height:30))
		labelUser.font = UIFont.systemFont(ofSize: 14.0)    //フォントサイズ
		labelUser.textAlignment = NSTextAlignment.center    // センター寄せ
		labelUser.text = userInfo
		//        labelUser.backgroundColor = UIColor.red
		view.addSubview(labelUser)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// debugTextを生成
		debugText = UITextView(frame: CGRect(x:10, y:self.view.frame.height / 2 - 180, width:self.view.frame.width - 20, height:160))
		debugText.font = UIFont.systemFont(ofSize: 14.0)    //フォントサイズ
		debugText.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1)    // 背景色
		debugText.isEditable = false    // 編集不可
		debugText.textAlignment = NSTextAlignment.left    // センター寄せ
		
		// デバッグモードの場合
		if(debugMode == 1) {
			view.addSubview(debugText)
		} else {
			debugText.removeFromSuperview()
		}
		
		// 自動送信機能をN分ごとに実行
		self.timerPostInterval = Timer.scheduledTimer(timeInterval: TimeInterval(appDelegate.postInterval), target: self, selector: #selector(ViewController.autoUpdate(timer:)), userInfo: nil, repeats: true)
		
		// ナビゲーション
		self.navigationItem.title = appDelegate.appTitle
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 自動送信トグルスイッチを生成
		let SWautoSender: UISwitch = UISwitch()
		SWautoSender.layer.position = CGPoint(x: self.view.bounds.width - 50, y: 125)
		if(autoSender == 0) {
			SWautoSender.isOn = false    // SwitchをOffに設定
		} else if( autoSender == 1 ) {
			SWautoSender.isOn = true    // SwitchをOnに設定
		}
		
		// SwitchのOn/Off切り替わりの際に、呼ばれるイベントを設定する.
		SWautoSender.addTarget(self, action: #selector(ViewController.onClickSWautoSender(sender:)), for: UIControlEvents.valueChanged)
		self.view.addSubview(SWautoSender)  // SwitchをViewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 出席ボタンを設置
		attendBtn = UIButton(frame: CGRect(x: (self.view.frame.width/2 - 100),y: (self.view.frame.height - 300),width: 200,height:200))
		attendBtn.addTarget(self, action: #selector(ViewController.sendAttend(sender:)), for: .touchUpInside)
		attendBtn.titleLabel?.lineBreakMode = .byWordWrapping
		attendBtn.titleLabel?.numberOfLines = 0
		attendBtn.titleLabel?.textAlignment = NSTextAlignment.center
		attendBtn.titleLabel?.font = UIFont.systemFont(ofSize: 25)
		attendBtn.layer.cornerRadius = attendBtn.frame.size.width * 0.5 //丸まり
		attendBtn.layer.shadowOffset = CGSize(width: 0, height: 20 )
		attendBtn.layer.shadowColor = UIColor.black.cgColor
		attendBtn.layer.shadowRadius = 10
		attendBtn.layer.shadowOpacity = 0.3
		btnDisable()
		view.addSubview(attendBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ログボタンを設置
		let logViewBtn = UIButton(
			frame: CGRect(
				x: 0,
				y: Int(self.view.frame.height - 50),
				width: btnWidth,
				height: 50
		))
		
		logViewBtn.setTitle("ログ表示", for: .normal)  //タイトル
		logViewBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		logViewBtn.backgroundColor = appDelegate.ifNormalColor  //色
		logViewBtn.addTarget(self, action: #selector(ViewController.logView(_:)), for: .touchUpInside)
		view.addSubview(logViewBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ヘルプボタンを設置
		let helpViewBtn = UIButton(
			frame: CGRect(
				x: btnWidth,
				y: Int(self.view.frame.height - 50),
				width: btnWidth,
				height: 50
		))
		helpViewBtn.setTitle("ヘルプ", for: .normal)  //タイトル
		helpViewBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		helpViewBtn.backgroundColor = appDelegate.ifNormalColor  //色
		helpViewBtn.addTarget(self, action: #selector(ViewController.helpView(_:)), for: .touchUpInside)
		view.addSubview(helpViewBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ログアウトボタンを設置
		let logoutBtn = UIButton(
			frame: CGRect(
				x: btnWidth * 2,
				y: Int(self.view.frame.height - 50),
				width: btnWidth,
				height: 50
		))
		logoutBtn.setTitle("ログアウト", for: .normal)  //タイトル
		logoutBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		logoutBtn.backgroundColor = appDelegate.ifNormalColor  //色
		logoutBtn.addTarget(self, action: #selector(ViewController.onClickLogout(sender:)), for: .touchUpInside)
		view.addSubview(logoutBtn)  // Viewに追加
		
		
		// --------------------------------------------------------------------------------------------------------------------------
		// iBeacon 監視開始
		
		myLocationManager = CLLocationManager()
		myLocationManager.delegate = self
		
		// 取得精度の設定.
		myLocationManager.desiredAccuracy = kCLLocationAccuracyBest
		
		// 取得頻度の設定.(1mごとに位置情報取得)
		myLocationManager.distanceFilter = 1
		
		// セキュリティ認証のステータスを取得
		let status = CLLocationManager.authorizationStatus()
		print("CLAuthorizedStatus: \(status.rawValue)");
		
		// まだ認証が得られていない場合は、認証ダイアログを表示
		if(status == .notDetermined) {
			print("認証ダイアログを表示")
			myLocationManager.requestAlwaysAuthorization()
			myUserDefault.set(nil, forKey: "timestamp")
		}
		
		// 配列をリセット
		beaconUuids = NSMutableArray()
		beaconDetails = Array<String>()
		
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
	}
	
	// 出席ボタンイベント　ボタン押下
	@objc func sendAttend(sender: UIButton) {
		print("==================== touchDown")
		// ボタンの動き
		attendBtn.backgroundColor = appDelegate.ifOnDownColor //色
		attendBtn.layer.shadowOpacity = 0
		
		print("beaconDetails: \(beaconDetails)")
		
		// 登録されているUserDefaultから設定値を呼び出す
		let user:String = myUserDefault.string(forKey: "user")!
		let key:String = myUserDefault.string(forKey: "key")!
		
		// データ送信
		let result = sendAttend(user: user, key: key, type: "M", beaconDetails: beaconDetails)
		
		// データが正常に記録された場合の処理
		if result == "success" {
			
			// ウィンドウを表示
			showAlertAutoHidden(title : "Success", message: "Attendance data was sent to the server.", time: 0.5)
			
			// バイブレーション
			AudioServicesPlaySystemSound(1003);
			AudioServicesDisposeSystemSoundID(1003);
			
			print("Attendance data was sent to the server.")
		}
	}
	
	// 自動送信機能のトグルスイッチ
	@objc internal func onClickSWautoSender(sender: UISwitch){
		
		// UserDefaultの生成.
		let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
		
		if sender.isOn {
			// 登録されているautoSenderを1にする
			let count:Int = 1
			myUserDefault.set(count, forKey: "autoSender")
			print("autosender ---> on: \(autoSender)")
			
			// ボタン表示を変更
			if(beaconFlg == true){
				btnAuto()
			} else {
				btnDisable()
			}
		} else {
			// 登録されているautoSenderを0にする
			let count:Int = 0
			myUserDefault.set(count, forKey: "autoSender")
			print("autosender ---> off: \(autoSender)")
			
			// ボタン表示を変更
			if(beaconFlg == true){
				btnNormal()
			} else {
				btnDisable()
			}
		}
	}
	
	// ヘルプの表示
	@objc func helpView(_ sender: UIButton) {
		let helpvc = HelpViewController()
		helpvc.view.backgroundColor = appDelegate.backgroundColor // 背景色
		self.present(helpvc, animated: true, completion: nil)
	}
	
	// ログの表示
	@objc func logView(_ sender: UIButton) {
		let logvc = LogViewController()
		logvc.view.backgroundColor = appDelegate.backgroundColor // 背景色
		self.present(logvc, animated: true, completion: nil)
	}
	
	// ログインの表示
	@objc func loginView() {
		let loginvc = LoginViewController()
		loginvc.view.backgroundColor = appDelegate.backgroundColor // 背景色
		self.present(loginvc, animated: true, completion: nil)
	}
	
	
	// 自動送信
	@objc func autoUpdate(timer : Timer)  {
		
		// UserDefaultの生成.
		// 登録されているUserDefaultから設定値を呼び出す
		let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
		
		// 自動送信がオンの場合
		if(autoSender == 1) {
			
			// 登録されているUserDefaultから設定値を呼び出す
			let user:String = myUserDefault.string(forKey: "user")!
			let key:String = myUserDefault.string(forKey: "key")!
			
			print("自動送信機能のチェック：\(appDelegate.postInterval)ごと")   //デバッグ
			
			// データ送信
			let result = sendAttend(user: user, key: key, type: "A", beaconDetails: beaconDetails)
			
			// データが正常に記録された場合の処理
			if result == "success" {
				// バイブレーション
				AudioServicesPlaySystemSound(1003);
				AudioServicesDisposeSystemSoundID(1003);
				print("Attendance data was sent to the server.")
			}
		}
	}
	
	// アラート表示
	func showAlert(title : String, message: String) {
		let alert = UIAlertController(
			title: title,
			message: message,
			preferredStyle: UIAlertControllerStyle.alert
		)
		let ok = UIAlertAction(
			title: "OK",
			style: UIAlertActionStyle.default,
			handler: nil
		)
		alert.addAction(ok)
		present(alert, animated: true, completion: nil)
	}
	
	func showAlertAutoHidden(title : String, message: String, time : Double) {
		
		let alert = UIAlertController(
			title: title,
			message: message,
			preferredStyle: UIAlertControllerStyle.alert
		)
		// アラート表示
		present(alert, animated: true, completion: {
			// アラートを閉じる
			DispatchQueue.main.asyncAfter(deadline: .now() + time, execute: {
				alert.dismiss(animated: true, completion: nil)
			})
			
			// 出席ボタンの表示を変更する
			self.attendBtn.backgroundColor = self.appDelegate.ifActiveColor //色
			self.attendBtn.layer.shadowOpacity = 0.3
		})
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// iBeacon関係
	//
	
	// CoreLocationの利用許可が取れたらiBeaconの検出を開始する.
	private func startMyMonitoring() {
		
		let UUIDList = appDelegate.uuidList
		
		// UUIDListのUUIDを設定して、反応するようにする
		for i in 0 ..< UUIDList.count {
			
			// BeaconのUUIDを設定.
			let uuid: NSUUID! = NSUUID(uuidString: "\(UUIDList[i].lowercased())")
			
			// BeaconのIfentifierを設定.
			let identifierStr: String = "ru-ibeacon\(i)"
			
			// リージョンを作成.
			myBeaconRegion = CLBeaconRegion(proximityUUID: uuid as UUID, identifier: identifierStr)
			
			// ディスプレイがOffでもイベントが通知されるように設定(trueにするとディスプレイがOnの時だけ反応).
			myBeaconRegion.notifyEntryStateOnDisplay = false
			
			// 入域通知の設定.
			myBeaconRegion.notifyOnEntry = true
			
			// 退域通知の設定.
			myBeaconRegion.notifyOnExit = true
			
			// [iBeacon 手順1] iBeaconのモニタリング開始([iBeacon 手順2]がDelegateで呼び出される).
			myLocationManager.startMonitoring(for: myBeaconRegion)
		}
	}
	
	// 認証のステータスがかわったら呼び出される.
	func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
		
		print("didChangeAuthorizationStatus");
		
		// 認証のステータスをログで表示
		switch (status) {
		case .notDetermined:
			print("未認証の状態")
			break
		case .restricted:
			print("制限された状態")
			break
		case .denied:
			print("許可しない")
			break
		case .authorizedAlways:
			print("常に許可")
			// 許可がある場合はiBeacon検出を開始.
			startMyMonitoring()
			break
		case .authorizedWhenInUse:
			print("このAppの使用中のみ許可")
			// 許可がある場合はiBeacon検出を開始.
			startMyMonitoring()
			break
		}
	}
	
	// startMyMonitoring()内のでstartMonitoringForRegionが正常に開始されると呼び出される。
	func locationManager(_ manager: CLLocationManager, didStartMonitoringFor region: CLRegion) {
		
		print("[iBeacon 手順2] didStartMonitoringForRegion");
		
		// この時点でビーコンがすでにRegion内に入っている可能性があるので、その問い合わせを行う
		// Delegateで呼び出される.
		manager.requestState(for: region);
	}
	
	// [iBeacon 手順4] 現在リージョン内にiBeaconが存在するかどうかの通知を受け取る.
	func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState,  for region: CLRegion) {
		
		if let region = region as? CLBeaconRegion { //これがあると安定する？
			
			// 登録されているUserDefaultから設定値を呼び出す
			let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
			
			print("[iBeacon 手順4] locationManager: didDetermineState \(state)")
			
			switch (state) {
				
			case .inside: // リージョン内にiBeaconが存在いる
				print("iBeaconが存在!");
				beaconFlg = true
				
				// iBeacon受信時の動き
				if( autoSender == 1 ) {
					btnAuto()   //自動送信ボタンの表示
				} else {
					btnNormal() //通常送信ボタンの表示
				}
				
				// すでに入っている場合は、そのままiBeaconのRangingをスタートさせる。
				// Delegateで呼び出される.
				// iBeaconがなくなったら、Rangingを停止する
				manager.startRangingBeacons(in: region )
				break;
				
			case .outside:
				print("iBeaconが圏外!")
				beaconFlg = false
				btnDisable()
				break;
				
			case .unknown:
				print("iBeaconが圏外もしくは不明な状態!")
				beaconFlg = false
				btnDisable()
				break;
				
			}
		}
	}
	
	// 現在取得しているiBeacon情報一覧が取得できる. iBeaconを検出していなくても1秒ごとに呼ばれる.
	
	func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion)  {
		
		// 配列をリセット
		beaconUuids = NSMutableArray()
		beaconDetails = Array<String>()
		var debugBeaconDetails = Array<String>()
		// 範囲内で検知されたビーコンはこのbeaconsにCLBeaconオブジェクトとして格納される
		// rangingが開始されると１秒毎に呼ばれるため、beaconがある場合のみ処理をするようにすること.
		if(beacons.count > 0){
			
			// 発見したBeaconの数だけLoopをまわす
			for i in 0 ..< beacons.count {
				
				let beacon = beacons[i]
				
				let beaconUUID = beacon.proximityUUID
				let minorID = beacon.minor
				let majorID = beacon.major
				let rssi = beacon.rssi
				let accuracy = beacon.accuracy
				
				var proximity = ""
				
				print("beaconFlg: \(beaconFlg)")
				
				switch (beacon.proximity) {
					
				case CLProximity.unknown :
					print("Proximity: Unknown");
					proximity = "Unknown"
					break
					
				case CLProximity.far:
					print("Proximity: Far");
					proximity = "Far"
					break
					
				case CLProximity.near:
					print("Proximity: Near");
					proximity = "Near"
					break
					
				case CLProximity.immediate:
					print("Proximity: Immediate");
					proximity = "Immediate"
					break
				}
				
				//                //iBeaconを検出している状態でAttendボタンをタップすると発信
				//                if(proximity != "Unknown") {
				//                    beaconFlg = true
				////                    print("--------------------------------------------- iBeacon検出しているよ！")
				//                } else {
				//                    beaconFlg = false
				//                }
				
				// 変数に保存
				beaconUuids.add(beaconUUID.uuidString)
				beaconDetails.append("\(majorID),\(minorID),\(accuracy)")
				debugBeaconDetails.append("\(majorID),\(minorID),\(proximity),\(rssi),\(accuracy)")
			}
			
			// デバッグ画面にiBeaconの値を表示
			var beaconText:String = "now: \(appDelegate.currentTime())\n\nuuid: \(beaconUuids[0])\n"
			for str in debugBeaconDetails {
				beaconText += "-->\(String(describing: str))\n"
			}
			
			debugText.text = String(describing: beaconText)
		}
	}
	
	// iBeaconを検出した際に呼ばれる.
	func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
		print("didEnterRegion: iBeaconが圏内に発見されました。");
		
		// Rangingを始める (Rangingは1秒ごとに呼ばれるので、検出中のiBeaconがなくなったら止める)
		manager.startRangingBeacons(in: region as! CLBeaconRegion)
	}
	
	// iBeaconを喪失した際に呼ばれる. 喪失後 30秒ぐらいあとに呼び出される.
	func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
		print("didExitRegion: iBeaconが圏外に喪失されました。");
		
		// 検出中のiBeaconが存在しないのなら、iBeaconのモニタリングを終了する.
		manager.stopRangingBeacons(in: region as! CLBeaconRegion)
	}
	
	
	func sendAttend(user: String, key: String, type: String, beaconDetails: Array<String>) -> String{
		let resultVal:String
		let now = appDelegate.currentTime()
		var sendtext = "\(user),\(key),\(type),\(now)"
		
		print("beaconDetails: \(beaconDetails.count)")
		
		//iBeaconの検知数が3以上ある場合
		if(beaconDetails.count > 2) {
			for i in stride(from: 0, to: 3, by: 1) {
				sendtext += ",\(beaconDetails[i])"
				print("\(i)回目のループの値は\(beaconDetails[i])")
			}
		} else {
			for value in beaconDetails {
				sendtext += ",\(value)"
			}
			for _ in stride(from: 0, to: (3 - beaconDetails.count), by: 1) {
				sendtext += ",,,"
			}
		}
		print("sendtext:\(sendtext)")
		
		// socket通信
		print("--------------------- sendAttend begin ---------------------")
		Connection.connect()
		
//		let sendtext2 = "\(user),\(key),\(type),\(now),2,2,1.1,2,2,0.12345,1,1,0.12345"
		
		// ローカルログ（対応中）
		let log = myUserDefault.string(forKey: "log")
		myUserDefault.set("\(log!)\n\(sendtext)", forKey: "log")
		//        print("log: \(log!)")
		print("--------------------- sendAttend end ---------------------")
		let retVal = Connection.sendCommand(command: sendtext)
		
		// 値がカラの場合はエラー
		if(retVal.isEmpty) {
			resultVal = "fail"
		} else {
			let result:String = retVal["response"] as! String;
			
			// 出席が正常に記録された場合の処理
			if result == "success" {
				resultVal = "success"
			} else {
				resultVal = "fail"
			}
		}
		return resultVal
	}
	
	// ログアウト
	@objc internal func onClickLogout(sender: UIButton){
		// UserDefaultの生成.
		let _:UserDefaults = UserDefaults()
		
		// UIAlertControllerを作成する.
		let myAlert: UIAlertController = UIAlertController(title: "ログアウト", message: "ログアウトしますか？", preferredStyle: .alert)
		
		// OKのアクションを作成する.
		let myOkAction = UIAlertAction(title: "OK", style: .default) { action in
			//            print("OK")
			
			// すべてのローカルデータを消去
			let appDomain = Bundle.main.bundleIdentifier
			UserDefaults.standard.removePersistentDomain(forName: appDomain!)
			
			// ログイン画面を表示
			let vc = LoginViewController()
			self.navigationController?.pushViewController(vc, animated: false)
		}
		
		// キャンセルボタン
		let myCancelAction = UIAlertAction(title: "キャンセル", style: .default, handler: { action in
			//            print("Cancel")
		})
		
		// Actionを追加する.
		myAlert.addAction(myOkAction)
		myAlert.addAction(myCancelAction)
		
		// UIAlertを発動する.
		present(myAlert, animated: true, completion: nil)
	}
	
	// ボタンの表示変更
	func btnAuto(){
		UIView.animate(withDuration: 0.5, animations: { () -> Void in
			self.attendBtn.backgroundColor = self.appDelegate.ifActiveColor
			self.attendBtn.setTitle("AUTO", for: .normal)  //タイトル
			self.attendBtn.isEnabled = false
			self.attendBtn.layer.shadowOpacity = 0
		})
	}
	func btnNormal(){
		UIView.animate(withDuration: 0.5, animations: { () -> Void in
			self.attendBtn.backgroundColor = self.appDelegate.ifActiveColor
			self.attendBtn.setTitle("出席", for: .normal)  //タイトル
			self.attendBtn.isEnabled = true
			self.attendBtn.layer.shadowOpacity = 0.3
		})
	}
	func btnDisable(){
		UIView.animate(withDuration: 0.5, animations: { () -> Void in
			self.attendBtn.backgroundColor = self.appDelegate.ifDisableColor
			self.attendBtn.setTitle("利用不可", for: .normal)  //タイトル
			self.attendBtn.isEnabled = false
			self.attendBtn.layer.shadowOpacity = 0
		})
	}
}

// --------------------------------------------------------------------------------------------------------------------------
// SSL Socketコネクション

// AppDelegateのインスタンスを取得
let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate

class Connection3: NSObject, StreamDelegate {
	
	let ServerAddress: CFString = appDelegate.serverIp as CFString //IPアドレスを指定
	let serverPort: UInt32 = UInt32(appDelegate.serverPort) //開放するポートを指定
	
	private var inputStream : InputStream!
	private var outputStream: OutputStream!
	
	// サーバーとの接続を確立する
	func connect(){
		print("connecting.....")
		
		var readStream : Unmanaged<CFReadStream>?
		var writeStream: Unmanaged<CFWriteStream>?
		
		CFStreamCreatePairWithSocketToHost(nil, self.ServerAddress, self.serverPort, &readStream, &writeStream)
		
		self.inputStream  = readStream!.takeRetainedValue()
		self.outputStream = writeStream!.takeRetainedValue()
		
		let dict = [
			kCFStreamSSLValidatesCertificateChain: kCFBooleanFalse,     // allow self-signed certificate
			kCFStreamSSLLevel: "kCFStreamSocketSecurityLevelNegotiatedSSL"    // don't understand, why there isn't a constant for version 1.2
			] as CFDictionary
		
		_ = CFReadStreamSetProperty(inputStream, CFStreamPropertyKey(kCFStreamPropertySSLSettings), dict)
		_ = CFWriteStreamSetProperty(outputStream, CFStreamPropertyKey(kCFStreamPropertySSLSettings), dict)
		
		//        if sslSetRead == false || sslSetWrite == false {
		//            throw ConnectionError.sslConfigurationFailed
		//        }
		
		self.inputStream.delegate  = self
		self.outputStream.delegate = self
		
		self.inputStream.schedule(in: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		self.outputStream.schedule(in: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		
		self.inputStream.open()
		self.outputStream.open()
		
		print("connect success!!")
	}
	
	// inputStream/outputStreamに何かしらのイベントが起きたら起動してくれる関数
	// 今回の場合では、同期型なのでoutputStreamの時しか起動してくれない
	
	func stream(_ stream:Stream, handle eventCode : Stream.Event){
		print(stream)
	}
	
	// サーバーにコマンド文字列を送信する関数
	func sendCommand(command: String) -> Dictionary<String, Any> {
		
		// 返り値を定義
		var retval = Dictionary<String, String>()
		var countCommand = command.data(using: String.Encoding.utf8, allowLossyConversion: false)!
		
		let bytes : String = countCommand.withUnsafeMutableBytes{
			bytes in return String(bytesNoCopy: bytes, length: countCommand.count, encoding: String.Encoding.utf8, freeWhenDone: false)!
		}
		
		// エラー処理
		var timeout = 5 * 100000 // タイムアウト値は5秒
		while !self.outputStream.hasSpaceAvailable {
			usleep(1000) // wait until the socket is ready
			timeout -= 100
			
			if (timeout < 0 || self.outputStream.streamError != nil) {
				print("time out")
				retval["response"] = "fail"
				
				self.inputStream.close()
				self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				self.outputStream.close()
				self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				return retval // disconnectStream will be called.
			}
		}
		
		// コマンドをサーバーに送信
		self.outputStream.write( command, maxLength: bytes.utf8.count)
		
		print("Send: \(command)")
		
		self.outputStream.close()
		self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		
		//        while(!inputStream.hasBytesAvailable){}   //不要かどうか確認中（2018/08/19）
		let bufferSize = 1024
		var buffer = Array<UInt8>(repeating: 0, count: bufferSize)
		let bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
		
		if (bytesRead >= 0) {
			let read = NSString(bytes: &buffer, length: bytesRead, encoding: String.Encoding.utf8.rawValue)!
			//            let read = String(bytes: buffer, encoding: String.Encoding.utf8)!
			retval["response"] = read as String
			print("Receive: \(retval["response"]!)")   //デバッグ
		}
		
		self.inputStream.close()
		self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		
		return retval
	}
}
