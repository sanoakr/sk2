
//  AppDelegate.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
import CoreData
import UserNotifications

@available(iOS 13.0, *)
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
	
	var window: UIWindow?
	var navigationController: UINavigationController?
	var ActivityIndicator: UIActivityIndicatorView?
	
	// アプリ設定
	var appTitle = "龍大理工学部出欠システム sk2"
	var timeLimit = 10368000 // 認証後の有効期限（秒/120日）
	var serverIp = "133.83.30.65"
	var serverPort = 4440
	var serverPort2 = 4441
	var helpUrl = "https://sk2.st.ryukoku.ac.jp/"
	var uuidList = ["ebf59ccc-21f2-4558-9488-00f2b388e5e6"] //検知対象は1つのUUID。(OS等のバージョンで検出可能な上限数は20個程度が目安)
	var debugUser = "testuser-skmt"
	var iconSize = 60
	var timeout = 5		// 処理のタイムアウト（秒）
	var startHour = 7	// 出席取得　開始時間（時）
	var stopHour = 24	// 出席取得　終了時間（時）
	var maxLocalLog = 10	// ローカルログの保持回数
	var postInterval:Int = 600 // 自動送信の間隔（秒） default:600
	var consentText = "龍谷大学（以下「本学」）理工学部（以下「当学部」）は、当学部が提供するスマートフォン用アプリケーション「理工出席」（以下「本アプリ」）および本アプリによって提供するサービス（以下「本サービス」）を通じて利用者の位置情報をご提供いただきます。当学部は、本サービスの円滑な提供を実施させていただくために、本学が定めた龍谷大学プライバシーポリシーに基づき、本プライバシーポリシーを定め、利用者の情報の保護に努めます。\n\nなお、利用者が本アプリによる情報提供を希望されない場合は、利用者自身の判断により、位置情報の提供を拒否することができます。この場合、本アプリおよび本サービスを利用になれない場合があります。\n\n1. 本アプリにより本学部が取得する情報と利用目的\n\n当学部は、本アプリおよび本サービスの提供等にあたり、次の利用目的の達成に必要な範囲で下記に記載する情報をアプリケーション経由で自動的に取得および利用者の操作によって取得し、取扱います。\n\n　1. 本アプリの認証時に当学部サーバに通知される全学統合認証ID\n　　・利用目的\n　　　本サービス提供の際に利用者を識別するため\n　　・取得方法\n　　　利用者によるログイン操作または、本アプリによる自動取得\n　2. 利用端末のBluetooth機能を利用した、本学キャンパスおよび関連施設に限定した位置情報及び、その取得時刻\n　　・利用目的\n　　　本サービス提供の際、位置情報を確認し、利用者の講義等への出欠情報を取得するため\n　　　利用者の位置情報を利用者個人が特定されないかたちで、本学の運営・教育・研究に利用するため\n　　・取得方法\n　　　利用者による取得送信操作及び本アプリによる定期的な自動取得\n　　　※ 位置情報は本アプリの起動後、利用者が出席情報の送信ボタンを押下したタイミングまたは、本アプリの自動送信機能をオンにした場合およそ10分間に1回、本学キャンパスおよびその関連施設内で、授業実施時間に限り取得します。\n\n2. 本学部が取得する情報の加工および第三者提供\n\n　・当学部は、前条において取得した情報を、本サービスの提供に係る当学部のシステムへ取得・蓄積・転送し、前条の利用目的に利用します。\n　・当学部は、前項の規定に関わらず、本アプリが取得する情報を次の各号のいずれかに該当すると認める場合は、本人の権利利益に最大限の配慮を払いつつ、個人情報を第三者に提供する場合があります。\n\n　　1. 本人から同意を得た場合。\n　　2. 法令に基づく場合。\n　　3. 人の生命、身体又は財産の保護のために必要がある場合であって、本人の同意を得ることが困難である場合。\n　　4. 公衆衛生の向上又は児童の健全な育成の推進のために特に必要がある場合であって、本人の同意を得ることが困難である場合。\n　　5. 国の機関若しくは地方公共団体又はその委託を受けた者が、法令の定める事務を遂行することに協力する必要がある場合であって、本人の同意を得ることによりその遂行に支障を及ぼすおそれがある場合。\n　　6. 取得した位置情報等を、本学部が利用者本人を特定できる情報を含まない、総体的かつ統計的なデータに加工した場合。\n\n3. 本学部が取得する情報の取得・蓄積・利用に関する同意\n\n利用者は本アプリをインストールする際に本プライバシーポリシーを確認頂き、本アプリおよび本サービスに関する内容を理解した上でご利用ください。\n\n4. 本学部が取得する情報の取得停止等\n\n　　1. 本サービスでは、第1条に定める規定に基づき、取得した利用者情報の内容に関し、本学部の教学上の利用者本人に資する必要性がなくなった後に、照会・訂正・削除等を申請することができます。\n　　2. 本アプリおよび本サービスは、利用者が、本アプリが利用端末から削除（アンインストール）された場合、利用者情報は全て端末より直ちに削除されます。\n\n5. 本学部が取得する情報の取り扱いに関する問い合わせ窓口\n\n本アプリおよび本サービスにおける本学部が取得する情報の取扱いに関して、ご意見・ご要望がございましたら、下記窓口までご連絡ください。\n\n　・窓口名称：龍谷大学理工学部教務課\n　・お問い合わせ方法：\n　　　・電話：077-543-7730\n　　　・電子メール： rikou@ad.ryukoku.ac.jp\n　・受付時間：平日午前9時～午後5時\n\n6. 本プライバシーポリシーの変更\n\n　　1. 本学部は、法令の変更等に伴い、本プライバシーポリシーを変更することがあります。\n　　2. 本学部は、本アプリのバージョンアップに伴って、本学部が取得する情報の取得項目の変更や追加、利用目的の変更、第三者提供等について変更がある場合には、本アプリ内で通知し、重要なものについてはインストール前もしくはインストール時にあらためて同意を取得させていただきます。"
	
	// VC共通カラー
    
    // iOS13かどうかチェック
