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

package com.algorand.android.models.builder

import network.voi.hera.R
import com.algorand.android.models.BaseAssetTransferTransaction
import com.algorand.android.models.WalletConnectTransactionAmount
import com.algorand.android.models.WalletConnectTransactionShortDetail
import com.algorand.android.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import javax.inject.Inject

class BaseAssetTransferSingleTransactionUiBuilder @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider
) : WalletConnectSingleTransactionUiBuilder<BaseAssetTransferTransaction> {

    override fun buildToolbarTitleRes(txn: BaseAssetTransferTransaction): Int {
        return when (txn) {
            is BaseAssetTransferTransaction.AssetOptInTransaction -> R.string.possible_opt_in_request
            else -> R.string.transaction_request
        }
    }

    override fun buildTransactionShortDetail(txn: BaseAssetTransferTransaction): WalletConnectTransactionShortDetail {
        return with(txn) {
            WalletConnectTransactionShortDetail(
                accountIconResource = createAccountIconResource(),
                accountName = fromAccount?.name,
                accountBalance = assetBalance,
                warningCount = warningCount,
                assetShortName = walletConnectTransactionAssetDetail?.shortName,
                decimal = assetDecimal,
                fee = fee
            )
        }
    }

    override fun buildTransactionAmount(txn: BaseAssetTransferTransaction): WalletConnectTransactionAmount {
        return with(txn) {
            WalletConnectTransactionAmount(
                assetName = walletConnectTransactionAssetDetail?.fullName,
                assetId = assetId,
                transactionAmount = transactionAmount,
                assetDecimal = assetDecimal,
                assetShortName = walletConnectTransactionAssetDetail?.shortName,
                formattedSelectedCurrencyValue = assetInformation?.formattedSelectedCurrencyValue,
                verificationTierConfiguration =
                verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier),
                fromDisplayedAddress = getFromAddressAsDisplayAddress(assetReceiverAddress.decodedAddress.orEmpty())
            )
        }
    }
}
