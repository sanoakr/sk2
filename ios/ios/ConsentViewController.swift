//
//  ConsentViewController.swift
//  ios
//
//  Created by 関本達生 on 2018/09/07.
//  Copyright © 2018年 関本達生. All rights reserved.
//

import UIKit

class ConsentViewController: UIViewController,UIWebViewDelegate {
	
	// AppDelegateのインスタンスを取得
	let appDelegate: AppDelegate = UIApplication.shared.delegate as! AppDelegate
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		// ナビゲーションバーを非表示
		navigationController?.setNavigationBarHidden(true, animated: false)
		
		// 背景色をセット
		self.view.backgroundColor = appDelegate.backgroundColor
		
		// Status Barの高さを取得
		let barHeight: CGFloat = UIApplication.shared.statusBarFrame.size.height
		
		// Viewの高さと幅を取得
		let displayWidth: CGFloat = self.view.frame.width
		let displayHeight: CGFloat = self.view.frame.height
		
		// footer tool barの高さを取得
		let navigationBarHeight = navigationController!.navigationBar.frame.size.height
		let toolbarHeight = navigationController!.toolbar.frame.size.height
		print("barHeight: \(barHeight)")
		
		// --------------------------------------------------------------------------------------------------------------------------
		// タイトル
		let labelTitle = UILabel(frame: CGRect(x:0, y: barHeight + 5, width:self.view.frame.width, height:30))
		labelTitle.font = UIFont.systemFont(ofSize: 18.0)    //フォントサイズ
		labelTitle.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)	//  ボールド
		labelTitle.textAlignment = NSTextAlignment.center    // センター寄せ
		labelTitle.text = "プライバシーポリシー"
		view.addSubview(labelTitle)  // Viewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// 規約文
		let myTextView: UITextView = UITextView(frame: CGRect(x:10, y:barHeight + 35, width:displayWidth - 20, height:(displayHeight - navigationBarHeight - barHeight - toolbarHeight) ))	// TextView生成
		myTextView.text = appDelegate.consentText	// 表示コンテンツ
		myTextView.font = UIFont.systemFont(ofSize: 14.0)	// フォントの設定
		myTextView.textColor = UIColor.black	// フォントの色の設定
		myTextView.dataDetectorTypes = UIDataDetectorTypes.all	// リンク、日付などを自動的に検出してリンクに変換
		myTextView.isEditable = false	// テキストを編集不可
		self.view.addSubview(myTextView)	// TextViewをViewに追加
		
		// --------------------------------------------------------------------------------------------------------------------------
		// ボタン
		// 配置するボタンを宣言
		let cancelButton = UIBarButtonItem(title: "同意しない", style: .plain, target: self, action: #selector(ConsentViewController.Cancel(_:)))
		let okButton = UIBarButtonItem(title: "同意する", style: .plain, target: self, action: #selector(ConsentViewController.Ok(_:)))
		let flexibleSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
		
		self.toolbarItems = [cancelButton, flexibleSpace, okButton]	// フッターに配置
		self.navigationController?.setToolbarHidden(false, animated: false)	// フッターの非表示を無効化
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	// ボタンの挙動
	
	// 同意しないボタン
	@objc func Cancel(_ sender: UIButton){
//		print("cancek")
		// すべてのローカルデータを消去
		let appDomain = Bundle.main.bundleIdentifier
		UserDefaults.standard.removePersistentDomain(forName: appDomain!)
		UserDefaults.standard.synchronize()
		// ログイン画面を表示
		let vc = LoginViewController()
		self.navigationController?.pushViewController(vc, animated: false)
		self.navigationController?.setNavigationBarHidden(false, animated: false)	// ナビゲーションバーを表示
		self.navigationController?.setToolbarHidden(true, animated: true)	// フッターの非表示
	}
	
	// 同意するボタン
	@objc func Ok(_ sender: UIButton){
//		print("ok")
		// useDefaultsに同意フラグ
		UserDefaults.standard.set(true, forKey: "consent")
        
        // ローカルにバージョンとビルドを保存
        UserDefaults.standard.set(appDelegate.currentVersion, forKey: "currentVersion")
        UserDefaults.standard.set(appDelegate.currentBuild, forKey: "currentBuild")
        
		// メイン画面を表示
		let vc = ViewController()
		self.navigationController?.pushViewController(vc, animated: false)
		self.navigationController?.setNavigationBarHidden(false, animated: false)	// ナビゲーションバーを表示
		self.navigationController?.setToolbarHidden(true, animated: true)	// フッターの非表示
	}
	
	override func didReceiveMemoryWarning() {
		super.didReceiveMemoryWarning()
		// Dispose of any resources that can be recreated.
	}
}
