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

package network.voi.hera.models.builder

import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.BasePaymentTransaction
import network.voi.hera.models.TransactionRequestAmountInfo
import network.voi.hera.models.TransactionRequestAssetInformation
import network.voi.hera.models.TransactionRequestExtrasInfo
import network.voi.hera.models.TransactionRequestNoteInfo
import network.voi.hera.models.TransactionRequestTransactionInfo
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.ALGO_FULL_NAME
import network.voi.hera.utils.ALGO_SHORT_NAME
import network.voi.hera.utils.MIN_FEE
import javax.inject.Inject

class BasePaymentTransactionDetailUiBuilder @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider
) : WalletConnectTransactionDetailBuilder<BasePaymentTransaction> {

    override fun buildTransactionRequestTransactionInfo(
        txn: BasePaymentTransaction
    ): TransactionRequestTransactionInfo {
        return with(txn) {
            TransactionRequestTransactionInfo(
                fromDisplayedAddress = getFromAddressAsDisplayAddress(senderAddress.decodedAddress.orEmpty()),
                fromAccountIcon = createAccountIconResource(),
                toDisplayedAddress = getToAddressAsDisplayAddress(receiverAddress.decodedAddress.orEmpty()),
                accountBalance = assetInformation?.amount,
                assetInformation = TransactionRequestAssetInformation(
                    assetId = ALGO_ID,
                    shortName = ALGO_SHORT_NAME,
                    fullName = ALGO_FULL_NAME,
                    decimals = assetDecimal,
                    verificationTierConfiguration =
                    verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier)
                ),
                rekeyToAccountAddress = getFromAddressAsDisplayAddress(
                    getRekeyToAccountAddress()?.decodedAddress.orEmpty()
                ),
                closeToAccountAddress = getFromAddressAsDisplayAddress(
                    getCloseToAccountAddress()?.decodedAddress.orEmpty()
                )
            )
        }
    }

    override fun buildTransactionRequestAmountInfo(txn: BasePaymentTransaction): TransactionRequestAmountInfo {
        return with(txn) {
            TransactionRequestAmountInfo(
                amount = transactionAmount,
                fee = fee,
                shouldShowFeeWarning = fee > MIN_FEE,
                assetDecimal = assetDecimal,
                assetShortName = ALGO_SHORT_NAME
            )
        }
    }

    override fun buildTransactionRequestNoteInfo(txn: BasePaymentTransaction): TransactionRequestNoteInfo? {
        with(txn) {
            if (note.isNullOrBlank()) return null
            return TransactionRequestNoteInfo(note = note)
        }
    }

    override fun buildTransactionRequestExtrasInfo(txn: BasePaymentTransaction): TransactionRequestExtrasInfo {
        return TransactionRequestExtrasInfo(rawTransaction = txn.rawTransactionPayload)
    }
}
