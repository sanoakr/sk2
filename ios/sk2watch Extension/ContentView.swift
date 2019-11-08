//
//  ContentView.swift
//  sk2watch Extension
//
//  Created by sano on 20191019.
//  Copyright © 2019 関本達生. All rights reserved.
//

import SwiftUI
import WatchConnectivity

struct ContentView: View {
    @State private var btState = true
    @State private var btText = "理工出席"
    @ObservedObject var obVals = ObservableValues()

    //func setText(text: String) { self.sentText = text }
    
    var body: some View {
        VStack {
            Button(action: {
                self.btState = !self.btState
                if self.btState {
                    self.btText = "理工出席"
                    //self.sentText = "A"
                } else {
                    self.btText = "sk2"
                    //self.sentText = "B"
                }
                let message = ["msg": "attend"]
                WCSession.default.sendMessage(message,
                    replyHandler: {reply in print(reply)},
                    errorHandler: {error in print(error)})
                }){
                    Text(btText)
                        .frame(width: 100, height: 100)
                        .font(.headline)
                }
                .background(Color(UIColor(red: 0.16, green: 0.50, blue: 0.73, alpha: 1)))
                .frame(width: 100, height: 100)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.gray, lineWidth: 1))
            /*
            Text(obVals.info)
                .fixedSize(horizontal: false, vertical: true)
                .lineLimit(nil)
                .font(.footnote)
            */
        }
    }
}

class ObservableValues: ObservableObject {
    @Published var info = "info"
    func setInfo(text: String) { info = text }
}

#if DEBUG
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
#endif
