//
//  LoginViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/25.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
import CoreData

class LoginViewController: UIViewController {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	var ServerAuth_ = ServerAuth()
	
	// UIを宣言
	var leftBarButton: UIBarButtonItem!
	
    // ローディングを宣言
    var activityIndicatorView = UIActivityIndicatorView()
    
	@IBOutlet var useridField: UITextField!
	@IBOutlet var passwordField: UITextField!
	
	override func viewDidLoad() {
		super.viewDidLoad()
        
//		// データベースからデータをロード
//		print("load begin")
//		container = NSPersistentContainer(name: "sk2")
//		container.loadPersistentStores { (desc, error) in
//			if let error = error {
//				print("load error: \(error)")
//			} else {
//				print("load ok: \(desc)")
//			}
//		}
//
		self.view.backgroundColor = appDelegate.backgroundColor  // 背景色をセット
		self.navigationItem.hidesBackButton = true	//　バックボタンを消す
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ナビゲーション
		self.navigationItem.title = "ログイン"

		// --------------------------------------------------------------------------------------------------------------------------
		// titleViewを生成
		let titleView: UITextView = UITextView(frame: CGRect(x:10, y:100, width:self.view.frame.width - 20, height:100))
		titleView.text = appDelegate.appTitle
		titleView.font = UIFont.systemFont(ofSize: 20.0)    //フォントサイズ
		titleView.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0)    // 背景色
		titleView.isEditable = false    // 編集不可
		titleView.textAlignment = NSTextAlignment.center    // センター寄せ
		view.addSubview(titleView)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// userLabelを生成
		let userLabel: UILabel = UILabel(frame: CGRect(x:20, y:200, width:self.view.frame.width - 40, height:30))
		userLabel.text = "全学統合認証ID / パスワード"
		userLabel.font = UIFont.systemFont(ofSize: 15.0)  //フォントサイズ
		userLabel.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0)  // 背景色
		view.addSubview(userLabel)  // TextViewをViewに追加
		
		// useridFieldを生成
		useridField = UITextField(frame: CGRect(x:20, y:230, width:self.view.frame.width - 40, height:30))
		useridField.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1)    // 背景色
		useridField.borderStyle = .none
		useridField.autocapitalizationType = .none
		useridField.placeholder = "全学統合認証ID"
