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

package network.voi.hera.modules.swap.assetswap.ui.mapper

import network.voi.hera.models.AccountIconResource
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.swap.assetswap.ui.model.AssetSwapPreview
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.ErrorResource
import network.voi.hera.utils.Event
import javax.inject.Inject

class AssetSwapPreviewMapper @Inject constructor() {

    @Suppress("LongParameterList")
    fun mapToAssetSwapPreview(
        accountDisplayName: AccountDisplayName,
        accountIconResource: AccountIconResource?,
        fromSelectedAssetDetail: AssetSwapPreview.SelectedAssetDetail,
        toSelectedAssetDetail: AssetSwapPreview.SelectedAssetDetail?,
        isSwapButtonEnabled: Boolean,
        isLoadingVisible: Boolean,
        fromSelectedAssetAmountDetail: AssetSwapPreview.SelectedAssetAmountDetail?,
        toSelectedAssetAmountDetail: AssetSwapPreview.SelectedAssetAmountDetail?,
        isSwitchAssetsButtonEnabled: Boolean,
        isMaxAndPercentageButtonEnabled: Boolean,
        formattedPercentageText: String,
        errorEvent: Event<ErrorResource>?,
        swapQuote: SwapQuote?,
        clearToSelectedAssetDetailEvent: Event<Unit>?,
        navigateToConfirmSwapFragmentEvent: Event<SwapQuote>?
    ): AssetSwapPreview {
        return AssetSwapPreview(
            accountDisplayName = accountDisplayName,
            accountIconResource = accountIconResource,
            fromSelectedAssetDetail = fromSelectedAssetDetail,
            toSelectedAssetDetail = toSelectedAssetDetail,
            isSwapButtonEnabled = isSwapButtonEnabled,
            isLoadingVisible = isLoadingVisible,
            fromSelectedAssetAmountDetail = fromSelectedAssetAmountDetail,
            toSelectedAssetAmountDetail = toSelectedAssetAmountDetail,
            isSwitchAssetsButtonEnabled = isSwitchAssetsButtonEnabled,
            isMaxAndPercentageButtonEnabled = isMaxAndPercentageButtonEnabled,
            formattedPercentageText = formattedPercentageText,
            errorEvent = errorEvent,
            swapQuote = swapQuote,
            clearToSelectedAssetDetailEvent = clearToSelectedAssetDetailEvent,
            navigateToConfirmSwapFragmentEvent = navigateToConfirmSwapFragmentEvent
        )
    }
}
