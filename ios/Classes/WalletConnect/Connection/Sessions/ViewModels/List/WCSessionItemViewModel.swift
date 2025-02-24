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
//   WCSessionItemViewModel.swift

import CoreGraphics
import MacaroonUIKit
import SwiftDate
import MacaroonURLImage

struct WCSessionItemViewModel: PairedViewModel {
    private(set) var image: ImageSource?
    private(set) var name: EditText?
    private(set) var description: EditText?
    private(set) var date: EditText?
    private(set) var status: EditText?

    init(_ session: WCSession) {
        bindImage(session)
        bindName(session)
        bindDescription(session)
        bindDate(session)
        bindStatus(session)
    }
}

extension WCSessionItemViewModel {
    private mutating func bindImage(_ session: WCSession) {
        let placeholderImages: [Image] = [
            "icon-session-placeholder-1",
            "icon-session-placeholder-2",
            "icon-session-placeholder-3",
            "icon-session-placeholder-4"
        ]

        let placeholder = ImagePlaceholder(
            image: AssetImageSource(asset: placeholderImages.randomElement()!.uiImage)
        )

        image = PNGImageSource(
            url: session.peerMeta.icons.first,
            size: .resize(CGSize(width: 40, height: 40), .aspectFit),
            shape: .circle,
            placeholder: placeholder
        )
    }

    private mutating func bindName(_ session: WCSession) {
        name = .attributedString(
            session.peerMeta.name
                .bodyMedium(
                    lineBreakMode: .byTruncatingTail
                )
        )
    }

    private mutating func bindDescription(_ session: WCSession) {
        guard let aDescription = session.peerMeta.description,
              !aDescription.isEmptyOrBlank else {
            return
        }

        description = .attributedString(
            aDescription
                .footnoteRegular()
        )
    }

    private mutating func bindDate(_ session: WCSession) {
        let formattedDate = session.date.toFormat("MMMM dd, yyyy - HH:mm")

        date = .attributedString(
            formattedDate
                .footnoteRegular(
                    lineBreakMode: .byTruncatingTail
                )
        )
    }

    private mutating func bindStatus(_ session: WCSession) {
        let aStatus: String

        if let connectedAccount = session.walletMeta?.accounts?.first {
            aStatus =
            "wallet-connect-session-connected-with-account".localized(params: connectedAccount.shortAddressDisplay)
        } else {
            aStatus = "wallet-connect-session-connected".localized
        }

        status = .attributedString(
            aStatus
                .footnoteMedium(
                    alignment: .center,
                    lineBreakMode: .byTruncatingTail
                )
        )
    }
}
