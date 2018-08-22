//
//  LogViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit

class LogViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	var Connection = Connection2()
	
	// Tableで使用する配列を設定する
	private var myItems: Array<Any> = []
	private var myTableView: UITableView!
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		Connection.connect()
		
		// UserDefaultの生成.
		let myUserDefault:UserDefaults = UserDefaults()
		// 登録されているUserDefaultから設定値を呼び出す.
		let userName:String = myUserDefault.string(forKey: "user")!
		let key:String = myUserDefault.string(forKey: "key")!
		
		// socket通信
		let sendtext = "\(userName),\(key)"
		let tmp = Connection.sendCommand(command: sendtext)
		
		// keysを昇順でソートする
		let retval = tmp.sorted(){ $0.0 < $1.0 }
		
		// ログデータを取得しTableViewに渡す
		for (_, value) in retval {
			myItems += ["\(value)"]
		}
		
		print("retval:\n\(retval)")  //デバッグ
		
		// Status Barの高さを取得する.
		let barHeight: CGFloat = UIApplication.shared.statusBarFrame.size.height
		
		// Viewの高さと幅を取得する.
		let displayWidth: CGFloat = self.view.frame.width
		let displayHeight: CGFloat = self.view.frame.height
		
		// TableViewの生成(Status barの高さをずらして表示).
		myTableView = UITableView(frame: CGRect(x: 0, y: barHeight + 75, width: displayWidth, height: displayHeight - 135))
		
		// セルの高さ
//		myTableView.estimatedRowHeight = 100
//		myTableView.rowHeight = UITableViewAutomaticDimension
		myTableView.rowHeight = 100
		
		// Cell名の登録をおこなう.
		myTableView.register(MyCell.self, forCellReuseIdentifier: NSStringFromClass(MyCell.self))
		
		// DataSourceを自身に設定する.
		myTableView.dataSource = self
		
		// Delegateを自身に設定する.
		myTableView.delegate = self
		
		// Viewに追加する.
		self.view.addSubview(myTableView)
		
		
		// --------------------------------------------------------------------------------------------------------------------------
		// タイトル
		let labelTitle = UILabel(frame: CGRect(x:0, y: barHeight + 5, width:self.view.frame.width, height:30))
		labelTitle.font = UIFont.systemFont(ofSize: 18.0)    //フォントサイズ
		labelTitle.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
		labelTitle.textAlignment = NSTextAlignment.center    // センター寄せ
		labelTitle.text = "ログ表示"
		view.addSubview(labelTitle)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ローカルログボタン
		let localLogButton = UIButton(frame: CGRect(x:0,y: Int(barHeight + 35),width: Int(self.view.frame.width),height:40))
		localLogButton.setTitle("ローカルログに切り替える", for: .normal)  //タイトル
		localLogButton.backgroundColor = appDelegate.ifNormalColor
		localLogButton.addTarget(self, action: #selector(LogViewController.back(_:)), for: .touchUpInside)
		view.addSubview(localLogButton)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 戻るボタン
		let backButton = UIButton(frame: CGRect(x:0,y: Int(self.view.frame.height - 40),width: Int(self.view.frame.width),height:40))
		backButton.setTitle("閉じる", for: .normal)  //タイトル
		backButton.backgroundColor = appDelegate.ifNormalColor
		backButton.addTarget(self, action: #selector(LogViewController.back(_:)), for: .touchUpInside)
		view.addSubview(backButton)  // Viewに追加
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
		// Dispose of any resources that can be recreated.
	}
	
	// Cellが選択された際に呼び出される
	func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
		print("Num: \(indexPath.row)")
		print("Value: \(myItems[indexPath.row])")
	}

	// Cellの総数を返す
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return myItems.count
	}
	
	// Cellに値を設定する
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// 再利用するCellを取得する.
		let cell = tableView.dequeueReusableCell(withIdentifier: NSStringFromClass(MyCell.self), for: indexPath) as! MyCell
		
		// 値を取得してカンマで分割
		let splitRead = (myItems[indexPath.row] as AnyObject).components(separatedBy: ",")
		
//		cell.textLabel!.text = textVal
		cell.labelUUid.text = splitRead[0]
		cell.labelMode.text = splitRead[1]
		cell.labelDate.text = splitRead[2]
		cell.labelVal1.text = "major:\(splitRead[3]) minor:\(splitRead[4]) rssi:\(splitRead[5]) "
		cell.labelVal2.text = "major:\(splitRead[6]) minor:\(splitRead[7]) rssi:\(splitRead[8]) "
		cell.labelVal3.text = "major:\(splitRead[9]) minor:\(splitRead[10]) rssi:\(splitRead[11]) "
		
		return cell
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// 戻るボタンの動作
	@objc func back(_ sender: UIButton) {// selectorで呼び出す場合Swift4からは「@objc」をつける。
		self.dismiss(animated: true, completion: nil)
	}
}

