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

package network.voi.hera.modules.assets.profile.about.ui.usecase

import androidx.annotation.StringRes
import network.voi.hera.R
import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.assetsearch.domain.model.VerificationTier.SUSPICIOUS
import network.voi.hera.assetsearch.domain.model.VerificationTier.TRUSTED
import network.voi.hera.assetsearch.domain.model.VerificationTier.UNVERIFIED
import network.voi.hera.assetsearch.domain.model.VerificationTier.VERIFIED
import network.voi.hera.models.AssetCreator
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.BaseAssetDetail
import network.voi.hera.modules.assets.profile.about.domain.usecase.CacheAssetDetailToAsaProfileLocalCacheUseCase
import network.voi.hera.modules.assets.profile.about.domain.usecase.ClearAsaProfileLocalCacheUseCase
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetAssetDetailFlowFromAsaProfileLocalCache
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetSelectedAssetExchangeValueUseCase
import network.voi.hera.modules.assets.profile.about.ui.mapper.AssetAboutPreviewMapper
import network.voi.hera.modules.assets.profile.about.ui.mapper.BaseAssetAboutListItemMapper
import network.voi.hera.modules.assets.profile.about.ui.model.AssetAboutPreview
import network.voi.hera.modules.assets.profile.about.ui.model.BaseAssetAboutListItem
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.browser.addProtocolIfNeed
import network.voi.hera.utils.browser.removeProtocolIfNeed
import network.voi.hera.utils.formatAmount
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class AssetAboutPreviewUseCase @Inject constructor(
    private val cacheAssetDetailToAsaProfileLocalCacheUseCase: CacheAssetDetailToAsaProfileLocalCacheUseCase,
    private val getAssetDetailFlowFromAsaProfileLocalCache: GetAssetDetailFlowFromAsaProfileLocalCache,
    private val clearAsaProfileLocalCacheUseCase: ClearAsaProfileLocalCacheUseCase,
    private val assetAboutPreviewMapper: AssetAboutPreviewMapper,
    private val baseAssetAboutListItemMapper: BaseAssetAboutListItemMapper,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val getSelectedAssetExchangeValueUseCase: GetSelectedAssetExchangeValueUseCase
) {

    fun clearAsaProfileLocalCache() {
        clearAsaProfileLocalCacheUseCase.clearAsaProfileLocalCache()
    }

    suspend fun cacheAssetDetailToAsaProfileLocalCache(assetId: Long) {
        cacheAssetDetailToAsaProfileLocalCacheUseCase.cacheAssetDetailToAsaProfileLocalCache(assetId)
    }

    fun getAssetAboutPreview(assetId: Long) = flow {
        emit(assetAboutPreviewMapper.mapToAssetAboutPreviewInitialState())
        if (assetId == ALGO_ID) {
            val cachedAlgoAssetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(assetId)
            cachedAlgoAssetDetail?.useSuspended(
                onSuccess = { cachedAlgoDetail ->
                    val algoAboutPreview = cachedAlgoDetail.data?.run { createAlgoAboutPreview(this) }
                    emit(algoAboutPreview)
                }
            )
        } else {
            getAssetDetailFlowFromAsaProfileLocalCache.getAssetDetailFlowFromAsaProfileLocalCache()
                .collect { cacheResult ->
                    cacheResult?.useSuspended(
                        onSuccess = { cachedAssetDetail ->
                            cachedAssetDetail.data?.let { assetDetail ->
                                emit(createAssetAboutPreview(assetDetail))
                            }
                        }
                    )
                }
        }
    }

    private fun createAlgoAboutPreview(assetDetail: BaseAssetDetail): AssetAboutPreview {
        val algorandAboutList = mutableListOf<BaseAssetAboutListItem>().apply {
            with(assetDetail) {
                add(createStatisticsItem(this))
                add(BaseAssetAboutListItem.DividerItem)
                add(
                    createAboutAssetItem(
                        fullName = fullName,
                        assetId = null,
                        assetCreator = null,
                        explorerUrl = null,
                        projectUrl = null,
                        asaUrl = url
                    )
                )
                add(BaseAssetAboutListItem.DividerItem)
                add(createAlgoDescriptionItem(R.string.the_algo_is_the_official_cryptocurrency))
                createSocialMediaItem(discordUrl, telegramUrl, twitterUsername)?.run {
                    add(BaseAssetAboutListItem.DividerItem)
                    add(this)
                }
                addVerificationTierDescriptionIfNeed(this@apply, verificationTier)
            }
        }
        return assetAboutPreviewMapper.mapToAssetAboutPreview(assetAboutListItems = algorandAboutList)
    }

    private fun createAssetAboutPreview(assetDetail: BaseAssetDetail): AssetAboutPreview {
        val assetAboutList = mutableListOf<BaseAssetAboutListItem>().apply {
            with(assetDetail) {

                add(createStatisticsItem(this))
                add(BaseAssetAboutListItem.DividerItem)

                add(createAboutAssetItem(fullName, assetId, assetCreator, explorerUrl, projectUrl, url))

                createAssetDescriptionItem(assetDescription)?.run {
                    add(BaseAssetAboutListItem.DividerItem)
                    add(this)
                }

                createSocialMediaItem(discordUrl, telegramUrl, twitterUsername)?.run {
                    add(BaseAssetAboutListItem.DividerItem)
                    add(this)
                }
                addReportItemIfNeed(this@apply, verificationTier, assetId, shortName)
                addVerificationTierDescriptionIfNeed(this@apply, verificationTier)
            }
        }
        return assetAboutPreviewMapper.mapToAssetAboutPreview(assetAboutListItems = assetAboutList)
    }

    private fun addVerificationTierDescriptionIfNeed(
        assetAboutList: MutableList<BaseAssetAboutListItem>,
        verificationTier: VerificationTier
    ) {
        val position = when (verificationTier) {
            TRUSTED, VERIFIED -> assetAboutList.indexOfFirst { it is BaseAssetAboutListItem.AboutAssetItem } + 1
            SUSPICIOUS -> assetAboutList.indexOfFirst { it is BaseAssetAboutListItem.StatisticsItem }
            UNVERIFIED -> null
        }
        val item = when (verificationTier) {
            VERIFIED -> BaseAssetAboutListItem.BadgeDescriptionItem.VerifiedBadgeItem
            TRUSTED -> BaseAssetAboutListItem.BadgeDescriptionItem.TrustedBadgeItem
            SUSPICIOUS -> BaseAssetAboutListItem.BadgeDescriptionItem.SuspiciousBadgeItem
            UNVERIFIED -> null
        }
        if (item != null && position != null) {
            assetAboutList.add(position, item)
        }
    }

    private fun addReportItemIfNeed(
        mutableList: MutableList<BaseAssetAboutListItem>,
        verificationTier: VerificationTier,
        assetId: Long,
        shortName: String?
    ) {
        if (verificationTier != TRUSTED) {
            mutableList.add(BaseAssetAboutListItem.DividerItem)
            mutableList.add(createReportItem(assetId, shortName))
        }
    }

    private fun createStatisticsItem(assetDetail: BaseAssetDetail): BaseAssetAboutListItem.StatisticsItem {
        with(assetDetail) {
            val formattedAssetPrice = getSelectedAssetExchangeValueUseCase
                .getSelectedAssetExchangeValue(assetDetail = this)
                ?.getFormattedValue()
            return baseAssetAboutListItemMapper.mapToStatisticsItem(
                formattedPriceText = formattedAssetPrice,
                formattedCompactTotalSupplyText = totalSupply?.formatAmount(
                    decimals = fractionDecimals ?: DEFAULT_ASSET_DECIMAL,
                    isCompact = true,
                    isDecimalFixed = false
                )
            )
        }
    }

    private fun createAboutAssetItem(
        fullName: String?,
        assetId: Long?,
        assetCreator: AssetCreator?,
        explorerUrl: String?,
        projectUrl: String?,
        asaUrl: String?
    ): BaseAssetAboutListItem.AboutAssetItem {
        return baseAssetAboutListItemMapper.mapToAboutAssetItem(
            assetName = AssetName.create(fullName),
            assetId = assetId,
            assetCreatorAddress = assetCreator?.publicKey,
            asaUrl = asaUrl.addProtocolIfNeed(),
            displayAsaUrl = asaUrl.removeProtocolIfNeed(),
            peraExplorerUrl = explorerUrl,
            projectWebsiteUrl = projectUrl
        )
    }

    private fun createAssetDescriptionItem(
        assetDescription: String?
    ): BaseAssetAboutListItem.BaseAssetDescriptionItem.AssetDescriptionItem? {
        if (assetDescription.isNullOrBlank()) return null
        return baseAssetAboutListItemMapper.mapToAssetDescriptionItem(descriptionText = assetDescription)
    }

    private fun createAlgoDescriptionItem(
        @StringRes descriptionTextResId: Int
    ): BaseAssetAboutListItem.BaseAssetDescriptionItem.AlgoDescriptionItem {
        return baseAssetAboutListItemMapper.mapToAlgoDescriptionItem(descriptionTextResId = descriptionTextResId)
    }

    private fun createSocialMediaItem(
        discordUrl: String?,
        telegramUrl: String?,
        twitterUsername: String?
    ): BaseAssetAboutListItem.SocialMediaItem? {
        if (discordUrl.isNullOrBlank() && telegramUrl.isNullOrBlank() && twitterUsername.isNullOrBlank()) return null
        return baseAssetAboutListItemMapper.mapToSocialMediaItem(
            discordUrl = discordUrl,
            telegramUrl = telegramUrl,
            twitterUsername = twitterUsername
        )
    }

    private fun createReportItem(assetId: Long, shortName: String?): BaseAssetAboutListItem.ReportItem {
        return baseAssetAboutListItemMapper.mapToReportItem(
            assetName = AssetName.createShortName(shortName),
            assetId = assetId
        )
    }
}
