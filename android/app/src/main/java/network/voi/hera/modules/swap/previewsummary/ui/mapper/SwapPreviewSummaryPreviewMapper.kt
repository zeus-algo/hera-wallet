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

package network.voi.hera.modules.swap.previewsummary.ui.mapper

import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AnnotatedString
import network.voi.hera.modules.swap.previewsummary.ui.model.SwapPreviewSummaryPreview
import network.voi.hera.modules.swap.utils.priceratioprovider.SwapPriceRatioProvider
import network.voi.hera.utils.AccountDisplayName
import javax.inject.Inject

class SwapPreviewSummaryPreviewMapper @Inject constructor() {

    fun mapToSwapPreviewSummaryPreview(
        slippageTolerance: String,
        priceImpact: String,
        minimumReceived: AnnotatedString,
        formattedExchangeFee: String,
        formattedPeraFee: String,
        formattedTotalFee: String,
        accountDisplayName: AccountDisplayName,
        accountIconResource: AccountIconResource,
        priceRatioProvider: SwapPriceRatioProvider
    ): SwapPreviewSummaryPreview {
        return SwapPreviewSummaryPreview(
            slippageTolerance = slippageTolerance,
            priceImpact = priceImpact,
            minimumReceived = minimumReceived,
            formattedExchangeFee = formattedExchangeFee,
            formattedPeraFee = formattedPeraFee,
            formattedTotalFee = formattedTotalFee,
            accountDisplayName = accountDisplayName,
            accountIconResource = accountIconResource,
            priceRatioProvider = priceRatioProvider
        )
    }
}
