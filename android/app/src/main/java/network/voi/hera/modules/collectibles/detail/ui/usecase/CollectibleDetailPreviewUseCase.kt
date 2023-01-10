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

package network.voi.hera.modules.collectibles.detail.ui.usecase

import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.collectibles.detail.base.domain.decider.CollectibleDetailDecider
import network.voi.hera.modules.collectibles.detail.base.domain.usecase.GetCollectibleDetailUseCase
import network.voi.hera.modules.collectibles.detail.base.ui.mapper.CollectibleMediaItemMapper
import network.voi.hera.modules.collectibles.detail.base.ui.mapper.CollectibleTraitItemMapper
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem
import network.voi.hera.modules.collectibles.detail.base.ui.model.CollectibleTraitItem
import network.voi.hera.modules.collectibles.detail.ui.mapper.NFTDetailPreviewMapper
import network.voi.hera.modules.collectibles.detail.ui.model.NFTDetailPreview
import network.voi.hera.modules.collectibles.util.deciders.NFTAmountFormatDecider
import network.voi.hera.nft.domain.model.BaseCollectibleDetail
import network.voi.hera.usecase.AccountCollectibleDataUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetBaseOwnedAssetDataUseCase
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.Event
import javax.inject.Inject

open class CollectibleDetailPreviewUseCase @Inject constructor(
    private val nftDetailPreviewMapper: NFTDetailPreviewMapper,
    private val getCollectibleDetailUseCase: GetCollectibleDetailUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val accountCollectibleDataUseCase: AccountCollectibleDataUseCase,
    private val collectibleMediaItemMapper: CollectibleMediaItemMapper,
    private val collectibleTraitItemMapper: CollectibleTraitItemMapper,
    private val collectibleDetailDecider: CollectibleDetailDecider,
    private val getBaseOwnedAssetDataUseCase: GetBaseOwnedAssetDataUseCase,
    private val baseAssetDrawableProviderDecider: AssetDrawableProviderDecider,
    private val nftAmountFormatDecider: NFTAmountFormatDecider
) {

    fun getOptOutEventPreview(preview: NFTDetailPreview?, nftId: Long, accountAddress: String): NFTDetailPreview? {
        val assetInformation = getAssetInformationOfGivenNFT(
            nftId = nftId,
            accountAddress = accountAddress
        ) ?: return null
        return preview?.copy(optOutNFTEvent = Event(assetInformation))
    }

    fun getSendEventPreviewAccordingToNFTType(preview: NFTDetailPreview?): NFTDetailPreview? {
        val isPureNFT = preview?.isPureNFT ?: false
        return preview?.copy(
            fractionalCollectibleSendEvent = if (!isPureNFT) Event(Unit) else null,
            pureCollectibleSendEvent = if (isPureNFT) Event(Unit) else null
        )
    }

    fun getAssetInformationOfGivenNFT(nftId: Long, accountAddress: String): AssetInformation? {
        val ownedNFTData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(nftId, accountAddress) ?: return null
        return AssetInformation.createAssetInformation(
            baseOwnedAssetData = ownedNFTData,
            assetDrawableProvider = baseAssetDrawableProviderDecider.getAssetDrawableProvider(nftId)
        )
    }

    @SuppressWarnings("LongMethod")
    suspend fun getCollectibleDetailPreview(nftId: Long, accountAddress: String): NFTDetailPreview? {
        var nftDetailPreview: NFTDetailPreview? = null
        getCollectibleDetailUseCase.getCollectibleDetail(nftId).use(
            onSuccess = { baseNFTDetail ->
                val accountDetail = accountDetailUseCase.getCachedAccountDetail(accountAddress)?.data
                val collectibleDetail = accountCollectibleDataUseCase.getAccountCollectibleDetail(
                    accountAddress = accountAddress,
                    collectibleId = nftId
                )
                val isOwnedByTheUser = collectibleDetail?.isOwnedByTheUser ?: false
                val isCreatedByOwnerAccount = baseNFTDetail.assetCreator?.publicKey == accountDetail?.account?.address
                val isOwnedByWatchAccount = accountDetail?.account?.type == Account.Type.WATCH
                nftDetailPreview = nftDetailPreviewMapper.mapToNFTDetailPreview(
                    isLoadingVisible = false,
                    nftName = AssetName.create(baseNFTDetail.title ?: baseNFTDetail.fullName),
                    collectionNameOfNFT = baseNFTDetail.collectionName,
                    optedInAccountTypeDrawableResId = AccountIconResource.getAccountIconResourceByAccountType(
                        accountType = accountDetail?.account?.type
                    ).iconResId,
                    optedInAccountDisplayName = AccountDisplayName.create(
                        accountAddress = accountDetail?.account?.address.orEmpty(),
                        accountName = accountDetail?.account?.name.orEmpty(),
                        type = accountDetail?.account?.type
                    ),
                    formattedNFTAmount = nftAmountFormatDecider.decideNFTAmountFormat(
                        nftAmount = collectibleDetail?.amount,
                        fractionalDecimal = collectibleDetail?.decimals,
                        formattedAmount = collectibleDetail?.formattedAmount,
                        formattedCompactAmount = collectibleDetail?.formattedCompactAmount
                    ),
                    mediaListOfNFT = createNFTMediaList(
                        baseCollectibleDetail = baseNFTDetail,
                        shouldDecreaseOpacity = false
                    ),
                    traitListOfNFT = createNFTTraitList(
                        baseCollectibleDetail = baseNFTDetail
                    ),
                    nftDescription = baseNFTDetail.description,
                    creatorAccountOfNFT = baseNFTDetail.assetCreator?.publicKey.orEmpty(),
                    nftId = baseNFTDetail.assetId,
                    formattedTotalSupply = nftAmountFormatDecider.decideNFTAmountFormat(
                        nftAmount = baseNFTDetail.totalSupply,
                        fractionalDecimal = baseNFTDetail.fractionDecimals
                    ),
                    peraExplorerUrl = baseNFTDetail.explorerUrl.orEmpty(),
                    isPureNFT = baseNFTDetail.isPure(),
                    primaryWarningResId = collectibleDetailDecider.decideWarningTextRes(
                        prismUrl = baseNFTDetail.prismUrl
                    ),
                    secondaryWarningResId = collectibleDetailDecider.decideOptedInWarningTextRes(
                        isOwnedByTheUser = isOwnedByTheUser,
                        accountType = accountDetail?.account?.type
                    ),
                    isSendButtonVisible = isOwnedByTheUser && !isOwnedByWatchAccount,
                    isOptOutButtonVisible = !isOwnedByTheUser && !isCreatedByOwnerAccount && !isOwnedByWatchAccount
                )
            }
        )
        return nftDetailPreview
    }

    private fun createNFTMediaList(
        baseCollectibleDetail: BaseCollectibleDetail,
        shouldDecreaseOpacity: Boolean
    ): List<BaseCollectibleMediaItem> {
        return baseCollectibleDetail.collectibleMedias?.map {
            collectibleMediaItemMapper.mapToCollectibleMediaItem(
                baseCollectibleMedia = it,
                shouldDecreaseOpacity = shouldDecreaseOpacity,
                baseCollectibleDetail = baseCollectibleDetail,
                showMediaButtons = true
            )
        }.orEmpty()
    }

    private fun createNFTTraitList(baseCollectibleDetail: BaseCollectibleDetail): List<CollectibleTraitItem> {
        return baseCollectibleDetail.traits?.map { collectibleTraitItemMapper.mapToTraitItem(it) }.orEmpty()
    }
}
