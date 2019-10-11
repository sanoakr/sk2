//
//  LogViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/20.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit

@available(iOS 13.0, *)
class LogViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	// Status Barの高さを取得する.
	let barHeight: CGFloat = UIApplication.shared.statusBarFrame.size.height
	let safeAreaInsets = UIApplication.shared.keyWindow?.rootViewController?.view.safeAreaInsets.bottom
	
	var Connection = Connection2()
	
	// Tableで使用する配列を設定する
	private var myItems: Array<Any> = []
	private var myTableView: UITableView!
	
	// UserDefaultの生成
	let myUserDefault:UserDefaults = UserDefaults()
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		// --------------------------------------------------------------------------------------------------------------------------
		// タイトル
		let labelTitle = UILabel(frame: CGRect(x:0, y: barHeight + 5, width:self.view.frame.width, height:30))
		labelTitle.font = UIFont.systemFont(ofSize: 18.0)    //フォントサイズ
		labelTitle.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
		labelTitle.textAlignment = NSTextAlignment.center    // センター寄せ
		labelTitle.text = "ログ"
		view.addSubview(labelTitle)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// UISegmentedControlの表示
		
		// ボタンの表記を配列で作成
		let array = ["サーバーログ","ローカルログ"]
		
		// UISegmentedControlのインスタンス作成
		let segment: UISegmentedControl = UISegmentedControl(items: array as [AnyObject])
		
		// segmentの位置を設定
		segment.frame = CGRect(x: 20, y: Int(barHeight + 55), width: Int(self.view.frame.width) - 40, height: 30)
		
		// 背景色を設定
        segment.tintColor = appDelegate.setColor ( name : "ifNormalColor" )
		
		// ボタンを押した時の処理を設定
		segment.addTarget(self, action: #selector(LogViewController.change(segment:)), for: UIControl.Event.valueChanged)
		
		// ViewにsegmentをsubViewとして追加
		self.view.addSubview(segment)
		
		// 初期値のセット
		segment.selectedSegmentIndex = 0
		
		// サーバーログを表示
		showServerLog()

		// --------------------------------------------------------------------------------------------------------------------------
		// 戻るボタン
		let backButton = UIButton(frame: CGRect(x:0,y: Int(self.view.frame.height - safeAreaInsets! - 40),width: Int(self.view.frame.width),height:40))
		backButton.setTitle("閉じる", for: .normal)  //タイトル
        backButton.backgroundColor = appDelegate.setColor ( name : "ifNormalColor" )
		backButton.addTarget(self, action: #selector(LogViewController.back(_:)), for: .touchUpInside)
		view.addSubview(backButton)  // Viewに追加
		
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
		// Dispose of any resources that can be recreated.
	}
	
	@objc func showServerLog() {
		Connection.connect()
		
		// セルを削除
		myItems.removeAll()
		
		// UserDefaultの生成.
		let myUserDefault:UserDefaults = UserDefaults()
		// 登録されているUserDefaultから設定値を呼び出す.
		let userName:String = myUserDefault.string(forKey: "user")!
		let key:String = myUserDefault.string(forKey: "key")!
		
		// socket通信
		let sendtext = "\(userName),\(key)"
        _ = Connection.sendCommand(command: sendtext)
        let tmp = Connection.sendCommand(command: "end")

		// keysを昇順でソートする
		let retval = tmp.sorted(){ $0.0 < $1.0 }
		
		//配列を初期化
		myItems = Array()
		
		// ログデータを取得しTableViewに渡す
		for (_, value) in retval {
			myItems += ["\(value)"]
		}
		
//		print("myItems:\n\(myItems)")  //デバッグ
		
		// Viewの高さと幅を取得する.
		let displayWidth: CGFloat = self.view.frame.width
		let displayHeight: CGFloat = self.view.frame.height
		let safeAreaInsets = UIApplication.shared.keyWindow?.rootViewController?.view.safeAreaInsets.bottom
		
		// TableViewの生成(Status barの高さをずらして表示).
		let tableHeight = displayHeight - safeAreaInsets! - barHeight - 135
//        let tableHeight = displayHeight - safeAreaInsets! - barHeight - 96
		myTableView = UITableView(frame: CGRect(x: 0, y: barHeight + 95, width: displayWidth, height: tableHeight))
		
		// セルの高さ
		myTableView.rowHeight = 120
		
		// Cell名の登録をおこなう.
		myTableView.register(MyCell.self, forCellReuseIdentifier: NSStringFromClass(MyCell.self))
		
		// DataSourceを自身に設定する.
		myTableView.dataSource = self
		
		// Delegateを自身に設定する.
		myTableView.delegate = self

		// リフレッシュ
		// UIRefreshControlのインスタンス作成
		let refresh = UIRefreshControl()
		myTableView.refreshControl = refresh
		
		// リフレッシュしたときの処理内容を設定
		refresh.addTarget(self, action: #selector(LogViewController.serverLogRefresh(sender:)), for: .valueChanged)
		
		// Viewに追加する.
		self.view.addSubview(myTableView)
	}
	
	//サーバーログをリフレッシュ
	@objc func serverLogRefresh(sender: UIRefreshControl) {
		//1秒待ってから処理を始める
		DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
			//サーバーログを表示
			self.showServerLog()
			//refreshを閉じる
			sender.endRefreshing()
		})
	}
	
	@objc func showLocalLog() {
		print("ローカルログを表示")
		
		// 一旦テーブルを削除
		myItems.removeAll()
//		myTableView.removeFromSuperview()
		
		// ローカルログの取得
		if myUserDefault.array(forKey: "log") != nil {

			let localLog:Array = myUserDefault.array(forKey: "log")!
			print("localLog: \(localLog)")
			
			// 配列を初期化
			myItems = Array()
			
			// ログデータを取得しTableViewに渡す
			myItems = localLog
			
		} else {
			myItems.removeAll()
			myItems.append(",,値がありません,,,,,,,,,")
		}
		
		// Viewの高さと幅を取得する.
		let displayWidth: CGFloat = self.view.frame.width
		let displayHeight: CGFloat = self.view.frame.height
		
		// TableViewの生成(Status barの高さをずらして表示).
		let tableHeight = displayHeight - safeAreaInsets! - barHeight - 135
//        let tableHeight = displayHeight - safeAreaInsets! - barHeight - 96
		myTableView = UITableView(frame: CGRect(x: 0, y: barHeight + 95, width: displayWidth, height: tableHeight))

		// セルの高さ
		myTableView.rowHeight = 120
		
		// Cell名の登録をおこなう.
		myTableView.register(MyCell.self, forCellReuseIdentifier: NSStringFromClass(MyCell.self))
		
		// DataSourceを自身に設定する.
		myTableView.dataSource = self
		
		// Delegateを自身に設定する.
		myTableView.delegate = self
		
		// リフレッシュ
		// UIRefreshControlのインスタンス作成
		let refresh = UIRefreshControl()
		myTableView.refreshControl = refresh
		
		// リフレッシュしたときの処理内容を設定
		refresh.addTarget(self, action: #selector(LogViewController.localLogRefresh(sender:)), for: .valueChanged)
		
		// Viewに追加する.
		self.view.addSubview(myTableView)
	}
	
	// ローカルログをリフレッシュ
	@objc func localLogRefresh(sender: UIRefreshControl) {
		//1秒待ってから処理を始める
		DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
			//サーバーログを表示
			self.showLocalLog()
			//refreshを閉じる
			sender.endRefreshing()
		})
	}
	
	// ボタンを押した時の処理
	@objc func change(segment:UISegmentedControl){
		
		// ボタンごとの処理をswitch文で処理
		switch segment.selectedSegmentIndex {
			
		case 0:
			print("サーバーログ")
			showServerLog()

		case 1:
			print("ローカルログ")
			showLocalLog()
			
		default:
			print("デフォルト")
		}
		
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
		
		// 配列数をチェック
		if( splitRead.count == 12 ) {
			
			// AP名を取得
			var ap1 = "-"
			if( !splitRead[3].isEmpty && !splitRead[4].isEmpty ) {
				ap1 = appDelegate.getBeaconName( major: splitRead[3], minor: splitRead[4])
			}
			
			var ap2 = "-"
			if( !splitRead[6].isEmpty && !splitRead[7].isEmpty ) {
				ap2 = appDelegate.getBeaconName( major: splitRead[6], minor: splitRead[7])
			}

			var ap3 = "-"
			if( !splitRead[9].isEmpty && !splitRead[10].isEmpty ) {
				ap3 = appDelegate.getBeaconName( major: splitRead[9], minor: splitRead[10])
			}
			
			cell.labelDate.text = " \(splitRead[2])"
            cell.labelMode.text = String(splitRead[1].prefix(1))
			cell.labelVal1.text = "ap:\(String(describing: ap1)) major:\(splitRead[3]) minor:\(splitRead[4]) RSSI:\(splitRead[5])"
			cell.labelVal2.text = "ap:\(String(describing: ap2)) major:\(splitRead[6]) minor:\(splitRead[7]) RSSI:\(splitRead[8])"
			cell.labelVal3.text = "ap:\(String(describing: ap3)) major:\(splitRead[9]) minor:\(splitRead[10]) RSSI:\(splitRead[11])"
			
		} else {
			if myItems.count == 1 {
				cell.labelDate.text = " 値がありません"
				cell.labelVal1.text = "major: minor: rssi: "
				cell.labelVal2.text = "major: minor: rssi: "
				cell.labelVal3.text = "major: minor: rssi: "
			} else {
				cell.labelDate.text = " 値が不正です"
				cell.labelVal1.text = "major: minor: rssi: "
				cell.labelVal2.text = "major: minor: rssi: "
				cell.labelVal3.text = "major: minor: rssi: "
			}
		}
		return cell
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// 戻るボタンの動作
	@objc func back(_ sender: UIButton) {
		self.dismiss(animated: true, completion: nil)
	}
}

