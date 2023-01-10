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

package network.voi.hera.modules.swap.assetswap.ui.utils

import network.voi.hera.R
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.swap.utils.swapFeePadding
import network.voi.hera.usecase.AccountAssetDataUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.ErrorResource
import network.voi.hera.utils.ErrorResource.LocalErrorResource.Local
import network.voi.hera.utils.Event
import network.voi.hera.utils.isEqualTo
import network.voi.hera.utils.isLesserThan
import java.math.BigDecimal
import javax.inject.Inject

class SwapBalanceErrorProvider @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val accountAssetDataUseCase: AccountAssetDataUseCase
) {

    fun checkIfSwapHasError(swapQuote: SwapQuote, accountAddress: String): Event<ErrorResource>? {
        return when {
            !hasAccountEnoughBalanceToCompleteSwap(swapQuote, accountAddress) -> {
                val errorResource = if (swapQuote.isFromAssetAlgo) {
                    Local(R.string.account_does_not_have)
                } else {
                    getSafeAsaInsufficientBalanceErrorResource(swapQuote)
                }
                Event(errorResource)
            }
            !hasAccountEnoughBalanceToPayFees(swapQuote, accountAddress) -> {
                Event(Local(R.string.account_does_not_have_algo))
            }
            else -> null
        }
    }

    private fun hasAccountEnoughBalanceToCompleteSwap(swapQuote: SwapQuote, accountAddress: String): Boolean {
        val fromAssetAmount = swapQuote.fromAssetAmount.movePointLeft(swapQuote.fromAssetDetail.fractionDecimals)
        val userBalance = getUserBalance(swapQuote.fromAssetDetail.assetId, accountAddress)
        return fromAssetAmount isLesserThan userBalance || fromAssetAmount isEqualTo userBalance
    }

    private fun hasAccountEnoughBalanceToPayFees(swapQuote: SwapQuote, accountAddress: String): Boolean {
        with(swapQuote) {
            val userAlgoBalance = getUserBalance(ALGO_ID, accountAddress)
            val accountInfo = accountDetailUseCase.getCachedAccountDetail(accountAddress)?.data
            val minBalanceUserNeedsToKeep = accountInfo?.accountInformation?.getMinAlgoBalance()
                ?.toBigDecimal()?.movePointLeft(ALGO_DECIMALS) ?: BigDecimal.ZERO
            val requiredBalance = if (isFromAssetAlgo) {
                fromAssetAmount.movePointLeft(ALGO_DECIMALS).add(minBalanceUserNeedsToKeep)
            } else {
                minBalanceUserNeedsToKeep
            }.add(peraFeeAmount).add(swapFeePadding)

            return requiredBalance isLesserThan userAlgoBalance || requiredBalance isEqualTo userAlgoBalance
        }
    }

    private fun getUserBalance(assetId: Long, accountAddress: String): BigDecimal {
        val ownedAssetData = accountAssetDataUseCase.getAccountOwnedAssetData(accountAddress, includeAlgo = true)
            .firstOrNull { it.id == assetId }
        return ownedAssetData?.amount?.toBigDecimal()?.movePointLeft(ownedAssetData.decimals) ?: BigDecimal.ZERO
    }

    private fun getSafeAsaInsufficientBalanceErrorResource(swapQuote: SwapQuote): ErrorResource {
        val asaShortName = swapQuote.fromAssetDetail.shortName.getName()
        return if (asaShortName.isNullOrBlank()) {
            Local(R.string.asa_balance_is_not_sufficient)
        } else {
            val asaShortNamePair = "asa_short_name" to asaShortName.orEmpty()
            ErrorResource.LocalErrorResource.Defined(
                AnnotatedString(
                    stringResId = R.string.asa_balance_is_not_sufficient_formatted,
                    replacementList = listOf(asaShortNamePair)
                )
            )
        }
    }
}
