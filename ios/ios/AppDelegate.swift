//
//  AppDelegate.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
import CoreData

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
	
	var window: UIWindow?
	var navigationController: UINavigationController?
	var ActivityIndicator: UIActivityIndicatorView?
	
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
	var consentText = "プライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\nプライバシーポリシーです\n\n"
	
	// VC共通カラー
	var backgroundColor = UIColor(red: 0.93, green: 0.94, blue: 0.95, alpha: 1)	//#ecf0f1
	var ifNormalColor = UIColor(red: 0.20, green: 0.60, blue: 0.86, alpha: 1)		//#3498db
	var ifActiveColor = UIColor(red: 0.10, green: 0.74, blue: 0.61, alpha: 1)		//#1abc9c
	var ifOnDownColor = UIColor(red: 0.16, green: 0.50, blue: 0.73, alpha: 1)		//#2980b9
	var ifDisableColor = UIColor(red: 0.74, green: 0.76, blue: 0.78, alpha: 1)	//#bdc3c7
	//UIColor(red: 0.09, green: 0.63, blue: 0.52, alpha: 1)
//	// 夜間モード
//	var backgroundColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1)

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
		let user = myUserDefault.string(forKey: "user")
		let key = myUserDefault.string(forKey: "key")
		
		print(autoSender)
		print(debug)
		
		let splash: SplashViewController = SplashViewController()
		navigationController = UINavigationController(rootViewController: splash)

		if(user == nil || key == nil) {
			print("============ delegeteユーザーがセットされていないよ！ ============")
			let login: LoginViewController = LoginViewController()
			navigationController = UINavigationController(rootViewController: login)
		} else {
			
			print("============ delegeteユーザーがセットされている！ ============")
			let main: ViewController = ViewController()
			navigationController = UINavigationController(rootViewController: main)
			
			// 同意しているかチェック
			let consent:Bool = UserDefaults.standard.bool(forKey: "consent")
			
			// 利用同意をしているかチェック
			if(consent == true) {
				// 認証した場合はタイムスタンプを確認
				let timestamp:Int = myUserDefault.integer(forKey: "timestamp")
				let term = Int(NSDate().timeIntervalSince1970) - timestamp
				
				print("term:\(term)")
				
				if(term > timeLimit) {
					print("時間切れ")
					let login: LoginViewController = LoginViewController()
					navigationController = UINavigationController(rootViewController: login)
					myUserDefault.set(nil, forKey: "timestamp")
				}
			// 同意していない場合は同意画面を表示
			} else {
				print("consent: \(consent)")
				let consent: ConsentViewController = ConsentViewController()
				navigationController = UINavigationController(rootViewController: consent)
			}
		}
		
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
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskInvalid
		}
	}
	
	func applicationWillEnterForeground(_ application: UIApplication) {
		// Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskInvalid
		}
	}
	
	func applicationDidBecomeActive(_ application: UIApplication) {
		// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
		
		print("アプリが起動したよ")
	}
	
	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
	}
	
	
	// ----------------------------------------------------------------------------------------------------------------
	
	// Core Data stack
	
	lazy var persistentContainer: NSPersistentContainer = {
		/*
		The persistent container for the application. This implementation
		creates and returns a container, having loaded the store for the
		application to it. This property is optional since there are legitimate
		error conditions that could cause the creation of the store to fail.
		*/
		let container = NSPersistentContainer(name: "sk2") 
		container.loadPersistentStores(completionHandler: { (storeDescription, error) in
			if let error = error as NSError? {
				// Replace this implementation with code to handle the error appropriately.
				// fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
				
				/*
				Typical reasons for an error here include:
				* The parent directory does not exist, cannot be created, or disallows writing.
				* The persistent store is not accessible, due to permissions or data protection when the device is locked.
				* The device is out of space.
				* The store could not be migrated to the current model version.
				Check the error message to determine what the actual problem was.
				*/
				fatalError("Unresolved error \(error), \(error.userInfo)")
			}
		})
		return container
	}()
	
	// Core Data Saving support
	
	func saveContext () {
		let context = persistentContainer.viewContext
		if context.hasChanges {
			do {
				try context.save()
			} catch {
				// Replace this implementation with code to handle the error appropriately.
				// fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
				let nserror = error as NSError
				fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
			}
		}
	}
	
	// ----------------------------------------------------------------------------------------------------------------
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
	
	// --------------------------------------------------------------------------------------------------------------------------
	// iBeaconのmajor,minor値を元にローカルDBからAP名を取得
	func getBeaconName( major:String, minor:String) -> String {
		
		// データベースからデータを抽出
		let context = appDelegate.persistentContainer.viewContext
		let request = NSFetchRequest<NSManagedObject>(entityName: "IBeacon")
		//属性nameが検索文字列と一致するデータをフェッチ対象にする。
		request.predicate = NSPredicate(format:"major = %@ and minor = %@", major, minor)
		
		let models = try! context.fetch(request)
		var retval:String = ""
		for model in models {
			let name = model.value(forKey: "name")
			//			let major = model.value(forKey: "major")
			//			let minor = model.value(forKey: "minor")
			retval = name as! String
			//print("value: \(String(describing: name)) - \(String(describing: major)) - \(String(describing: minor))")
		}
		return retval
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ローカルDBのデータをすべて削除
	func deleteDbAll() -> Bool {
		
		// データの削除
		//let context = (UIApplication.shared.delegate as! AppDelegate).persistentContainer.viewContext
		let context = appDelegate.persistentContainer.viewContext
		do {
		let fetchRequest: NSFetchRequest<IBeacon> = IBeacon.fetchRequest()
		let data = try context.fetch(fetchRequest)
		
		for task in data {
			context.delete(task)
		}
		
		(UIApplication.shared.delegate as! AppDelegate).saveContext()
		} catch {
			print("Fetching Failed.")
			return false
		}
		
		// 保存
		try! context.save()
		
		return true
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ローカルDBのデータ追加
	func putApData( name:String, major:Int, minor:Int) -> Bool {
		let context = appDelegate.persistentContainer.viewContext
		let IBeacon = NSEntityDescription.insertNewObject(forEntityName: "IBeacon", into: context)
		IBeacon.setValue(name, forKey: "name")
		IBeacon.setValue(major, forKey: "major")
		IBeacon.setValue(minor, forKey: "minor")
		
		// 保存
		try! context.save()
		
		return true
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ローカルDBのデータ追加
	func getApData() -> Bool {
		let context = appDelegate.persistentContainer.viewContext
		let request = NSFetchRequest<NSManagedObject>(entityName: "IBeacon")
		let models = try! context.fetch(request)
		print("-- model count: \(models.count)")
		for model in models {
			let name = model.value(forKey: "name")
			let major = model.value(forKey: "major")
			let minor = model.value(forKey: "minor")
			print("value: \(String(describing: name)) - \(String(describing: major)) - \(String(describing: minor))")
		}
		
		return true
	}
	
}

