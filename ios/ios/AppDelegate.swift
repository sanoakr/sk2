//
//  AppDelegate.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
	
	var window: UIWindow?
	var navigationController: UINavigationController?
	
	// アプリ設定
	var appTitle = "龍大理工学部出欠システム sk2"
	var timeLimit = 10368000 //sec 120days
	var serverIp = "133.83.30.65"
	var serverPort = 4440
	var serverPort2 = 4441
	var helpUrl = "https://sk2.st.ryukoku.ac.jp/"
	//    var uuidList = ["00000000-87B3-1001-B000-001C4D975326"] //検知対象は1つのUUID。(OS等のバージョンで検出可能な上限数は20個程度が目安)
	var uuidList = ["ebf59ccc-21f2-4558-9488-00f2b388e5e6"] //本番meraki
	var debugUser = "testuser-skmt"
	var timeout = 5
	var maxLocalLog = 10
	
	// VC共通カラー
	var backgroundColor = UIColor(red: 0.93, green: 0.94, blue: 0.95, alpha: 1)
	var ifNormalColor = UIColor(red: 0.20, green: 0.60, blue: 0.86, alpha: 1)
	var ifActiveColor = UIColor(red: 0.10, green: 0.74, blue: 0.61, alpha: 1)
	var ifOnDownColor = UIColor(red: 0.16, green: 0.50, blue: 0.73, alpha: 1)
	var ifDisableColor = UIColor(red: 0.74, green: 0.76, blue: 0.78, alpha: 1)
	//UIColor(red: 0.09, green: 0.63, blue: 0.52, alpha: 1)
	// 変数
	var iconSize = 60
	var postInterval:Int = 60 //sec
	
	// background処理
	var backgroundTaskID : UIBackgroundTaskIdentifier = 0
	
	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
		// Override point for customization after application launch.
		
		// UserDefaultの生成.
		let myUserDefault:UserDefaults = UserDefaults()
		// 登録されているUserDefaultから設定値を呼び出す.
		let autoSender:Int = myUserDefault.integer(forKey: "autoSender")
		let debug:Int = myUserDefault.integer(forKey: "debug")
//		let user = myUserDefault.string(forKey: "user")
//		let key = myUserDefault.string(forKey: "key")
		
		print(autoSender)
		print(debug)
		
		let splash: SplashViewController = SplashViewController()
		navigationController = UINavigationController(rootViewController: splash)

//		if(user == nil || key == nil) {
//			print("============ delegeteユーザーがセットされていないよ！ ============")
//			let login: LoginViewController = LoginViewController()
//			navigationController = UINavigationController(rootViewController: login)
//		} else {
//			
//			print("============ delegeteユーザーがセットされている！ ============")
//			let main: ViewController = ViewController()
//			navigationController = UINavigationController(rootViewController: main)
//			
//			// 認証した場合はタイムスタンプを確認
//			let timestamp:Int = myUserDefault.integer(forKey: "timestamp")
//			let term = Int(NSDate().timeIntervalSince1970) - timestamp
//			
//			print("term:\(term)")
//			
//			if(term > timeLimit) {
//				print("時間切れ")
//				let login: LoginViewController = LoginViewController()
//				navigationController = UINavigationController(rootViewController: login)
//				myUserDefault.set(nil, forKey: "timestamp")
//			}
//		}
		
		self.window = UIWindow(frame: UIScreen.main.bounds)
		self.window?.rootViewController = navigationController
		self.window?.makeKeyAndVisible()
		
		return true
	}
	
	// バックグラウンド遷移移行直前に呼ばれる
	func applicationWillResignActive(_ application: UIApplication) {
		// Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
		// Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
		
		print("バックグラウンド実行")
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskInvalid
		}
		
	}
	
	func applicationDidEnterBackground(_ application: UIApplication) {
		// Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
		// If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
	}
	
	func applicationWillEnterForeground(_ application: UIApplication) {
		// Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
	}
	
	func applicationDidBecomeActive(_ application: UIApplication) {
		// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
		
		print("アプリが起動したよ")
	}
	
	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
	}
	
	// 現在時刻取得
	func currentTime() -> String {
		
		let dateFormatter = DateFormatter()
		dateFormatter.locale = Locale(identifier: "ja_JP")
		dateFormatter.dateFormat = "yyyy-MM-dd HH-mm-ss"
		
		let date = Date()
		
		let dateString = dateFormatter.string(from: date)
//		print(dateString)
		
		return dateString
	}
}

