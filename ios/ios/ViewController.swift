//
//  ViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
//import NetworkExtension
import SystemConfiguration.CaptiveNetwork
import CoreLocation
import AudioToolbox
import Foundation
import CoreData

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
	var labelAuto : UILabel!
	var labelBeacon : UILabel!
	
	// iBeacon
	var myLocationManager : CLLocationManager!
	var myBeaconRegion : CLBeaconRegion!
	var beaconUuids : NSMutableArray!   //iBeaconのUUID配列
	var beaconDetails : Array<String>!  //iBeaconの値配列
	var beaconFlg = false   //iBeaconの取得フラグ
	var debugMode:Int = 0   //デバッグモードのフラグ
	
	// アイコン
	let btnLogImageDefault :UIImage? = UIImage(named:"log.png")
	let btnHelpImageDefault :UIImage? = UIImage(named:"help.png")
	let btnLogoutImageDefault :UIImage? = UIImage(named:"logout.png")
	
	// UserDefaultの生成
	let myUserDefault:UserDefaults = UserDefaults()
	
	override func viewDidLoad() {
		super.viewDidLoad()

		// デバイスの固有ID取得
//		print("DeviceID: \(String(describing: UIDevice.current.identifierForVendor))")
		
		// UI定義
		let btnWidth = Int(self.view.frame.width / 3)
		self.navigationItem.hidesBackButton = true	//　バックボタンを消す
		// ナビゲーションバーの高さを取得する
		let navigationBarHeight = self.navigationController?.navigationBar.frame.size.height
		let statusVar = UIApplication.shared.statusBarFrame.height
		
		// 登録されているUserDefaultから設定値を呼び出す
		let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
		let user = myUserDefault.string(forKey: "user")
//		let engName = myUserDefault.string(forKey: "engName")
		let jpnName = myUserDefault.string(forKey: "jpnName")
		//        let key = myUserDefault.string(forKey: "key")
		let userInfo = user! + " / " + jpnName!
		let consent:Bool = UserDefaults.standard.bool(forKey: "consent")
		
		print("consent:\(consent)")
		
		// 同意済みかチェックし、同意していない場合は同意画面に遷移
		if(consent == false) {
			// 同意画面を表示
			let vc = ConsentViewController()
			self.navigationController?.pushViewController(vc, animated: false)
		}
		
//		// 保存されているデータをすべて表示（デバッグ用）
//		for (key, value) in UserDefaults.standard.dictionaryRepresentation().sorted(by: { $0.0 < $1.0 }) {
//			print("- \(key) => \(value)")
//		}
		
		// 検証用ユーザーの場合はdebugモードにする
		if(user == appDelegate.debugUser) {
			debugMode = 1
		}
		self.view.backgroundColor = appDelegate.backgroundColor // 背景色をセット
		
		// --------------------------------------------------------------------------------------------------------------------------
		// userを生成
		labelUser = UILabel(frame: CGRect(x:0, y:navigationBarHeight! + statusVar, width:self.view.frame.width, height:50))
		labelUser.font = UIFont.systemFont(ofSize: 14.0)    //フォントサイズ
		labelUser.backgroundColor = #colorLiteral(red: 0.9019607843, green: 0.9137254902, blue: 0.9176470588, alpha: 1)
		labelUser.textAlignment = NSTextAlignment.left    // 左寄せ
		labelUser.text = " \(userInfo)"
		view.addSubview(labelUser)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// debugTextを生成
		debugText = UITextView(frame: CGRect(x:10, y:navigationBarHeight! + 70, width:self.view.frame.width - 20, height:160))
		debugText.font = UIFont.systemFont(ofSize: 14.0)    //フォントサイズ
		debugText.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0)    // 背景色
		debugText.isEditable = false    // 編集不可
		debugText.textAlignment = NSTextAlignment.left    // 左寄せ
		
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
		SWautoSender.layer.position = CGPoint(x: self.view.bounds.width - 30, y: navigationBarHeight! + statusVar + 25)
		if(autoSender == 0) {
			SWautoSender.isOn = false    // SwitchをOffに設定
		} else if( autoSender == 1 ) {
			SWautoSender.isOn = true    // SwitchをOnに設定
		}
		// 背景色を設定
		SWautoSender.onTintColor = appDelegate.ifNormalColor
		
		// SwitchのOn/Off切り替わりの際に、呼ばれるイベントを設定する.
		SWautoSender.addTarget(self, action: #selector(ViewController.onClickSWautoSender(sender:)), for: UIControlEvents.valueChanged)
		self.view.addSubview(SWautoSender)  // SwitchをViewに追加
		
		// 説明ラベル
		labelAuto = UILabel(frame: CGRect(x:0, y: navigationBarHeight! + statusVar, width:self.view.frame.width - 60, height:50))
		labelAuto.font = UIFont.systemFont(ofSize: 14.0)    //フォントサイズ
		labelAuto.textAlignment = NSTextAlignment.right    // センター寄せ
		labelAuto.text = "自動送信"
		view.addSubview(labelAuto)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 出席ボタンを設置
		attendBtn = UIButton(frame: CGRect(x: (self.view.frame.width/2 - 120),y: (self.view.frame.height / 2 - 120 + navigationBarHeight!),width: 240,height:240))
		attendBtn.addTarget(self, action: #selector(ViewController.sendAttend(sender:)), for: .touchUpInside)
		attendBtn.titleLabel?.lineBreakMode = .byWordWrapping
		attendBtn.titleLabel?.numberOfLines = 0
		attendBtn.titleLabel?.textAlignment = NSTextAlignment.center
		attendBtn.titleLabel?.font = UIFont.systemFont(ofSize: 35)
		attendBtn.layer.cornerRadius = attendBtn.frame.size.width * 0.5 //丸まり
		attendBtn.layer.shadowOffset = CGSize(width: 0, height: 2 )
		attendBtn.layer.shadowColor = UIColor.black.cgColor
		attendBtn.layer.shadowRadius = 6
		attendBtn.layer.shadowOpacity = 0.3
		attendBtn.layer.borderColor = UIColor.white.cgColor
		attendBtn.layer.borderWidth = 1
		btnDisable()
		view.addSubview(attendBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ログボタンを設置
		let logViewBtn = UIButton(
			frame: CGRect(
				x: Int(btnWidth - 80),
				y: Int(self.view.frame.height - 120),
				width: appDelegate.iconSize,
				height: appDelegate.iconSize
		))
		logViewBtn.setBackgroundImage(btnLogImageDefault!, for: .normal)
		logViewBtn.setTitle("ログ", for: .normal)  //タイトル
		logViewBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		logViewBtn.setTitleColor(appDelegate.ifNormalColor, for: .normal) // タイトルの色
		logViewBtn.titleEdgeInsets = UIEdgeInsets(top: 0.0, left: 0.0, bottom: -90.0, right: 0.0)
		logViewBtn.layer.cornerRadius = logViewBtn.frame.size.width * 0.5 //丸まり
		logViewBtn.addTarget(self, action: #selector(ViewController.logView(_:)), for: .touchUpInside)
		logViewBtn.layer.borderColor = appDelegate.ifNormalColor.cgColor
		logViewBtn.layer.borderWidth = 1
		view.addSubview(logViewBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ヘルプボタンを設置
		let helpViewBtn = UIButton(
			frame: CGRect(
				x: Int(self.view.frame.width / 2) - Int(appDelegate.iconSize / 2),
				y: Int(self.view.frame.height - 120),
				width: appDelegate.iconSize,
				height: appDelegate.iconSize
		))
		helpViewBtn.setBackgroundImage(btnHelpImageDefault!, for: .normal)
		helpViewBtn.setTitle("ヘルプ", for: .normal)  //タイトル
		helpViewBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		helpViewBtn.setTitleColor(appDelegate.ifNormalColor, for: .normal) // タイトルの色
		helpViewBtn.titleEdgeInsets = UIEdgeInsets(top: 0.0, left: 0.0, bottom: -90.0, right: 0.0)
		helpViewBtn.layer.cornerRadius = helpViewBtn.frame.size.width * 0.5 //丸まり
		helpViewBtn.addTarget(self, action: #selector(ViewController.helpView(_:)), for: .touchUpInside)
		helpViewBtn.layer.borderColor = appDelegate.ifNormalColor.cgColor
		helpViewBtn.layer.borderWidth = 1
		view.addSubview(helpViewBtn)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ログアウトボタンを設置
		let logoutBtn = UIButton(
			frame: CGRect(
				x: Int(self.view.frame.width) - Int(appDelegate.iconSize + 50),
				y: Int(self.view.frame.height - 120),
				width: appDelegate.iconSize,
//				width: btnWidth,
				height: appDelegate.iconSize
		))
//		logoutBtn.setImage(btnLogoutImageDefault!, for: .normal)
		logoutBtn.setBackgroundImage(btnLogoutImageDefault!, for: .normal)
		logoutBtn.setTitle("ログアウト", for: .normal)  //タイトル
		logoutBtn.titleLabel?.font = UIFont.systemFont(ofSize: 12)
		logoutBtn.setTitleColor(appDelegate.ifNormalColor, for: .normal) // タイトルの色
		logoutBtn.titleEdgeInsets = UIEdgeInsets(top: 0.0, left: 0.0, bottom: -90.0, right: 0.0)
		logoutBtn.layer.cornerRadius = logoutBtn.frame.size.width * 0.5 //丸まり
		logoutBtn.addTarget(self, action: #selector(ViewController.onClickLogout(sender:)), for: .touchUpInside)
		logoutBtn.layer.borderColor = appDelegate.ifNormalColor.cgColor
		logoutBtn.layer.borderWidth = 1
//		logoutBtn.titleEdgeInsets = UIEdgeInsetsMake(0, -50, 0, 50);
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
			showAlertAutoHidden(title : "出席を記録しました", message: "", time: 0.5)
			
			// バイブレーション
			AudioServicesPlaySystemSound(1003);
			AudioServicesDisposeSystemSoundID(1003);
			
			print("sendAttend: success")
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
			self.attendBtn.backgroundColor = self.appDelegate.ifNormalColor //色
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
			
			// iBeaconのモニタリング開始
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
	
	// startMyMonitoring()内のでstartMonitoringForRegionが正常に開始されると呼び出される
	func locationManager(_ manager: CLLocationManager, didStartMonitoringFor region: CLRegion) {
		
		print("didStartMonitoringForRegion");
		
		// この時点でビーコンがすでにRegion内に入っている可能性があるので、その問い合わせを行う
		// Delegateで呼び出される.
		manager.requestState(for: region);
	}
	
	// 現在リージョン内にiBeaconが存在するかどうかの通知を受け取る
	func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState,  for region: CLRegion) {
		
		if let region = region as? CLBeaconRegion { //これがあると安定する？
			
			// 登録されているUserDefaultから設定値を呼び出す
			let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
			
			print("locationManager: didDetermineState \(state)")
			
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
				
				// すでに入っている場合は、そのままiBeaconのRangingをスタートさせる
				// Delegateで呼び出される
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
	
	// 現在取得しているiBeacon情報一覧が取得
	// iBeaconを検出していなくても1秒ごとに呼ばれる
	func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion)  {
		
		// 配列をリセット
		beaconUuids = NSMutableArray()
		beaconDetails = Array<String>()
		var val : [(majorID:Int, minorID:Int, rssi:Double, accuracy:Double, proximity:String)] = []
		// 範囲内で検知されたビーコンはこのbeaconsにCLBeaconオブジェクトとして格納される
		// rangingが開始されると１秒毎に呼ばれるため、beaconがある場合のみ処理をするようにすること
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
				
//				print("beaconFlg: \(beaconFlg)")
				
				switch (beacon.proximity) {
					
				case CLProximity.unknown :
//					print("Proximity: Unknown");
					proximity = "Unknown"
					break
					
				case CLProximity.far:
//					print("Proximity: Far");
					proximity = "Far"
					break
					
				case CLProximity.near:
//					print("Proximity: Near");
					proximity = "Near"
					break
					
				case CLProximity.immediate:
//					print("Proximity: Immediate");
					proximity = "Immediate"
					break
				}
				
				// 変数に保存
				beaconUuids.add(beaconUUID.uuidString)
				val.append((majorID: Int(truncating: majorID), minorID: Int(truncating: minorID), rssi: Double(rssi), accuracy: Double(accuracy), proximity: String(proximity)))
			}
			// accuracyでソート
			val.sort(by: {$0.3 > $1.3})
			
			// デバッグ画面にiBeaconの値を表示
			var beaconText:String = "now: \(appDelegate.currentTime())\n\nuuid: \(beaconUuids[0])\n"
			for str in val {
				beaconDetails.append("\(Int(str.majorID)),\(Int(str.minorID)),\(Double(str.accuracy))")
				beaconText += "-->\(str.majorID),\(str.minorID),\(str.rssi),\(str.accuracy),\(str.proximity)\n"
//				beaconText += "-->\(String(describing: str))\n"
			}
			
			debugText.text = String(describing: beaconText)
		}
	}
	
	// iBeaconを検出した際に呼ばれる
	func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
		print("didEnterRegion: iBeaconが圏内に発見されました。");
		
		// Rangingを始める (Rangingは1秒ごとに呼ばれるので、検出中のiBeaconがなくなったら止める)
		manager.startRangingBeacons(in: region as! CLBeaconRegion)
	}
	
	// iBeaconを喪失した際に呼ばれる. 喪失後 30秒ぐらいあとに呼び出される
	func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
		print("didExitRegion: iBeaconが圏外に喪失されました。");
		
		// 検出中のiBeaconが存在しないのなら、iBeaconのモニタリングを終了する
		manager.stopRangingBeacons(in: region as! CLBeaconRegion)
	}
	
	
	func sendAttend(user: String, key: String, type: String, beaconDetails: Array<String>) -> String{
		
		let resultVal:String
		let now = appDelegate.currentTime()
		var sendtext = "\(key),\(type),\(now)"
		
		print("--------------------- sendAttend begin ---------------------")
		
		//iBeaconの検出数が3以上ある場合
		if(beaconDetails.count > 2) {
			for i in stride(from: 0, to: 3, by: 1) {
				sendtext += ",\(beaconDetails[i])"
				print("\(i)回目のループの値は\(beaconDetails[i])")
			}
		} else {
			//検出したiBeaconの値を入れる
			for value in beaconDetails {
				sendtext += ",\(value)"
			}
			//不足分をカラの値に入れる
			for _ in stride(from: 0, to: (3 - beaconDetails.count), by: 1) {
				sendtext += ",,,"
			}
		}
		print("sendtext:\(sendtext)")
		
		// socket通信
		Connection.connect()
		
		// ローカルログへ保存
		saveLocalLog(sendtext: sendtext)
		
		sendtext = "\(user)," + sendtext
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
		
		print("--------------------- sendAttend end ---------------------")
		
		return resultVal
	}
	
	// ローカルログの保存
	@objc internal func saveLocalLog(sendtext: String) {
		
		var log = Array<Any>()
		
		// ローカルログが存在する場合
		if myUserDefault.array(forKey: "log") != nil {
			
			// その値を取得して配列に追加
			log = myUserDefault.array(forKey: "log")!
			
			// 指定数を超えた過去のログは削除
			if( log.count >= Int(appDelegate.maxLocalLog) ) {
				log.removeLast()
			}
		}
		
		log.insert(sendtext, at: 0)
		myUserDefault.set(log, forKey: "log")
	}
	
	// ログアウト
	@objc internal func onClickLogout(sender: UIButton){
		// UserDefaultの生成.
		let _:UserDefaults = UserDefaults()
		
		// UIAlertControllerを作成する
		let myAlert: UIAlertController = UIAlertController(title: "ログアウト", message: "ログアウトしますか？", preferredStyle: .alert)
		
		// OKのアクションを作成する
		let myOkAction = UIAlertAction(title: "OK", style: .default) { action in
			//            print("OK")
			
			// すべてのローカルデータを消去
			let appDomain = Bundle.main.bundleIdentifier
			UserDefaults.standard.removePersistentDomain(forName: appDomain!)
			UserDefaults.standard.synchronize()
			
			// ログイン画面を表示
			let vc = LoginViewController()
			self.navigationController?.pushViewController(vc, animated: false)
		}
		
		// キャンセルボタン
		let myCancelAction = UIAlertAction(title: "キャンセル", style: .default, handler: { action in
			//            print("Cancel")
		})
		
		// Actionを追加する
		myAlert.addAction(myOkAction)
		myAlert.addAction(myCancelAction)
		
		// UIAlertを発動する
		present(myAlert, animated: true, completion: nil)
	}
	
	// ボタンの表示変更
	func btnAuto(){
		UIView.animate(withDuration: 0.5, animations: { () -> Void in
			self.attendBtn.backgroundColor = self.appDelegate.ifNormalColor
			self.attendBtn.setTitle("出席\n（自動ON）", for: .normal)  //タイトル
			self.attendBtn.isEnabled = true
			self.attendBtn.layer.shadowOpacity = 0.3
		})
	}
	func btnNormal(){
		UIView.animate(withDuration: 0.5, animations: { () -> Void in
			self.attendBtn.backgroundColor = self.appDelegate.ifNormalColor
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
		print("stream: \(stream)")
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
		var timeout = appDelegate.timeout * 100000 // タイムアウト値は5秒
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
