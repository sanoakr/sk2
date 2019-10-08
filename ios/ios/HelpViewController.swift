//
//  helpViewController.swift
//  attend
//
//  Created by 関本達生 on 2017/12/22.
//  Copyright © 2017年 関本達生. All rights reserved.
//

import UIKit
import WebKit

@available(iOS 13.0, *)
class HelpViewController: UIViewController,UIWebViewDelegate {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	private var myWebView: UIWebView!
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		// Status Barの高さを取得する.
		let barHeight: CGFloat = UIApplication.shared.statusBarFrame.size.height
		let safeAreaInsets = UIApplication.shared.keyWindow?.rootViewController?.view.safeAreaInsets.bottom
		
		// Viewの高さと幅を取得する.
		let displayWidth: CGFloat = self.view.frame.width
		let displayHeight: CGFloat = self.view.frame.height
		// WebViewの生成.
		myWebView = UIWebView(frame: CGRect(x: 0, y: barHeight + 40, width: displayWidth, height: displayHeight - barHeight - safeAreaInsets!  - 80))
		
		// Deletegateを自身に設定.
		myWebView.delegate = self
		
		// URLを設定.
		let helpUrl: URL = URL(string: appDelegate.helpUrl)!
		
		// リエストを発行する.
		let request: NSURLRequest = NSURLRequest(url: helpUrl)
		
		// リクエストを発行する.
		myWebView.loadRequest(request as URLRequest)
		
		// Viewに追加する
		self.view.addSubview(myWebView)
		
		// --------------------------------------------------------------------------------------------------------------------------
		// タイトル
		let labelTitle = UILabel(frame: CGRect(x:0, y: barHeight + 5, width:self.view.frame.width, height:30))
		labelTitle.font = UIFont.systemFont(ofSize: 18.0)    //フォントサイズ
		labelTitle.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
		labelTitle.textAlignment = NSTextAlignment.center    // センター寄せ
		labelTitle.text = "ヘルプ"
		view.addSubview(labelTitle)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 戻るボタン
		
		let backButton = UIButton(frame: CGRect(x:0,y: Int(self.view.frame.height - safeAreaInsets! - 40),width: Int(self.view.frame.width),height:40))
		
		backButton.setTitle("閉じる", for: .normal)  //タイトル
		backButton.backgroundColor = appDelegate.ifNormalColor
		backButton.addTarget(self, action: #selector(LogViewController.back(_:)), for: .touchUpInside)
		view.addSubview(backButton)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// インジケーター
		// スタイルは「.white」「.whiteLarge」「.gray」から選択可能
		appDelegate.ActivityIndicator = UIActivityIndicatorView(style: .gray)
		// ストップした時に非表示する
		appDelegate.ActivityIndicator?.hidesWhenStopped = true
		// ビューの中央に配置
		appDelegate.ActivityIndicator?.center = view.center
		// ビューに追加する
		view.addSubview((appDelegate.ActivityIndicator)!)
		
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
		// Dispose of any resources that can be recreated.
	}
	
	// Pageが全て読み終わったら呼ばれる.
	func webViewDidFinishLoad(_ webView: UIWebView) {
		appDelegate.ActivityIndicator?.stopAnimating()
		print("webViewDidFinishLoad")
	}
	
	// PageがLoadされ始めた時、呼ばれる.
	func webViewDidStartLoad(_ webView: UIWebView) {
		appDelegate.ActivityIndicator?.startAnimating()
		print("webViewDidStartLoad")
	}
	
	// 戻るボタンの動作
	@objc func back(_ sender: UIButton) {// selectorで呼び出す場合Swift4からは「@objc」をつける。
		self.dismiss(animated: true, completion: nil)
	}
}
