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

package network.voi.hera.modules.swap.confirmswap.data.mapper

import network.voi.hera.modules.swap.confirmswap.data.mapper.decider.SwapTransactionPurposeDecider
import network.voi.hera.modules.swap.confirmswap.data.model.SwapQuoteTransactionResponse
import network.voi.hera.modules.swap.confirmswap.domain.model.SwapQuoteTransactionDTO
import javax.inject.Inject

class SwapQuoteTransactionDTOMapper @Inject constructor(
    private val swapTransactionPurposeDecider: SwapTransactionPurposeDecider
) {

    fun mapToSwapQuoteTransactionDTO(response: SwapQuoteTransactionResponse): SwapQuoteTransactionDTO {
        return SwapQuoteTransactionDTO(
            purpose = swapTransactionPurposeDecider.decideSwapTransactionPurpose(response.purpose),
            transactionGroupId = response.transactionGroupId,
            transactions = response.transactions,
            signedTransactions = response.signedTransactions
        )
    }
}
