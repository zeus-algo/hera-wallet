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

package network.voi.hera.modules.swap.confirmswap.ui.model

import android.content.res.Resources
import network.voi.hera.assetsearch.ui.model.VerificationTierConfiguration
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AnnotatedString
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.swap.confirmswap.domain.model.SwapQuoteTransaction
import network.voi.hera.modules.swap.ledger.signwithledger.ui.model.LedgerDialogPayload
import network.voi.hera.modules.swap.utils.priceratioprovider.SwapPriceRatioProvider
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.ErrorResource
import network.voi.hera.utils.Event
import network.voi.hera.utils.assetdrawable.BaseAssetDrawableProvider

data class ConfirmSwapPreview(
    val fromAssetDetail: SwapAssetDetail,
    val toAssetDetail: SwapAssetDetail,
    val slippageTolerance: String,
    val formattedPriceImpact: String,
    val minimumReceived: AnnotatedString,
    val formattedExchangeFee: String,
    val formattedPeraFee: String,
    val swapQuote: SwapQuote,
    val isLoading: Boolean,
    val isPriceImpactErrorVisible: Boolean,
    val accountIconResource: AccountIconResource,
    val accountDisplayName: AccountDisplayName,
    val errorEvent: Event<ErrorResource>? = null,
    val slippageToleranceUpdateSuccessEvent: Event<Unit>? = null,
    val navigateToTransactionStatusFragmentEvent: Event<List<SwapQuoteTransaction>>? = null,
    val navigateToLedgerWaitingForApprovalDialogEvent: Event<LedgerDialogPayload>? = null,
    val navigateToLedgerNotFoundDialogEvent: Event<Unit>? = null,
    val dismissLedgerWaitingForApprovalDialogEvent: Event<Unit>? = null,
    private val priceRatioProvider: SwapPriceRatioProvider
) {

    fun getPriceRatio(resources: Resources): AnnotatedString {
        return priceRatioProvider.getRatioState(resources)
    }

    fun getSwitchedPriceRatio(resources: Resources): AnnotatedString {
        return priceRatioProvider.getSwitchedRatioState(resources)
    }

    data class SwapAssetDetail(
        val formattedAmount: String,
        val formattedApproximateValue: String,
        val shortName: AssetName,
        val assetDrawableProvider: BaseAssetDrawableProvider,
        val verificationTierConfig: VerificationTierConfiguration
    )
}
