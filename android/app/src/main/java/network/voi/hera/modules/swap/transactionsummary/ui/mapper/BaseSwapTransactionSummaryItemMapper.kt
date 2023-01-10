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

package network.voi.hera.modules.swap.transactionsummary.ui.mapper

import network.voi.hera.models.AccountIconResource
import network.voi.hera.modules.swap.transactionsummary.ui.model.BaseSwapTransactionSummaryItem
import network.voi.hera.utils.AccountDisplayName
import javax.inject.Inject

class BaseSwapTransactionSummaryItemMapper @Inject constructor() {

    fun mapToSwapAmountsItem(
        formattedReceivedAmount: String,
        formattedPaidAmount: String
    ): BaseSwapTransactionSummaryItem.SwapAmountsItemTransaction {
        return BaseSwapTransactionSummaryItem.SwapAmountsItemTransaction(
            formattedReceivedAmount = formattedReceivedAmount,
            formattedPaidAmount = formattedPaidAmount
        )
    }

    fun mapToSwapAccountItem(
        accountDisplayName: AccountDisplayName,
        accountIconResource: AccountIconResource
    ): BaseSwapTransactionSummaryItem.SwapAccountItemTransaction {
        return BaseSwapTransactionSummaryItem.SwapAccountItemTransaction(
            accountDisplayName = accountDisplayName,
            accountIconResource = accountIconResource
        )
    }

    fun mapToSwapFeesItem(
        formattedAlgorandFees: String,
        formattedOptInFees: String,
        isOptInFeesVisible: Boolean,
        formattedExchangeFees: String,
        isExchangeFeesVisible: Boolean,
        formattedPeraFees: String,
        isPeraFeeVisible: Boolean
    ): BaseSwapTransactionSummaryItem.SwapFeesItemTransaction {
        return BaseSwapTransactionSummaryItem.SwapFeesItemTransaction(
            formattedAlgorandFees = formattedAlgorandFees,
            formattedOptInFees = formattedOptInFees,
            isOptInFeesVisible = isOptInFeesVisible,
            formattedExchangeFees = formattedExchangeFees,
            isExchangeFeesVisible = isExchangeFeesVisible,
            formattedPeraFees = formattedPeraFees,
            isPeraFeeVisible = isPeraFeeVisible
        )
    }

    fun mapToSwapPriceImpactItem(
        priceImpact: String
    ): BaseSwapTransactionSummaryItem.SwapPriceImpactItemTransaction {
        return BaseSwapTransactionSummaryItem.SwapPriceImpactItemTransaction(
            priceImpact = priceImpact
        )
    }
}
