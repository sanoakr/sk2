//
//  SplashViewController.swift
//  ios
//
//  Created by 関本達生 on 2018/08/26.
//  Copyright © 2018年 関本達生. All rights reserved.
//

import UIKit
import WebKit

class SplashViewController: UIViewController,UIWebViewDelegate {
	
	// UL
	var labelTitle : UILabel!
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	private var myWebView: UIWebView!
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		// スプラッシュ画面を表示
		self.view.backgroundColor = appDelegate.ifNormalColor // 背景色をセット
		navigationController?.setNavigationBarHidden(true, animated: false)
		
		// --------------------------------------------------------------------------------------------------------------------------
		// タイトルを生成
		labelTitle = UILabel(frame: CGRect(x:0, y:self.view.frame.height / 2 - 30, width:self.view.frame.width, height:30))
		labelTitle.font = UIFont.systemFont(ofSize: 20.0)    //フォントサイズ
		labelTitle.textAlignment = NSTextAlignment.center    // センター寄せ
		labelTitle.text = appDelegate.appTitle
		labelTitle.textColor = UIColor.white
		view.addSubview(labelTitle)  // Viewに追加
		
		// UserDefaultの生成.
		let myUserDefault:UserDefaults = UserDefaults()
		// 登録されているUserDefaultから設定値を呼び出す
		let user = myUserDefault.string(forKey: "user")
		let key = myUserDefault.string(forKey: "key")
		
		// 1秒待って処理を行う
		DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
			// ユーザーかキーがセットされていない場合
			if(user == nil || key == nil) {
				// ログイン画面を表示
				let vc = LoginViewController()
				self.navigationController?.pushViewController(vc, animated: false)
				
			} else {

				// メイン画面を表示
				let vc = ViewController()
				self.navigationController?.pushViewController(vc, animated: false)
				self.navigationController?.setNavigationBarHidden(false, animated: false)
				
				// 認証した場合はタイムスタンプを確認
				let timestamp:Int = myUserDefault.integer(forKey: "timestamp")
				let term = Int(NSDate().timeIntervalSince1970) - timestamp

				print("term:\(term)")

				if(term > self.appDelegate.timeLimit) {
					print("時間切れ")
					// ログイン画面を表示
					let vc = LoginViewController()
					self.navigationController?.pushViewController(vc, animated: false)
					myUserDefault.set(nil, forKey: "timestamp")
				}
			}
		})
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
		// Dispose of any resources that can be recreated.
	}
}
