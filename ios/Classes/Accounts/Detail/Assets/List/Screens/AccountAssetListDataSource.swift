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
//   AccountAssetListDataSource.swift

import Foundation
import MacaroonUIKit
import UIKit

final class AccountAssetListDataSource: UICollectionViewDiffableDataSource<AccountAssetsSection, AccountAssetsItem> {
    lazy var handlers = Handlers()

    init(
        _ collectionView: UICollectionView
    ) {
        super.init(collectionView: collectionView) {
            collectionView, indexPath, itemIdentifier in

            switch itemIdentifier {
            case let .portfolio(item):
                let cell = collectionView.dequeue(AccountPortfolioCell.self, at: indexPath)
                cell.bindData(item)
                return cell
            case let .watchPortfolio(item):
                let cell = collectionView.dequeue(WatchAccountPortfolioCell.self, at: indexPath)
                cell.bindData(item)
                return cell
            case .assetManagement(let item):
                let cell = collectionView.dequeue(
                    ManagementItemWithSecondaryActionCell.self,
                    at: indexPath
                )
                cell.bindData(
                    item
                )
                return cell
            case .watchAccountAssetManagement(let item):
                let cell = collectionView.dequeue(
                    ManagementItemCell.self,
                    at: indexPath
                )
                cell.bindData(
                    item
                )
                return cell
            case .search:
                return collectionView.dequeue(SearchBarItemCell.self, at: indexPath)
            case let .asset(item):
                let cell = collectionView.dequeue(AssetListItemCell.self, at: indexPath)
                cell.bindData(item)
                return cell
            case let .pendingAsset(item):
                let cell = collectionView.dequeue(PendingAssetPreviewCell.self, at: indexPath)
                cell.bindData(item)
                return cell
            case .quickActions:
                let cell = collectionView.dequeue(
                    AccountQuickActionsCell.self,
                    at: indexPath
                )
                return cell
            case .empty(let item):
                let cell = collectionView.dequeue(NoContentCell.self, at: indexPath)
                cell.bindData(item)
                return cell
            }
        }

        [
            AccountPortfolioCell.self,
            WatchAccountPortfolioCell.self,
            ManagementItemWithSecondaryActionCell.self,
            ManagementItemCell.self,
            SearchBarItemCell.self,
            AssetListItemCell.self,
            PendingAssetPreviewCell.self,
            AccountQuickActionsCell.self,
            NoContentCell.self
        ].forEach {
            collectionView.register($0)
        }
    }
}

extension AccountAssetListDataSource: AddAssetItemViewDelegate {
    func addAssetItemViewDidTapAddAsset(_ addAssetItemView: AddAssetItemView) {
        handlers.didAddAsset?()
    }
}

extension AccountAssetListDataSource {
    struct Handlers {
        var didAddAsset: EmptyHandler?
    }
}
