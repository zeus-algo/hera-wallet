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

package network.voi.hera.models.decider

import network.voi.hera.models.BaseAppCallTransaction
import network.voi.hera.models.BaseAssetConfigurationTransaction
import network.voi.hera.models.BaseAssetTransferTransaction
import network.voi.hera.models.BasePaymentTransaction
import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.TransactionRequestAmountInfo
import network.voi.hera.models.TransactionRequestExtrasInfo
import network.voi.hera.models.TransactionRequestNoteInfo
import network.voi.hera.models.TransactionRequestSenderInfo
import network.voi.hera.models.TransactionRequestTransactionInfo
import network.voi.hera.models.builder.BaseAppCallTransactionDetailUiBuilder
import network.voi.hera.models.builder.BaseAssetConfigurationTransactionDetailUiBuilder
import network.voi.hera.models.builder.BaseAssetTransferTransactionDetailUiBuilder
import network.voi.hera.models.builder.BasePaymentTransactionDetailUiBuilder
import network.voi.hera.models.builder.WalletConnectTransactionDetailBuilder
import javax.inject.Inject

class WalletConnectTransactionDetailUiDecider @Inject constructor(
    private val basePaymentTransactionDetailUiBuilder: BasePaymentTransactionDetailUiBuilder,
    private val baseAssetTransferTransactionDetailUiBuilder: BaseAssetTransferTransactionDetailUiBuilder,
    private val baseAssetConfigurationTransactionDetailUiBuilder: BaseAssetConfigurationTransactionDetailUiBuilder,
    private val baseAppCallTransactionDetailUiBuilder: BaseAppCallTransactionDetailUiBuilder
) {

    fun buildTransactionRequestTransactionInfo(txn: BaseWalletConnectTransaction): TransactionRequestTransactionInfo? {
        return getTxnTypeUiBuilder(txn).buildTransactionRequestTransactionInfo(txn)
    }

    fun buildTransactionRequestSenderInfo(txn: BaseWalletConnectTransaction): TransactionRequestSenderInfo? {
        return getTxnTypeUiBuilder(txn).buildTransactionRequestSenderInfo(txn)
    }

    fun buildTransactionRequestNoteInfo(txn: BaseWalletConnectTransaction): TransactionRequestNoteInfo? {
        return getTxnTypeUiBuilder(txn).buildTransactionRequestNoteInfo(txn)
    }

    fun buildTransactionRequestExtrasInfo(txn: BaseWalletConnectTransaction): TransactionRequestExtrasInfo {
        return getTxnTypeUiBuilder(txn).buildTransactionRequestExtrasInfo(txn)
    }

    fun buildTransactionRequestAmountInfo(txn: BaseWalletConnectTransaction): TransactionRequestAmountInfo {
        return getTxnTypeUiBuilder(txn).buildTransactionRequestAmountInfo(txn)
    }

    private fun getTxnTypeUiBuilder(
        txn: BaseWalletConnectTransaction
    ): WalletConnectTransactionDetailBuilder<BaseWalletConnectTransaction> {
        return when (txn) {
            is BasePaymentTransaction -> basePaymentTransactionDetailUiBuilder
            is BaseAssetTransferTransaction -> baseAssetTransferTransactionDetailUiBuilder
            is BaseAssetConfigurationTransaction -> baseAssetConfigurationTransactionDetailUiBuilder
            is BaseAppCallTransaction -> baseAppCallTransactionDetailUiBuilder
            else -> throw Exception("Unknown wallet connect transaction type.")
        } as WalletConnectTransactionDetailBuilder<BaseWalletConnectTransaction>
    }
}