//    var backgroundColor = UIColor.systemGray6
//    var ifDebugBackgroundColor = UIColor.systemGray2
//    var ifUserInfoColor = UIColor.systemGray5
//    // var backgroundColor = UIColor(red: 0.93, green: 0.94, blue: 0.95, alpha: 1)    //#ecf0f1
//	var ifNormalColor = UIColor(red: 0.20, green: 0.60, blue: 0.86, alpha: 1)		//#3498db
//	var ifActiveColor = UIColor(red: 0.10, green: 0.74, blue: 0.61, alpha: 1)		//#1abc9c
//	var ifOnDownColor = UIColor(red: 0.16, green: 0.50, blue: 0.73, alpha: 1)		//#2980b9
//	var ifDisableColor = UIColor(red: 0.74, green: 0.76, blue: 0.78, alpha: 1)	//#bdc3c7

	// background処理
	var backgroundTaskID : UIBackgroundTaskIdentifier = UIBackgroundTaskIdentifier(rawValue: 0)
	
    var message : String!
    
    // バージョン情報
    let currentVersion: String! = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
    let currentBuild: String! = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String
    
	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
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
        
//        print("============ userDefaults begin ============")
//        print(UserDefaults.standard.dictionaryRepresentation())
//        print("============ userDefaults  end  ============")
//        
        
		let splash: SplashViewController = SplashViewController()
        let setVersion = UserDefaults().string(forKey: "currentVersion")
        
		navigationController = UINavigationController(rootViewController: splash)

		if(user == nil || key == nil) {
			print("============ delegeteユーザーがセットされていないよ！ ============")
            
			let login: LoginViewController = LoginViewController()
			navigationController = UINavigationController(rootViewController: login)
		} else {
			
			print("============ delegeteユーザーがセットされている！ ============")
			let main: ViewController = ViewController()
			navigationController = UINavigationController(rootViewController: main)
			
            print("setVersion:\(String(describing: setVersion))")
            print("currentVersion:\(String(describing: currentVersion))")
            
			// 同意しているかチェック
			let consent:Bool = UserDefaults.standard.bool(forKey: "consent")
			
			// 利用同意をしているかチェック
			if(consent == true) {
				// 認証した場合はタイムスタンプを確認
				let timestamp:Int = myUserDefault.integer(forKey: "timestamp")
				let term = Int(NSDate().timeIntervalSince1970) - timestamp
                
                print("timestamp:\(timestamp)")
				print("term:\(term)")
				
                // 認証維持期間を過ぎている場合
				if(term > timeLimit) {
					print("時間切れ")
                    appDelegate.message = "timeOver"
					let login: LoginViewController = LoginViewController()
					navigationController = UINavigationController(rootViewController: login)
					myUserDefault.set(nil, forKey: "timestamp")
                    
                // 保存されているバージョンとアプリのバージョンが違う場合
                } else if( (setVersion != nil) && setVersion != currentVersion) {
                    print("バージョン違い")
                    appDelegate.message = "versionMismatch"
                    let login: LoginViewController = LoginViewController()
                    navigationController = UINavigationController(rootViewController: login)
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
		
		print("applicationWillResignActive")
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskIdentifier.invalid
		}
		
	}
	
	func applicationDidEnterBackground(_ application: UIApplication) {
		// Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
		// If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
		
		print("applicationDidEnterBackground")
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskIdentifier.invalid
		}
		
//        //
//        // 通知テスト（使うかどうかはわからない）
//        //
//        //　通知設定に必要なクラスをインスタンス化
//        let trigger: UNNotificationTrigger
//        let content = UNMutableNotificationContent()
//        var notificationTime = DateComponents()
//        
//        // トリガー設定
//        notificationTime.hour = 12
//        notificationTime.minute = 14
//        trigger = UNCalendarNotificationTrigger(dateMatching: notificationTime, repeats: false)
//        
//        // 通知内容の設定
//        content.title = ""
//        content.body = "【通知テスト】\(String(describing: notificationTime.hour)):\(String(describing: notificationTime.minute))時になりました"
//        content.sound = UNNotificationSound.default
//        
//        // 通知スタイルを指定
//        let request = UNNotificationRequest(identifier: "uuid", content: content, trigger: trigger)
//        // 通知をセット
//        UNUserNotificationCenter.current().add(request, withCompletionHandler: nil)
	}
	
	func applicationWillEnterForeground(_ application: UIApplication) {
		// Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
		
		print("applicationWillEnterForeground")
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskIdentifier.invalid
		}
	}
	
	func applicationDidBecomeActive(_ application: UIApplication) {
		// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
		
		print("アプリが起動したよ")
		
		application.endBackgroundTask(convertToUIBackgroundTaskIdentifier(self.backgroundTaskID.rawValue))
	}
	
	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
		
		self.backgroundTaskID = application.beginBackgroundTask(){
			[weak self] in
			application.endBackgroundTask((self?.backgroundTaskID)!)
			self?.backgroundTaskID = UIBackgroundTaskIdentifier.invalid
		}
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
	// ローカルDBのオブジェクトの書込
	func putApData( name:String, notes:String, major:Int, minor:Int) -> Bool {
		let context = appDelegate.persistentContainer.viewContext
		let IBeacon = NSEntityDescription.insertNewObject(forEntityName: "IBeacon", into: context)
		IBeacon.setValue(notes, forKey: "name")
//        IBeacon.setValue(notes, forKey: "notes")
		IBeacon.setValue(major, forKey: "major")
		IBeacon.setValue(minor, forKey: "minor")
		
		// 保存
		try! context.save()
		
		return true
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ローカルDBからオブジェクトの読込
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
    
    // --------------------------------------------------------------------------------------------------------------------------
    // 条件をクリアするまで待ちます
    //
    // - Parameters:
    //   - waitContinuation: 待機条件
    //   - compleation: 通過後の処理
    func wait(_ waitContinuation: @escaping (()->Bool), compleation: @escaping (()->Void)) {
        var wait = waitContinuation()
        // 0.01秒周期で待機条件をクリアするまで待ちます。
        let semaphore = DispatchSemaphore(value: 0)
        DispatchQueue.global().async {
            while wait {
                DispatchQueue.main.async {
                    wait = waitContinuation()
                    semaphore.signal()
                }
                semaphore.wait()
                Thread.sleep(forTimeInterval: 0.01)
            }
            // 待機条件をクリアしたので通過後の処理を行います。
            DispatchQueue.main.async {
                compleation()
            }
        }
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    // カラー設定（iOS13対応版）
    //
    // - Parameters:
    //   - name: カラー指定
    func setColor( name:String ) -> UIColor {
        let color:UIColor
        
        switch name {
        case "backgroundColor":
            if #available(iOS 13.0, *) {
                color = UIColor.systemGray6
            } else {
                color = UIColor(red: 0.93, green: 0.94, blue: 0.95, alpha: 1)    //#ecf0f1
            }
        case "ifDebugBackgroundColor":
            if #available(iOS 13.0, *) {
                color = UIColor.systemGray2
            } else {
                color = UIColor(red: 0.68, green: 0.68, blue: 0.70, alpha: 1)    //#ecf0f1
            }
        case "ifUserInfoColor":
            if #available(iOS 13.0, *) {
                color = UIColor.systemGray5
            } else {
                color = UIColor(red: 0.90, green: 0.90, blue: 0.92, alpha: 1)    //#ecf0f1
            }
        case "ifNormalColor":
            color = UIColor(red: 0.20, green: 0.60, blue: 0.86, alpha: 1)        //#3498db
        case "ifActiveColor":
            color = UIColor(red: 0.10, green: 0.74, blue: 0.61, alpha: 1)        //#1abc9c
        case "ifOnDownColor":
            color = UIColor(red: 0.16, green: 0.50, blue: 0.73, alpha: 1)        //#2980b9
        case "ifDisableColor":
            color = UIColor(red: 0.74, green: 0.76, blue: 0.78, alpha: 1)        //#bdc3c7
        default:
            color = UIColor(red: 0.20, green: 0.60, blue: 0.86, alpha: 1)        //#3498db
        }
        
        return color
    }
	
}


// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToUIBackgroundTaskIdentifier(_ input: Int) -> UIBackgroundTaskIdentifier {
	return UIBackgroundTaskIdentifier(rawValue: input)
}