class MyCell: UITableViewCell {
	var labelUUid: UILabel!
	var labelMode: UILabel!
	var labelDate: UILabel!
	var labelVal1: UILabel!
	var labelVal2: UILabel!
	var labelVal3: UILabel!
	
	override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
		super.init(style: style, reuseIdentifier: reuseIdentifier)
		
		labelUUid = UILabel(frame: CGRect.zero)
		contentView.addSubview(labelUUid)
		
		labelMode = UILabel(frame: CGRect.zero)
		contentView.addSubview(labelMode)
		
		labelDate = UILabel(frame: CGRect.zero)
		labelDate.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
		contentView.addSubview(labelDate)
		
		labelVal1 = UILabel(frame: CGRect.zero)
		contentView.addSubview(labelVal1)
		
		labelVal2 = UILabel(frame: CGRect.zero)
		contentView.addSubview(labelVal2)
		
		labelVal3 = UILabel(frame: CGRect.zero)
		contentView.addSubview(labelVal3)
	}
	
	required init(coder aDecoder: NSCoder) {
		fatalError("init(coder: ) has not been implemented")
	}
	
	override func prepareForReuse() {
		super.prepareForReuse()
	}
	
	override func layoutSubviews() {
		super.layoutSubviews()
		labelDate.frame = CGRect(x: 10, y: -40, width: frame.width, height: frame.height)
		labelMode.frame = CGRect(x: frame.width - 30, y: -40, width: frame.width, height: frame.height)
		labelUUid.frame = CGRect(x: 20, y: -20, width: frame.width, height: frame.height)
		labelVal1.frame = CGRect(x: 20, y: 0, width: frame.width, height: frame.height)
		labelVal2.frame = CGRect(x: 20, y: 20, width: frame.width, height: frame.height)
		labelVal3.frame = CGRect(x: 20, y: 40, width: frame.width, height: frame.height)
	}
	
}

// --------------------------------------------------------------------------------------------------------------------------
// SSL Socketコネクション

class Connection2: NSObject, StreamDelegate {
	
	let ServerAddress: CFString = appDelegate.serverIp as CFString //IPアドレスを指定
	let serverPort: UInt32 = UInt32(appDelegate.serverPort2) //開放するポートを指定
	
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
	func sendCommand(command: String) -> Dictionary<Int, Any> {
		
		// 返り値を定義
		var retval = Dictionary<Int, String>()
		var countCommand = command.data(using: String.Encoding.utf8, allowLossyConversion: false)!
		
		let bytes : String = countCommand.withUnsafeMutableBytes{
			bytes in return String(bytesNoCopy: bytes, length: countCommand.count, encoding: String.Encoding.utf8, freeWhenDone: false)!
		}
		
		// エラー処理
		var timeout = 5 * 100000 // タイムアウト値は5秒
		while !self.outputStream.hasSpaceAvailable {
			usleep(1000) // wait until the socket is ready
			timeout -= 100
			if timeout < 0 {
				print("time out")
				retval[0] = "Error: time out."
				self.inputStream.close()
				self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				self.outputStream.close()
				self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				return retval // disconnectStream will be called.
			} else if self.outputStream.streamError != nil {
				print("disconnect Stream")
				retval[0] = "Error: disconnect Stream."
				self.inputStream.close()
				self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				self.outputStream.close()
				self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
				return retval
			}
		}
		
		print("write")
		// コマンドをサーバーに送信
		self.outputStream.write( command, maxLength: bytes.utf8.count)
		
		
		print("Send: \(command)")
		
		self.outputStream.close()
		self.outputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		
		//        while(!inputStream.hasBytesAvailable){}   //不要かどうか確認中（2018/08/19）
		let bufferSize = 2048
		var buffer = Array<UInt8>(repeating: 0, count: bufferSize)
		let bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
		if (bytesRead >= 0) {
			//            let read = String(bytes: buffer, encoding: String.Encoding.utf8)!
			let read = NSString(bytes: &buffer, length: bytesRead, encoding: String.Encoding.utf8.rawValue)!
			print("Receive:\n\(read)")   //デバッグ
			
			// ログがない場合
			if read.contains("Error") {
				retval[0] = "Error: Log not found."
				
				// ログを配列に格納
			} else {
				let splitRead = read.components(separatedBy: "\n")
				var num = 0
				for str in splitRead {
					retval[num] = str
					num = num + 1
				}
			}
		}
		self.inputStream.close()
		self.inputStream.remove(from: RunLoop.current, forMode: RunLoopMode.defaultRunLoopMode)
		
		return retval
	}
}
