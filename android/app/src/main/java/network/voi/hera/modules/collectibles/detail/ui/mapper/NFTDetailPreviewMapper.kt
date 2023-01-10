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

package network.voi.hera.modules.collectibles.detail.ui.mapper

import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem
import network.voi.hera.modules.collectibles.detail.base.ui.model.CollectibleTraitItem
import network.voi.hera.modules.collectibles.detail.ui.model.NFTDetailPreview
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.Event
import network.voi.hera.utils.toShortenedAddress
import javax.inject.Inject

class NFTDetailPreviewMapper @Inject constructor() {

    @SuppressWarnings("LongParameterList")
    fun mapToNFTDetailPreview(
        isLoadingVisible: Boolean,
        nftName: AssetName,
        collectionNameOfNFT: String?,
        optedInAccountTypeDrawableResId: Int,
        optedInAccountDisplayName: AccountDisplayName,
        formattedNFTAmount: String,
        mediaListOfNFT: List<BaseCollectibleMediaItem>,
        traitListOfNFT: List<CollectibleTraitItem>?,
        nftDescription: String?,
        creatorAccountOfNFT: String,
        nftId: Long,
        formattedTotalSupply: String,
        peraExplorerUrl: String,
        isPureNFT: Boolean,
        primaryWarningResId: Int?,
        secondaryWarningResId: Int?,
        isSendButtonVisible: Boolean,
        isOptOutButtonVisible: Boolean,
        globalErrorEvent: Event<String>? = null,
        fractionalCollectibleSendEvent: Event<Unit>? = null,
        pureCollectibleSendEvent: Event<Unit>? = null,
        optOutNFTEvent: Event<AssetInformation>? = null
    ): NFTDetailPreview {
        return NFTDetailPreview(
            isLoadingVisible = isLoadingVisible,
            nftName = nftName,
            collectionNameOfNFT = collectionNameOfNFT,
            optedInAccountTypeDrawableResId = optedInAccountTypeDrawableResId,
            optedInAccountDisplayName = optedInAccountDisplayName,
            formattedNFTAmount = formattedNFTAmount,
            mediaListOfNFT = mediaListOfNFT,
            traitListOfNFT = traitListOfNFT,
            nftDescription = nftDescription,
            formattedCreatorAccountAddressOfNFT = creatorAccountOfNFT.toShortenedAddress(),
            creatorAccountAddressOfNFT = creatorAccountOfNFT,
            nftId = nftId,
            formattedTotalSupply = formattedTotalSupply,
            peraExplorerUrl = peraExplorerUrl,
            isPureNFT = isPureNFT,
            primaryWarningResId = primaryWarningResId,
            secondaryWarningResId = secondaryWarningResId,
            globalErrorEvent = globalErrorEvent,
            fractionalCollectibleSendEvent = fractionalCollectibleSendEvent,
            pureCollectibleSendEvent = pureCollectibleSendEvent,
            isOptOutButtonVisible = isOptOutButtonVisible,
            isSendButtonVisible = isSendButtonVisible,
            optOutNFTEvent = optOutNFTEvent
        )
    }
}
