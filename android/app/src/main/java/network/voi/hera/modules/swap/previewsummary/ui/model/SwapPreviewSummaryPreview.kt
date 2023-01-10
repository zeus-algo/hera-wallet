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

package network.voi.hera.modules.swap.previewsummary.ui.model

import android.content.res.Resources
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AnnotatedString
import network.voi.hera.modules.swap.utils.priceratioprovider.SwapPriceRatioProvider
import network.voi.hera.utils.AccountDisplayName

data class SwapPreviewSummaryPreview(
    val slippageTolerance: String,
    val priceImpact: String,
    val minimumReceived: AnnotatedString,
    val formattedExchangeFee: String,
    val formattedPeraFee: String,
    val formattedTotalFee: String,
    val accountDisplayName: AccountDisplayName,
    val accountIconResource: AccountIconResource,
    private val priceRatioProvider: SwapPriceRatioProvider
) {
    fun getPriceRatio(resources: Resources): AnnotatedString {
        return priceRatioProvider.getRatioState(resources)
    }

    fun getSwitchedPriceRatio(resources: Resources): AnnotatedString {
        return priceRatioProvider.getSwitchedRatioState(resources)
    }
}
