// Copyright 2022 Pera Wallet, LDA

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//   ScrollScreen.swift

import Foundation
import MacaroonUIKit

class ScrollScreen: MacaroonUIKit.ScrollScreen {
    var leftBarButtonItems: [BarButtonItemRef] = []
    var rightBarButtonItems: [BarButtonItemRef] = []

    override func viewDidLoad() {
        super.viewDidLoad()

        setNeedsNavigationBarAppearanceUpdate()
    }

    override func configureNavigationBar() {
        super.configureNavigationBar()

        /// note
        /// Navigation Bar Controller life cycle is added by default in Macaroon's Screen
        /// In order to prevent conflicts, navigation bar controller life cycle removed
        /// Otherwise multiple back buttons can be added
        remove(lifeCycleObserver: navigationBarController)
    }
}

extension ScrollScreen: NavigationBarConfigurable {
    typealias BarButtonItemRef = ALGBarButtonItem
}
