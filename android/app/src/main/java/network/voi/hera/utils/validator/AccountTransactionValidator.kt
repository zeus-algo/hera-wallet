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

import network.voi.hera.R
import network.voi.hera.core.AccountManager
import network.voi.hera.models.AccountCacheData
import network.voi.hera.models.AccountInformation
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.Result
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetAccountMinimumBalanceUseCase
import network.voi.hera.utils.MIN_FEE
import network.voi.hera.utils.exceptions.WarningException
import network.voi.hera.utils.isEqualTo
import network.voi.hera.utils.isLesserThan
import network.voi.hera.utils.isValidAddress
import network.voi.hera.utils.minBalancePerAssetAsBigInteger
import java.math.BigInteger
import javax.inject.Inject

class AccountTransactionValidator @Inject constructor(
    private val accountManager: AccountManager,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val getAccountMinimumBalanceUseCase: GetAccountMinimumBalanceUseCase
) {

    fun isAccountAddressValid(toAccountPublicKey: String): Result<String> {
        if (toAccountPublicKey.isValidAddress()) {
            return Result.Success(toAccountPublicKey)
        }
        return Result.Error(WarningException(R.string.warning, AnnotatedString(R.string.key_not_valid)))
    }

    fun isSelectedAssetValid(fromAccountPublicKey: String, assetId: Long): Boolean {
        val accountDetail = accountDetailUseCase.getCachedAccountDetail(fromAccountPublicKey)?.data
        val isAlgo = assetId == ALGO_ID
        return accountDetail?.accountInformation?.assetHoldingList?.any { it.assetId == assetId } == true || isAlgo
    }

    fun isSelectedAssetSupported(accountInformation: AccountInformation, assetId: Long): Boolean {
        return accountInformation.isAssetSupported(assetId)
    }

    fun isThereAnyAccountWithToPublicKey(toAccountAddress: String): Boolean {
        return accountManager.isThereAnyAccountWithPublicKey(toAccountAddress)
    }

    fun isSendingAmountLesserThanMinimumBalance(
        toAccountSelectedAssetBalance: BigInteger,
        amount: BigInteger,
        minBalance: BigInteger
    ): Boolean {
        return (toAccountSelectedAssetBalance + amount) isLesserThan minBalance
    }

    fun isCloseTransactionToSameAccount(
        fromAccount: AccountCacheData?,
        toAccount: String,
        selectedAsset: AssetInformation?,
        amount: BigInteger
    ): Boolean {
        val isMax = amount == selectedAsset?.amount
        val hasOnlyAlgo = fromAccount?.accountInformation?.run {
            !isThereAnOptedInApp() || !isThereAnyDifferentAsset()
        } ?: false
        return fromAccount?.account?.address == toAccount && selectedAsset?.isAlgo() == true && isMax && hasOnlyAlgo
    }

    fun isSendingMaxAmountToTheSameAccount(
        fromAccount: String,
        toAccount: String,
        maxAmount: BigInteger,
        amount: BigInteger,
        isAlgo: Boolean
    ): Boolean {
        val maxSelectableAmount = if (isAlgo) {
            maxAmount - getAccountMinimumBalanceUseCase.getAccountMinimumBalance(toAccount) - MIN_FEE.toBigInteger()
        } else {
            maxAmount
        }
        val isMax = amount >= maxSelectableAmount
        val isTheSameAccount = fromAccount == toAccount
        return isMax && isTheSameAccount
    }

    fun isAccountNewlyOpenedAndBalanceInvalid(
        receiverAccountInformation: AccountInformation,
        amount: BigInteger,
        assetId: Long
    ): Boolean {
        return assetId == ALGO_ID &&
            receiverAccountInformation.amount isEqualTo BigInteger.ZERO &&
            amount < minBalancePerAssetAsBigInteger
    }
}
