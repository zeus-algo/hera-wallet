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

package network.voi.hera.modules.collectibles.detail.ui.model

import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem
import network.voi.hera.modules.collectibles.detail.base.ui.model.CollectibleTraitItem
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.Event

data class NFTDetailPreview(
    val isLoadingVisible: Boolean,
    val nftName: AssetName,
    val collectionNameOfNFT: String?,
    val optedInAccountTypeDrawableResId: Int,
    val optedInAccountDisplayName: AccountDisplayName,
    val formattedNFTAmount: String,
    val mediaListOfNFT: List<BaseCollectibleMediaItem>,
    val traitListOfNFT: List<CollectibleTraitItem>?,
    val nftDescription: String?,
    val creatorAccountAddressOfNFT: String,
    val formattedCreatorAccountAddressOfNFT: String,
    val nftId: Long,
    val formattedTotalSupply: String,
    val peraExplorerUrl: String,
    val isPureNFT: Boolean,
    val primaryWarningResId: Int?,
    val secondaryWarningResId: Int?,
    val isSendButtonVisible: Boolean,
    val isOptOutButtonVisible: Boolean,
    val globalErrorEvent: Event<String>?,
    val fractionalCollectibleSendEvent: Event<Unit>?,
    val pureCollectibleSendEvent: Event<Unit>?,
    val optOutNFTEvent: Event<AssetInformation>?
)
