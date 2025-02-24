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

//   AppCallAssetPreviewViewModel.swift

import Foundation
import MacaroonUIKit

struct AppCallAssetPreviewViewModel:
    ViewModel {
    private(set) var title: EditText?
    private(set) var accessoryIcon: Image?
    private(set) var subtitle: EditText?

    init(
        asset: Asset
    ) {
        bindTitle(asset)
        bindAccessoryIcon(asset)
        bindSubtitle(asset)
    }
}

extension AppCallAssetPreviewViewModel {
    mutating func bindTitle(
        _ asset: Asset
    ) {
        let name = asset.naming.name

        var attributes = Typography.bodyRegularAttributes(lineBreakMode: .byTruncatingTail)

        if asset.verificationTier.isSuspicious {
            attributes.insert(.textColor(Colors.Helpers.negative))
        } else {
            attributes.insert(.textColor(Colors.Text.main))
        }

        let aTitle = name.isNilOrEmpty ? "title-unknown".localized : name!

        title = .attributedString(
            aTitle.attributed(attributes)
        )
    }

    mutating func bindAccessoryIcon(
        _ asset: Asset
    ) {
        switch asset.verificationTier {
        case .trusted: accessoryIcon = "icon-trusted"
        case .verified: accessoryIcon = "icon-verified"
        case .unverified: accessoryIcon = nil
        case .suspicious: accessoryIcon = "icon-suspicious"
        }
    }

    mutating func bindSubtitle(
        _ asset: Asset
    ) {
        subtitle = .attributedString(
            String(asset.id)
                .footnoteRegular(
                    lineBreakMode: .byTruncatingTail
                )
        )
    }
}