class MyCell: UITableViewCell {
	var labelMode: UILabel!
	var labelDate: UILabel!
	var labelVal1: UILabel!
	var labelVal2: UILabel!
	var labelVal3: UILabel!
	
	override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
		super.init(style: style, reuseIdentifier: reuseIdentifier)
		
		labelMode = UILabel()
        if #available(iOS 13.0, *) {
            labelMode.backgroundColor = appDelegate.setColor ( name : "backgroundColor" )
        } else {
            labelMode.backgroundColor = UIColor.white
        }
		contentView.addSubview(labelMode)
		
		labelDate = UILabel()
		labelDate.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
        if #available(iOS 13.0, *) {
            labelDate.backgroundColor = appDelegate.setColor ( name : "backgroundColor" )
        } else {
            labelDate.backgroundColor = UIColor.white
        }
		contentView.addSubview(labelDate)
		
		labelVal1 = UILabel()
		labelVal1.font = UIFont.systemFont(ofSize: 11)
		contentView.addSubview(labelVal1)
		
		labelVal2 = UILabel()
		labelVal2.font = UIFont.systemFont(ofSize: 11)
		contentView.addSubview(labelVal2)
		
		labelVal3 = UILabel()
		labelVal3.font = UIFont.systemFont(ofSize: 11)
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
		labelDate.frame = CGRect(x: 0, y: 0, width: frame.width - 30, height: 40)
		labelMode.frame = CGRect(x: frame.width - 30, y: 0, width: 30, height: 40)
		labelVal1.frame = CGRect(x: 20, y: 50, width: frame.width, height: 20)
		labelVal2.frame = CGRect(x: 20, y: 70, width: frame.width, height: 20)
		labelVal3.frame = CGRect(x: 20, y: 90, width: frame.width, height: 20)
	}
	
}

