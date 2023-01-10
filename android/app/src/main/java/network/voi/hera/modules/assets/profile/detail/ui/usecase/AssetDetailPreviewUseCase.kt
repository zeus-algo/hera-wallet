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

package network.voi.hera.modules.assets.profile.detail.ui.usecase

import androidx.navigation.NavDirections
import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.discover.home.domain.model.TokenDetailInfo
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetAssetDetailUseCase
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetSelectedAssetExchangeValueUseCase
import network.voi.hera.modules.assets.profile.detail.domain.usecase.GetAccountAssetDetailUseCase
import network.voi.hera.modules.assets.profile.detail.ui.AssetDetailFragmentDirections
import network.voi.hera.modules.assets.profile.detail.ui.mapper.AssetDetailPreviewMapper
import network.voi.hera.modules.assets.profile.detail.ui.model.AssetDetailPreview
import network.voi.hera.modules.swap.reddot.domain.usecase.GetSwapFeatureRedDotVisibilityUseCase
import network.voi.hera.modules.swap.utils.SwapNavigationDestinationHelper
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.ALGO_SHORT_NAME
import network.voi.hera.utils.AlgoAssetInformationProvider
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.Event
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

class AssetDetailPreviewUseCase @Inject constructor(
    private val getAccountAssetDetailUseCase: GetAccountAssetDetailUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetDetailPreviewMapper: AssetDetailPreviewMapper,
    private val getSwapFeatureRedDotVisibilityUseCase: GetSwapFeatureRedDotVisibilityUseCase,
    private val swapNavigationDestinationHelper: SwapNavigationDestinationHelper,
    private val getAssetDetailUseCase: GetAssetDetailUseCase,
    private val getSelectedAssetExchangeValueUseCase: GetSelectedAssetExchangeValueUseCase,
    private val algoAssetInformationProvider: AlgoAssetInformationProvider
) {

    fun updatePreviewForDiscoverMarketEvent(currentPreview: AssetDetailPreview): AssetDetailPreview {
        val safeTokenId = if (currentPreview.assetId == ALGO_ID) ALGO_SHORT_NAME else currentPreview.assetId.toString()
        return currentPreview.copy(
            navigateToDiscoverMarket = Event(
                TokenDetailInfo(tokenId = safeTokenId, poolId = null)
            )
        )
    }

    suspend fun updatePreviewForNavigatingSwap(
        currentPreview: AssetDetailPreview,
        accountAddress: String
    ): Flow<AssetDetailPreview> = flow {
        var swapNavDirection: NavDirections? = null
        swapNavigationDestinationHelper.getSwapNavigationDestination(
            accountAddress = accountAddress,
            onNavToSwap = { address ->
                swapNavDirection = AssetDetailFragmentDirections.actionAssetDetailFragmentToSwapNavigation(address)
            },
            onNavToIntroduction = {
                swapNavDirection = AssetDetailFragmentDirections
                    .actionAssetDetailFragmentToSwapIntroductionNavigation(accountAddress)
            }
        )
        val newState = swapNavDirection?.let { direction ->
            currentPreview.copy(swapNavigationDirectionEvent = Event(direction))
        } ?: currentPreview
        emit(newState)
    }

    suspend fun initAssetDetailPreview(
        accountAddress: String,
        assetId: Long,
        isQuickActionButtonsVisible: Boolean
    ): Flow<AssetDetailPreview> {
        return combine(
            getAccountAssetDetailUseCase.getAssetDetail(accountAddress, assetId).filterNotNull(),
            accountDetailUseCase.getAccountDetailCacheFlow(accountAddress).filterNotNull(),
            getAssetDetailUseCase.getAssetDetail(assetId)
        ) { baseOwnedAssetDetail, cachedAccountDetail, assetDetailResult ->
            val account = cachedAccountDetail.data?.account
            val isSwapButtonSelected = getRedDotVisibility(baseOwnedAssetDetail.isAlgo)
            // TODO Check Error and Loading cases later
            val assetDetail = if (assetId != ALGO_ID) {
                (assetDetailResult as? DataResource.Success)?.data
            } else {
                algoAssetInformationProvider.getAlgoAssetInformation().data
            }
            val isAvailableOnDiscoverMobile = assetDetail?.isAvailableOnDiscoverMobile ?: false
            val formattedAssetPrice = getSelectedAssetExchangeValueUseCase.getSelectedAssetExchangeValue(assetDetail)
                ?.getFormattedValue(isCompact = true)
            val isMarketInformationVisible = isAvailableOnDiscoverMobile &&
                baseOwnedAssetDetail.verificationTier != VerificationTier.SUSPICIOUS &&
                assetDetail?.hasUsdValue() == true
            assetDetailPreviewMapper.mapToAssetDetailPreview(
                baseOwnedAssetDetail = baseOwnedAssetDetail,
                accountAddress = accountAddress,
                accountName = account?.name.orEmpty(),
                accountType = account?.type,
                canAccountSignTransaction = accountDetailUseCase.canAccountSignTransaction(accountAddress),
                isQuickActionButtonsVisible = isQuickActionButtonsVisible,
                isSwapButtonSelected = isSwapButtonSelected,
                isMarketInformationVisible = isMarketInformationVisible,
                last24HoursChange = assetDetail?.last24HoursAlgoPriceChangePercentage,
                formattedAssetPrice = formattedAssetPrice

            )
        }.distinctUntilChanged()
    }

    private suspend fun getRedDotVisibility(isAlgo: Boolean): Boolean {
        return getSwapFeatureRedDotVisibilityUseCase.getSwapFeatureRedDotVisibility() && isAlgo
    }
}
