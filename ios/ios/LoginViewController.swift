//
//  LoginViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/25.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	var Connection1 = Connection()
	
	// ボタンを宣言
	var leftBarButton: UIBarButtonItem!
	
	@IBOutlet weak var useridField: UITextField!
	@IBOutlet weak var passwordField: UITextField!
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
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
		useridField.bounds.insetBy(dx: 10, dy: 10)
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
		passwordField.leftViewMode = UITextFieldViewMode.always
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
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ログインボタンの動作
	@IBAction func login(_ sender: UIButton) {
		
		// ここでサーバーにuseridとpasswordを問い合わせて認証する
		
		Connection1.connect()
		
		let sendtext = "AUTH,\(String(describing: useridField.text!)),\(String(describing: passwordField.text!))"
		
		_ = Connection1.sendCommand(command: sendtext)
		let retval = Connection1.sendCommand(command: "end")
		
		print(retval)
		
		let result:String = retval["auth"] as! String;
		
		// 認証が通った場合の処理
		if result == "true" {
			let engName:String = retval["engName"] as! String;
			let jpnName:String = retval["jpnName"] as! String;
			let key:String = retval["key"] as! String;
			
			// UserDefaultを生成しuseridを保存
			let myUserDefault:UserDefaults = UserDefaults()
			let valUserid: String? = useridField.text!
			let valJpnName: String? = jpnName
			let valEngName: String? = engName
			let valKey: String? = key
			let timestamp:Int = Int(NSDate().timeIntervalSince1970) //unix timestampで認証日時を記録
			
			print("\(timestamp): Auth Success.")
			
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
			let navigationController = UINavigationController(rootViewController: modalViewController)
			self.present(navigationController, animated: true , completion: nil)
			
		} else if result == "timeout" {
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

class Connection: NSObject, StreamDelegate {
	
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
		
		var retval = Dictionary<String, String>()
		
		var ccommand = command.data(using: String.Encoding.utf8, allowLossyConversion: false)!
		let text : String = ccommand.withUnsafeMutableBytes{ bytes in return String(bytesNoCopy: bytes, length: ccommand.count, encoding: String.Encoding.utf8, freeWhenDone: false)!}
		
		// エラー処理
		var timeout = 5 * 100000 // タイムアウト値は5秒
		
		while !self.outputStream.hasSpaceAvailable {
			usleep(1000) // wait until the socket is ready
			timeout -= 100
			
			if (timeout < 0 || self.outputStream.streamError != nil) {
				print("time out")
				retval["auth"] = "timeout"
				
				self.inputStream.close()
				self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				self.outputStream.close()
				self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				return retval // disconnectStream will be called.
			}
		}
		
		// "end"を受信したら接続切断
		if (String(describing: command) == "end") {
			
			self.outputStream.close()
			self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
			
			//            while(!inputStream.hasBytesAvailable){}   //不要かどうか確認中（2018/08/19）
			let bufferSize = 1024
			var buffer = Array<UInt8>(repeating: 0, count: bufferSize)
			let bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
			if (bytesRead >= 0) {
				//                let read = String(bytes: buffer, encoding: String.Encoding.utf8)!
				let read = NSString(bytes: &buffer, length: bytesRead, encoding: String.Encoding.utf8.rawValue)!
				// デバッグ
				print("Receive: \(read)")
				
				// 認証失敗
				if read.contains("authfail") {
					retval["auth"] = "false"
					
					// 認証成功
				} else {
					let splitRead = read.components(separatedBy: ",")
					retval["auth"] = "true"
					retval["key"] = splitRead[0]
					retval["engName"] = splitRead[1]
					retval["jpnName"] = splitRead[2]
				}
			}
			self.inputStream.close()
			self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		} else {
			// UnsafePointerを使うとうまくいかない場合がある（最初にダミーコマンドを送る必要があった）
			// self.outputStream.write(UnsafePointer(command), maxLength: text.utf8.count)
			self.outputStream.write( command, maxLength: text.utf8.count)
			print("Send: \(text)")
		}
		
		return retval
		
	}
}
