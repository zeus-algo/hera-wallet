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

//   CollectibleMediaPreviewViewController+Theme.swift

import MacaroonUIKit

extension CollectibleMediaPreviewViewController {
    struct Theme:
        StyleSheet,
        LayoutSheet {
        let cellSpacing: LayoutMetric
        let horizontalInset: LayoutMetric
        let pageControlScale: LayoutMetric
        let tap3DActionViewTopPadding: LayoutMetric
        let tap3DActionView: ButtonStyle

        init(
            _ family: LayoutFamily
        ) {
            self.cellSpacing = 12
            self.horizontalInset = 24
            self.pageControlScale = 0.5
            self.tap3DActionViewTopPadding = 16
            self.tap3DActionView = [
                .icon([.normal("icon-3d"), .highlighted("icon-3d")]),
                .titleColor([ .normal(Colors.Text.grayLighter) ]),
                .title(Self.getTitle())
            ]
        }
    }
}

extension CollectibleMediaPreviewViewController.Theme {
    private static func getTitle() -> EditText {
        return .attributedString(
            "collectible-detail-tap-3D"
                .localized
                .footnoteMedium()
        )
    }
}
