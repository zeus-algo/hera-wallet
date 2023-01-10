/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package network.voi.hera.nft.ui.model

import network.voi.hera.models.AssetTransaction
import network.voi.hera.models.BaseSelectAssetItem
import network.voi.hera.modules.collectibles.detail.ui.model.CollectibleDetail
import network.voi.hera.utils.Event

data class AssetSelectionPreview(
    val assetTransaction: AssetTransaction,
    val assetList: List<BaseSelectAssetItem>?,
    val navigateToOptInEvent: Event<Long>?,
    val globalErrorTextEvent: Event<String>?,
    val navigateToAssetTransferAmountFragmentEvent: Event<Long>?,
    val isAssetListLoadingVisible: Boolean,
    val isReceiverAccountOptInCheckLoadingVisible: Boolean,
    val navigateToCollectibleSendFragmentEvent: Event<CollectibleDetail>?
)