//		useridField.bounds.insetBy(dx: 10, dy: 10)
		useridField.layer.cornerRadius = 0
		useridField.layer.borderWidth  = 0
		useridField.layer.masksToBounds = true
		useridField.clearButtonMode = .whileEditing   //キャンセル
		useridField.leftViewMode = .always    // 左の余白
		useridField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: 10))   // 左の余白
		view.addSubview(useridField)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// passwordFieldを生成
		passwordField = UITextField(frame: CGRect(x:20, y:265, width:self.view.frame.width - 40, height:30))
		passwordField.leftViewMode = UITextField.ViewMode.always
		passwordField.placeholder = "パスワード"
		passwordField.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1)    // 背景色
		passwordField.borderStyle = .none
		passwordField.layer.cornerRadius = 0
		passwordField.layer.borderWidth  = 0
		passwordField.isSecureTextEntry = true
		passwordField.layer.masksToBounds = true
		useridField.clearButtonMode = .whileEditing   //キャンセル
		passwordField.leftViewMode = .always    // 左の余白
		passwordField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: 10))   // 左の余白
		view.addSubview(passwordField)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ログインボタン
		let loginBtn = UIButton(frame: CGRect(x:20,y: 330,width: self.view.frame.width - 40, height:40))
		loginBtn.setTitle("ログイン", for: .normal)  //タイトル
		loginBtn.backgroundColor = appDelegate.ifNormalColor
		loginBtn.addTarget(self, action: #selector(LoginViewController.login(_:)), for: .touchUpInside)
		view.addSubview(loginBtn)  // Viewに追加
        
        let message = appDelegate.message

        if( message == "versionMismatch" ) {
            showAlertAutoHidden(title:"バージョン不一致", message: "新しいバージョンにアップグレードされたため、再度ログインが必要です。", showTime: 3)
        } else if ( message == "timeOver" ) {
            showAlertAutoHidden(title:"認証切れ", message: "認証の有効期限が切れたため、再度ログインが必要です。", showTime: 3)
        }
        
        // --------------------------------------------------------------------------------------------------------------------------
        // ローディング
        activityIndicatorView.center = view.center
        activityIndicatorView.style = .whiteLarge
        activityIndicatorView.color = .black
        
        view.addSubview(activityIndicatorView)
	}
    
	var container: NSPersistentContainer!	//データベース読み込みのコンテナ
	
    //
    // 時間制限付きアラート
    //
    func showAlertAutoHidden(title: Any, message: Any, showTime: Double) {
        
        // アラート作成
        let alert = UIAlertController(title: title as? String, message: message as? String, preferredStyle: .alert)
        
        // アラート表示
        self.present(alert, animated: true, completion: {
            // アラートを閉じる
            DispatchQueue.main.asyncAfter(deadline: .now() + showTime, execute: {
                alert.dismiss(animated: true, completion: nil)
            })
        })
    }
    
	// --------------------------------------------------------------------------------------------------------------------------
	// ログインボタンの動作
	@IBAction func login(_ sender: UIButton) {
		
        // ローディング開始
        activityIndicatorView.startAnimating()
        
		// 認証サーバーに問い合わせ
		ServerAuth_.connect()
		
		let sendtext = "AUTH,\(String(describing: useridField.text!)),\(String(describing: passwordField.text!))"
		
		_ = ServerAuth_.sendCommand(command: sendtext)
		let retval = ServerAuth_.sendCommand(command: "end")
		
//		self.dismissIndicator()
		
		let result:String = retval["auth"] as! String;
		
		// 認証が通った場合の処理
		if result == "true" {
			let engName:String = retval["engName"] as! String
			let jpnName:String = retval["jpnName"] as! String
			let key:String = retval["key"] as! String

			// UserDefaultを生成しuseridを保存
			let myUserDefault:UserDefaults = UserDefaults()
			let valUserid: String? = useridField.text!
			let valJpnName: String? = jpnName
			let valEngName: String? = engName
			let valKey: String? = key
			let timestamp:Int = Int(NSDate().timeIntervalSince1970) //unix timestampで認証日時を記録
//			print("\(timestamp): Auth Success.")
            
			myUserDefault.set(valUserid, forKey: "user")
			myUserDefault.set(valJpnName, forKey: "jpnName")
			myUserDefault.set(valEngName, forKey: "engName")
			myUserDefault.set(valKey, forKey: "key")
			myUserDefault.set("", forKey: "log")
			myUserDefault.set(timestamp, forKey: "timestamp")
			myUserDefault.set(0, forKey: "autosender")
			myUserDefault.set(0, forKey: "debug")
			
			// トップ画面に遷移
			let modalViewController = ViewController()
            self.navigationController?.pushViewController(modalViewController, animated: false)
			
		} else if result == "timeout" {
            
            self.activityIndicatorView.stopAnimating()
            
			print("Auth timeout.")
			
			// UIAlertControllerを作成する.
			let myAlert: UIAlertController = UIAlertController(title: "認証", message: "サーバーからの返答がありません。\nしばらくしてから改めて実行してください。", preferredStyle: .alert)
			
			// OKのアクションを作成する.
			let myOkAction = UIAlertAction(title: "OK", style: .default) { action in
				print("OK")
			}
			
			// Actionを追加する.
			myAlert.addAction(myOkAction)
			
			// UIAlertを発動する.
			present(myAlert, animated: true, completion: nil)
			
		} else {
            
            self.activityIndicatorView.stopAnimating()
            
			print("Auth Failure.")
			
			// UIAlertControllerを作成する.
			let myAlert: UIAlertController = UIAlertController(title: "認証", message: "IDまたはパスワードが間違っています", preferredStyle: .alert)
			
			// OKのアクションを作成する.
			let myOkAction = UIAlertAction(title: "OK", style: .default) { action in
				print("OK")
			}
			
			// Actionを追加する.
			myAlert.addAction(myOkAction)
			
			// UIAlertを発動する.
			present(myAlert, animated: true, completion: nil)
		}
	}
}

class ServerAuth: NSObject, StreamDelegate {
	
	let ServerAddress: CFString = appDelegate.serverIp as CFString //IPアドレスを指定
	let serverPort: UInt32 = UInt32(appDelegate.serverPort) //開放するポートを指定
	
	private var inputStream : InputStream!
	private var outputStream: OutputStream!
	
	func matches(for regex: String, in text: String) -> [String] {

		do {
			let regex = try NSRegularExpression(pattern: regex)
			let results = regex.matches(in: text,
										range: NSRange(text.startIndex..., in: text))
			return results.map {
				String(text[Range($0.range, in: text)!])
			}
		} catch let error {
			print("invalid regex: \(error.localizedDescription)")
			return []
		}
	}
	
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
		
