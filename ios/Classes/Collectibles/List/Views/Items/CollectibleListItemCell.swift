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

//   CollectibleListItemCell.swift

import UIKit
import MacaroonUIKit

final class CollectibleListItemCell:
    CollectionCell<CollectibleListItemView>,
    ViewModelBindable {
    var isPending: Bool = false {
        didSet {
            if oldValue == isPending {
                return
            }

            contextView.setPendingHiddenWhenPendingStatusChange(isPending)
        }
    }

    static let theme = CollectibleListItemViewTheme()

    override init(
        frame: CGRect
    ) {
        super.init(frame: frame)
        contextView.customize(Self.theme)
    }

    override func prepareForReuse() {
        contextView.prepareForReuse()

        isPending = false
    }

    func getTargetedPreview() -> UITargetedPreview {
        return contextView.getTargetedPreview()
    }
}
