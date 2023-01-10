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

import network.voi.hera.modules.currency.domain.usecase.DisplayedCurrencyUseCase
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapType
import network.voi.hera.modules.swap.assetswap.ui.mapper.SelectedAssetAmountDetailMapper
import network.voi.hera.modules.swap.assetswap.ui.model.AssetSwapPreview
import network.voi.hera.modules.swap.assetswap.ui.utils.SwapAmountUtils
import network.voi.hera.modules.swap.slippagetolerance.ui.util.DEFAULT_SLIPPAGE_TOLERANCE
import network.voi.hera.usecase.CheckUserHasAssetBalanceUseCase
import network.voi.hera.utils.emptyString
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class AssetSwapToAssetUpdatedUseCase @Inject constructor(
    private val assetSwapPreviewAssetDetailUseCase: AssetSwapPreviewAssetDetailUseCase,
    private val checkUserHasAssetBalanceUseCase: CheckUserHasAssetBalanceUseCase,
    private val assetSwapCreateQuotePreviewUseCase: AssetSwapCreateQuotePreviewUseCase,
    private val selectedAssetAmountDetailMapper: SelectedAssetAmountDetailMapper,
    private val displayedCurrencyUseCase: DisplayedCurrencyUseCase
) {

    suspend fun getToAssetUpdatedPreview(
        fromAssetId: Long,
        toAssetId: Long,
        amount: String?,
        fromAssetDecimal: Int,
        accountAddress: String,
        swapType: SwapType,
        previousState: AssetSwapPreview
    ) = flow<AssetSwapPreview> {
        emit(previousState.copy(isLoadingVisible = true))
        val toSelectedAssetDetail = assetSwapPreviewAssetDetailUseCase.createToSelectedAssetDetail(
            toAssetId = toAssetId,
            accountAddress = accountAddress,
            previousState = previousState
        )
        val amountAsBigDecimal = amount?.toBigDecimalOrNull()
        if (amountAsBigDecimal == null) {
            val toSelectedAssetAmountDetail = selectedAssetAmountDetailMapper.mapToDefaultSelectedAssetAmountDetail(
                primaryCurrencySymbol = displayedCurrencyUseCase.getDisplayedCurrencySymbol()
            )
            val newState = previousState.copy(
                toSelectedAssetDetail = toSelectedAssetDetail,
                isLoadingVisible = false,
                isSwitchAssetsButtonEnabled = checkUserHasAssetBalanceUseCase
                    .hasUserAssetBalance(accountAddress, toAssetId),
                isMaxAndPercentageButtonEnabled = true,
                toSelectedAssetAmountDetail = toSelectedAssetAmountDetail
            )
            emit(newState)
        } else {
            if (!SwapAmountUtils.isAmountValidForApiRequest(amount)) return@flow
            val newState = previousState.copy(toSelectedAssetDetail = toSelectedAssetDetail)
            val swapQuoteUpdatedPreview = assetSwapCreateQuotePreviewUseCase.getSwapQuoteUpdatedPreview(
                accountAddress = accountAddress,
                fromAssetId = fromAssetId,
                toAssetId = toAssetId,
                amount = amount,
                swapType = swapType,
                slippage = DEFAULT_SLIPPAGE_TOLERANCE,
                previousState = newState,
                swapTypeAssetDecimal = fromAssetDecimal,
                isMaxAndPercentageButtonEnabled = true,
                formattedPercentageText = emptyString()
            ) ?: return@flow
            emit(swapQuoteUpdatedPreview)
        }
    }
}
