//
//  NotificationView.swift
//  sk2watch Extension
//
//  Created by sano on 20191019.
//  Copyright © 2019 関本達生. All rights reserved.
//

import SwiftUI

struct NotificationView: View {
    let info: String

    var body: some View {
        Text(info)
    }
}

#if DEBUG
struct NotificationView_Previews: PreviewProvider {
    static var previews: some View {
        NotificationView(info: "Preview Text")
    }
}
#endif
