//
//  NotificationController.swift
//  sk2watch Extension
//
//  Created by sano on 20191019.
//  Copyright © 2019 関本達生. All rights reserved.
//

import WatchKit
import SwiftUI
import UserNotifications

class NotificationController: WKUserNotificationHostingController<NotificationView> {
    var info: String!
    
    override var body: NotificationView {
        return NotificationView(info: info ?? "no data")
    }

    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
    }

    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }

    override func didReceive(_ notification: UNNotification) {
        info = notification.request.content.body
        print(info ?? "no data")
    }
}
