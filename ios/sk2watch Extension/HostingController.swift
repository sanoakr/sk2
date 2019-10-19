//
//  HostingController.swift
//  sk2watch Extension
//
//  Created by sano on 20191019.
//  Copyright © 2019 関本達生. All rights reserved.
//

import WatchKit
import Foundation
import SwiftUI
import WatchConnectivity

class HostingController: WKHostingController<ContentView> {
    
    // WCSession
    var wcsession: WCSession?
    
    override var body: ContentView {
        wcActivate(session: &wcsession)
        return ContentView()
    }
}

extension HostingController: WCSessionDelegate {
    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
    }
    
    // WCSession Activate
    func wcActivate(session: inout WCSession?) -> Void {
        if (WCSession.isSupported()) {
            session = WCSession.default
            if let session_t = session {
                session_t.delegate = self
                session_t.activate()
            }
        }
    }
    // reply Handler
    func session(_ session: WCSession, didReceiveMessage message: [String : Any], replyHandler: @escaping ([String : Any]) -> Void){
        let obj0 = message["msg0"] as? String
        print(obj0 ?? "no_data") // testが出力される

        replyHandler(["reply" : "OK"])
    }
}
