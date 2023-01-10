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

package network.voi.hera.modules.swap.assetswap.ui.usecase

import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapType
import network.voi.hera.modules.swap.assetswap.ui.model.AssetSwapPreview
import network.voi.hera.modules.swap.assetswap.ui.utils.SwapAmountUtils
import network.voi.hera.modules.swap.slippagetolerance.ui.util.DEFAULT_SLIPPAGE_TOLERANCE
import network.voi.hera.usecase.CheckUserHasAssetBalanceUseCase
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.emptyString
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class AssetSwapAssetsSwitchUpdatePreviewUseCase @Inject constructor(
    private val assetSwapPreviewAssetDetailUseCase: AssetSwapPreviewAssetDetailUseCase,
    private val checkUserHasAssetBalanceUseCase: CheckUserHasAssetBalanceUseCase,
    private val assetSwapCreateQuotePreviewUseCase: AssetSwapCreateQuotePreviewUseCase
) {

    fun getAssetsSwitchedUpdatedPreview(
        fromAssetId: Long,
        toAssetId: Long,
        amount: String?,
        accountAddress: String,
        swapType: SwapType,
        previousState: AssetSwapPreview
    ) = flow<AssetSwapPreview> {
        emit(previousState.copy(isLoadingVisible = true))
        val fromAssetDetail = assetSwapPreviewAssetDetailUseCase.createFromSelectedAssetDetail(
            fromAssetId = fromAssetId,
            accountAddress = accountAddress,
            previousState = previousState
        )
        val toAssetDetail = assetSwapPreviewAssetDetailUseCase.createToSelectedAssetDetail(
            toAssetId = toAssetId,
            accountAddress = accountAddress,
            previousState = previousState
        )
        val newState = previousState.copy(
            fromSelectedAssetDetail = fromAssetDetail,
            toSelectedAssetDetail = toAssetDetail,
            isLoadingVisible = false,
            isSwitchAssetsButtonEnabled = checkUserHasAssetBalanceUseCase.hasUserAssetBalance(accountAddress, toAssetId)
        )

        if (!SwapAmountUtils.isAmountValidForApiRequest(amount)) {
            emit(newState)
        } else {
            val assetDecimal = if (swapType == SwapType.FIXED_INPUT) {
                fromAssetDetail.assetDecimal
            } else {
                toAssetDetail?.assetDecimal ?: DEFAULT_ASSET_DECIMAL
            }
            val swapQuoteUpdatedPreview = assetSwapCreateQuotePreviewUseCase.getSwapQuoteUpdatedPreview(
                accountAddress = accountAddress,
                fromAssetId = fromAssetId,
                toAssetId = toAssetId,
                amount = amount,
                swapType = swapType,
                slippage = DEFAULT_SLIPPAGE_TOLERANCE,
                previousState = newState,
                swapTypeAssetDecimal = assetDecimal,
                isMaxAndPercentageButtonEnabled = true,
                formattedPercentageText = emptyString()
            ) ?: return@flow
            emit(swapQuoteUpdatedPreview)
        }
    }
}
