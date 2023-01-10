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

import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.modules.swap.assetswap.data.utils.getSafeAssetIdForRequest
import network.voi.hera.modules.swap.assetswap.domain.usecase.GetPeraFeeUseCase
import network.voi.hera.modules.swap.utils.swapFeePadding
import network.voi.hera.usecase.AccountAssetDataUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.exceptions.InsufficientAlgoBalance
import network.voi.hera.utils.isLesserThan
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetPercentageCalculatedBalanceForSwapUseCase @Inject constructor(
    private val accountAssetDataUseCase: AccountAssetDataUseCase,
    private val getPeraFeeUseCase: GetPeraFeeUseCase,
    private val accountDetailUseCase: AccountDetailUseCase
) {

    suspend fun getBalanceForSelectedPercentage(
        fromAssetId: Long,
        percentage: Float,
        accountAddress: String
    ): Flow<DataResource<BigDecimal>> {
        val isFromAssetAlgo = fromAssetId == ALGO_ID
        val (minRequiredBalance, accountAlgoBalance) = getMinBalanceAndAccountAlgoBalancePair(accountAddress)
        val percentageAsBigDecimal = percentage.toBigDecimal()
        return if (isFromAssetAlgo) {
            getBalancePercentageForAlgo(accountAlgoBalance, minRequiredBalance, percentageAsBigDecimal)
        } else {
            getBalancePercentageForAsset(
                accountAlgoBalance,
                minRequiredBalance,
                accountAddress,
                fromAssetId,
                percentageAsBigDecimal
            )
        }
    }

    private suspend fun getBalancePercentageForAlgo(
        accountAlgoBalance: BigInteger,
        minRequiredBalance: BigInteger,
        percentage: BigDecimal
    ): Flow<DataResource<BigDecimal>> = flow {

        val percentageCalculatedAlgoAmount = accountAlgoBalance
            .toBigDecimal()
            .movePointLeft(ALGO_DECIMALS)
            .multiply(percentage)
            .divide(percentageDivider)

        getPeraFeeUseCase.getPeraFee(ALGO_ID, percentageCalculatedAlgoAmount, ALGO_DECIMALS).useSuspended(
            onSuccess = { peraFee ->

                val requiredBalancesDeductedAccountBalance = accountAlgoBalance.toBigDecimal()
                    .movePointLeft(ALGO_DECIMALS)
                    .minus(minRequiredBalance.toBigDecimal().movePointLeft(ALGO_DECIMALS))
                    .minus(swapFeePadding)
                    .minus(peraFee)

                when {
                    requiredBalancesDeductedAccountBalance isLesserThan BigDecimal.ZERO -> {
                        emit(DataResource.Error.Local<BigDecimal>(InsufficientAlgoBalance()))
                    }
                    requiredBalancesDeductedAccountBalance isLesserThan percentageCalculatedAlgoAmount -> {
                        emit(DataResource.Success(requiredBalancesDeductedAccountBalance))
                    }
                    else -> {
                        emit(DataResource.Success(percentageCalculatedAlgoAmount))
                    }
                }
            },
            onFailed = { emit(it) }
        )
    }

    private suspend fun getBalancePercentageForAsset(
        accountAlgoBalance: BigInteger,
        minRequiredBalance: BigInteger,
        accountAddress: String,
        assetId: Long,
        percentage: BigDecimal
    ): Flow<DataResource<BigDecimal>> = flow {

        val calculatedAlgoBalance = accountAlgoBalance
            .minus(minRequiredBalance)
            .minus(swapFeePadding.toBigInteger())
            .toBigDecimal()
            .movePointLeft(ALGO_DECIMALS)

        if (calculatedAlgoBalance isLesserThan BigDecimal.ZERO) {
            emit(DataResource.Error.Local<BigDecimal>(InsufficientAlgoBalance()))
        } else {
            val accountAssetData = accountAssetDataUseCase.getAccountOwnedAssetData(accountAddress, includeAlgo = false)
                .first { it.id == assetId }

            val assetDecimal = accountAssetData.decimals

            val percentageCalculatedBalance = accountAssetData.amount
                .toBigDecimal()
                .movePointLeft(accountAssetData.decimals)
                .multiply(percentage)
                .divide(percentageDivider, assetDecimal, RoundingMode.DOWN)

            getPeraFeeUseCase.getPeraFee(assetId, percentageCalculatedBalance, assetDecimal).useSuspended(
                onSuccess = {
                    val feeDeductedAmount = calculatedAlgoBalance.minus(it)
                    if (feeDeductedAmount isLesserThan BigDecimal.ZERO) {
                        emit(DataResource.Error.Local<BigDecimal>(InsufficientAlgoBalance()))
                    } else {
                        emit(DataResource.Success(percentageCalculatedBalance))
                    }
                },
                onFailed = { emit(it) }
            )
        }
    }

    private suspend fun getPeraFeeDeductedAmount(amount: BigDecimal): DataResource<BigDecimal> {
        var result: DataResource<BigDecimal>? = null
        val safeAssetId = getSafeAssetIdForRequest(ALGO_ID)
        getPeraFeeUseCase.getPeraFee(safeAssetId, amount, ALGO_DECIMALS).useSuspended(
            onSuccess = {
                val feeDeductedAmount = amount.minus(it)
                result = if (feeDeductedAmount isLesserThan BigDecimal.ZERO) {
                    DataResource.Error.Local<BigDecimal>(InsufficientAlgoBalance())
                } else {
                    DataResource.Success(feeDeductedAmount)
                }
            },
            onFailed = { result = it }
        )
        return result ?: DataResource.Error.Local(NullPointerException())
    }

    private fun getMinBalanceAndAccountAlgoBalancePair(accountAddress: String): Pair<BigInteger, BigInteger> {
        val cachedAccountData = accountDetailUseCase.getCachedAccountDetail(accountAddress)?.data?.accountInformation
        val minRequiredAlgoBalance = cachedAccountData?.getMinAlgoBalance() ?: BigInteger.ZERO
        val accountAlgoBalance = cachedAccountData?.getBalance(ALGO_ID) ?: BigInteger.ZERO
        return minRequiredAlgoBalance to accountAlgoBalance
    }

    companion object {
        private val percentageDivider = BigDecimal.valueOf(100L)
    }
}
