/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package network.voi.hera.utils.validator

import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.AssetTransferAmountValidationResult
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetBaseOwnedAssetDataUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.MIN_FEE
import network.voi.hera.utils.formatAmountAsBigInteger
import network.voi.hera.utils.isGreaterThan
import network.voi.hera.utils.isLesserThan
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class AmountTransactionValidationUseCase @Inject constructor(
    private val getBaseOwnedAssetDataUseCase: GetBaseOwnedAssetDataUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val simpleCollectibleUseCase: SimpleCollectibleUseCase
) {

    fun validateAssetAmount(
        amountInBigDecimal: BigDecimal,
        senderAddress: String,
        assetId: Long
    ): AssetTransferAmountValidationResult {
        val isAmountBiggerThanBalance = isAmountBiggerThanBalance(
            address = senderAddress,
            assetId = assetId,
            amount = amountInBigDecimal
        )
        val isBalanceInsufficientForPayingFee = isBalanceInsufficientForPayingFee(senderAddress)
        val isMinimumBalanceViolated = isMinimumBalanceViolated(
            address = senderAddress,
            assetId = assetId,
            amount = amountInBigDecimal
        )
        val selectedAmount = getAmountAsBigInteger(amountInBigDecimal, assetId)
        return AssetTransferAmountValidationResult(
            isAmountMoreThanBalance = isAmountBiggerThanBalance,
            isBalanceInsufficientForPayingFee = isBalanceInsufficientForPayingFee,
            isMinimumBalanceViolated = isMinimumBalanceViolated,
            selectedAmount = selectedAmount
        )
    }

    fun getMaximumSendableAmount(address: String, assetId: Long): BigInteger? {
        val accountInformation =
            accountDetailUseCase.getCachedAccountDetail(address)?.data?.accountInformation ?: return null
        val ownedAssetData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, address) ?: return null
        return if (assetId == ALGO_ID) {
            ownedAssetData.amount - accountInformation.getMinAlgoBalance() - MIN_FEE.toBigInteger()
        } else {
            ownedAssetData.amount
        }
    }

    private fun isAmountBiggerThanBalance(address: String, assetId: Long, amount: BigDecimal): Boolean? {
        val ownedAssetData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, address) ?: return null
        val amountAsBigInteger = amount.formatAmountAsBigInteger(ownedAssetData.decimals)
        return amountAsBigInteger.isGreaterThan(ownedAssetData.amount)
    }

    fun isAmountBiggerThanBalance(address: String, assetId: Long, amount: BigInteger): Boolean? {
        val ownedAssetData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, address) ?: return null
        return amount.isGreaterThan(ownedAssetData.amount)
    }

    private fun isBalanceInsufficientForPayingFee(address: String): Boolean? {
        val accountInformation =
            accountDetailUseCase.getCachedAccountDetail(address)?.data?.accountInformation ?: return null
        return accountInformation.amount.isLesserThan(accountInformation.getMinAlgoBalance() + MIN_FEE.toBigInteger())
    }

    private fun isMinimumBalanceViolated(
        address: String,
        assetId: Long,
        amount: BigDecimal
    ): Boolean? {
        val accountInformation =
            accountDetailUseCase.getCachedAccountDetail(address)?.data?.accountInformation ?: return null
        val ownedAssetData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, address) ?: return null
        val isThereAnotherAsset = accountInformation.isThereAnyDifferentAsset()
        val isThereAppOptedIn = accountInformation.isThereAnOptedInApp()
        val minimumBalance = accountInformation.getMinAlgoBalance()
        val amountAsBigInteger = amount.formatAmountAsBigInteger(ownedAssetData.decimals)
        return ownedAssetData.isAlgo &&
            (ownedAssetData.amount - amountAsBigInteger - MIN_FEE.toBigInteger()) isLesserThan minimumBalance &&
            (isThereAnotherAsset || isThereAppOptedIn)
    }

    fun getAmountAsBigInteger(amount: BigDecimal, assetId: Long): BigInteger? {
        val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(assetId)
            ?: simpleCollectibleUseCase.getCachedCollectibleById(assetId)
            ?: return null
        return amount.formatAmountAsBigInteger(assetDetail.data?.fractionDecimals ?: return null)
    }
}