// --------------------------------------------------------------------------------------------------------------------------
// SSL Socketコネクション

@available(iOS 13.0, *)
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
		
		self.inputStream.schedule(in: RunLoop.current, forMode: RunLoop.Mode.default)
		self.outputStream.schedule(in: RunLoop.current, forMode: RunLoop.Mode.default)
		
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
        
		var ccommand = command.data(using: String.Encoding.utf8, allowLossyConversion: false)!
		let commandLength = ccommand.count
		let text : String = ccommand.withUnsafeMutableBytes{
			bytes in return String(bytesNoCopy: bytes, length: commandLength, encoding: String.Encoding.utf8, freeWhenDone: false)!
		}
		
        
        // "end"を受信したら接続切断
        if (String(describing: command) == "end") {
            
            self.outputStream.close()
            self.outputStream.remove(from: RunLoop.current, forMode: RunLoop.Mode.default)
            
            let read = NSMutableData()
            
            // inputStreamからバイトデータを読み込む
            var bytesRead = 1
            
            while(bytesRead > 0) {
                          
                let bufferSize = 4096
                var buffer = Array<UInt8>(repeating: 0, count: bufferSize)
                bytesRead = inputStream.read(&buffer, maxLength: bufferSize)
                // データが断片化する可能性があるのでキューにためておく
                read.append(NSData(bytes: UnsafePointer(buffer), length: bytesRead) as Data)
                
//                sleep(UInt32(1))    //デバッグ
                print("bytesRead: \(bytesRead)")   // デバッグ
                print("hasBytesAvailable: \(inputStream.hasBytesAvailable)")   // デバッグ
                
                let readVal: String = String(data: read as Data, encoding: .utf8)!

                // ログがない場合
                if readVal.contains("Error") {
                    retval[0] = "Error: Log not found."
    
                    // ログを配列に格納
                } else {
                    let splitRead = readVal.components(separatedBy: "\n")
                    var num = 0
                    for str in splitRead {
                        retval[num] = str
                        num = num + 1
                    }
                }
                
            }
            
        } else {
            // UnsafePointerを使うとうまくいかない場合がある（最初にダミーコマンドを送る必要があった）
            // self.outputStream.write(UnsafePointer(command), maxLength: text.utf8.count)
            self.outputStream.write( command, maxLength: text.utf8.count)
            print("Send: \(text)")
        }
        
		return retval
	}
}
