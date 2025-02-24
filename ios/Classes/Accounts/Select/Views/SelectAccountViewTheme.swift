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

//
//   SelectAccountViewTheme.swift


import Foundation
import MacaroonUIKit
import UIKit

struct SelectAccountViewTheme: StyleSheet, LayoutSheet {
    let background: ViewStyle
    let searchInputViewTheme: SearchInputViewTheme
    let clipboardTheme: AccountClipboardViewTheme
    let nextButtonStyle: ButtonPrimaryTheme
    let searchInputViewTopPadding: LayoutMetric
    let horizontalPadding: LayoutMetric
    let cellSpacing: LayoutMetric
    let contentInsetTopForClipboard: LayoutMetric
    let clipboardPaddings: LayoutPaddings
    let nextButtonHeight: LayoutMetric
    let bottomInset: LayoutMetric

    init(_ family: LayoutFamily) {
        self.background = [
            .backgroundColor(Colors.Defaults.background)
        ]
        self.searchInputViewTheme = QRSearchInputViewTheme(
            placeholder: "account-select-header-search-title".localized,
            family: family
        )
        self.clipboardTheme = AccountClipboardViewTheme(family)
        self.nextButtonStyle = ButtonPrimaryTheme(family)
        self.searchInputViewTopPadding = 16
        self.horizontalPadding = 24
        self.cellSpacing = 0
        self.contentInsetTopForClipboard = 112
        self.clipboardPaddings = (20, 0, 0, 0)
        self.nextButtonHeight = 50
        self.bottomInset = 16
    }
}