		self.inputStream.schedule(in: RunLoop.current, forMode: RunLoop.Mode.default)
		self.outputStream.schedule(in: RunLoop.current, forMode: RunLoop.Mode.default)
		
		self.inputStream.open()
		self.outputStream.open()
		
		print("connect success!!")
	}
	
//    // inputStream/outputStreamに何かしらのイベントが起きたら起動してくれる関数
//    // 今回の場合では、同期型なのでoutputStreamの時しか起動してくれない
//    func stream(_ stream:Stream, handle eventCode : Stream.Event){
//        print(stream)
//    }
//
	// サーバーにコマンド文字列を送信する関数
	func sendCommand(command: String) -> Dictionary<String, Any> {
		
		var retval = Dictionary<String, Any>()
		
		var ccommand = command.data(using: String.Encoding.utf8, allowLossyConversion: false)!
		let commandLength = ccommand.count
		let text : String = ccommand.withUnsafeMutableBytes{ bytes in return String(bytesNoCopy: bytes, length: commandLength, encoding: String.Encoding.utf8, freeWhenDone: false)!}
		
        // "end"を受信したら接続切断
		if (String(describing: command) == "end") {
			
			self.outputStream.close()
			self.outputStream.remove(from: RunLoop.current, forMode: RunLoop.Mode.default)
			
            let read = NSMutableData()
            
            // inputStreamからバイトデータを読み込む
            var bytesRead = 1
            
            while(bytesRead > 0) {
                
//                LoginViewController().showLoading();
                
                let bufferSize = 4096
                var buffer = Array<UInt8>(repeating: 0, count: bufferSize)
                bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
                // データが断片化する可能性があるのでキューにためておく
                read.append(NSData(bytes: UnsafePointer(buffer), length: bytesRead) as Data)
                
//                sleep(UInt32(1))    //デバッグ
                print("bytesRead: \(bytesRead)")   // デバッグ
                print("hasBytesAvailable: \(inputStream.hasBytesAvailable)")   // デバッグ
            }
            
            // 受信したバイトデータを文字列に変換
            let readVal: String = String(data: read as Data, encoding: .utf8)!
//            print("---------------------> \(readVal)")  // デバッグ
            
			// 認証失敗
			if readVal.contains("authfail") {
				retval["auth"] = "false"

			// 認証成功
			} else {
				// 値を最後までリードできているかチェック
				// 最後の文字が"]"の場合は、データを正しく受信できていると判断
				if readVal.hasSuffix("]") {
					
					let splitRead = readVal.components(separatedBy: ",")
					retval["auth"] = "true"
					retval["key"] = splitRead[0]
					retval["engName"] = splitRead[1]
					retval["jpnName"] = splitRead[2]
					
					let splitRead2 = readVal.components(separatedBy: ",[")
					let varAps = "[" + String(splitRead2[1])
					
					// APデータ
					struct aps: Codable {
						let Name: String?
                        let Notes: String?
                        let Major: Int?
                        let Minor: Int?
					}
					
					// サーバーから取得した無線APの値をjsonデコード
					let apJson = try! JSONDecoder().decode([aps].self, from: varAps.data(using: .utf8)!)
					
					// データの削除
					let _ = appDelegate.deleteDbAll()

					for json in apJson {
                        if((json.Notes) != nil) {
//                        if((json.beaconIdParams) != nil) {
//                            // データ追加
                         let _ = appDelegate.putApData(name: json.Name!, notes: json.Notes!, major: json.Major!, minor: json.Minor!)
////                            let _ = appDelegate.putApData(name: json.Name, major: (json.beaconIdParams?.Major)!, minor: (json.beaconIdParams?.Minor)!)
////                            let _ = appDelegate.putApData(name: json.Name, notes: json.Notes, major: (json.beaconIdParams?.Major)!, minor: (json.beaconIdParams?.Minor)!)
                        }
					}
					
//					// データベースからデータを抽出（デバッグ用）
//                    let _ = appDelegate.getApData()

				} else {
					// サーバーからのデータ取得が出来ていない場合は認証失敗を返す（臨時対応）
					retval["auth"] = "false"
				}
			}
			
			self.inputStream.close()
			self.inputStream.remove(from: RunLoop.current, forMode: RunLoop.Mode.default)
		
		} else {
			// UnsafePointerを使うとうまくいかない場合がある（最初にダミーコマンドを送る必要があった）
			// self.outputStream.write(UnsafePointer(command), maxLength: text.utf8.count)
			self.outputStream.write( command, maxLength: text.utf8.count)
			print("Send: \(text)")
		}
		
		return retval
	}
}
