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
import network.voi.hera.models.WalletConnectTransactionSummary
import network.voi.hera.models.builder.BaseAppCallTransactionSummaryUiBuilder
import network.voi.hera.models.builder.BaseAssetConfigurationTransactionSummaryUiBuilder
import network.voi.hera.models.builder.BaseAssetTransferTransactionSummaryUiBuilder
import network.voi.hera.models.builder.BasePaymentTransactionSummaryUiBuilder
import network.voi.hera.models.builder.WalletConnectTransactionSummaryUIBuilder
import javax.inject.Inject

class WalletConnectTransactionSummaryUiDecider @Inject constructor(
    private val basePaymentTransactionSummaryUiBuilder: BasePaymentTransactionSummaryUiBuilder,
    private val baseAssetTransferTransactionSummaryUiBuilder: BaseAssetTransferTransactionSummaryUiBuilder,
    private val baseAssetConfigurationTransactionSummaryUiBuilder: BaseAssetConfigurationTransactionSummaryUiBuilder,
    private val baseAppCallTransactionSummaryUiBuilder: BaseAppCallTransactionSummaryUiBuilder
) {

    fun buildTransactionSummary(txn: BaseWalletConnectTransaction): WalletConnectTransactionSummary {
        return getTxnTypeUiBuilder(txn).buildTransactionSummary(txn)
    }

    private fun getTxnTypeUiBuilder(
        txn: BaseWalletConnectTransaction
    ): WalletConnectTransactionSummaryUIBuilder<BaseWalletConnectTransaction> {
        return when (txn) {
            is BasePaymentTransaction -> basePaymentTransactionSummaryUiBuilder
            is BaseAssetTransferTransaction -> baseAssetTransferTransactionSummaryUiBuilder
            is BaseAssetConfigurationTransaction -> baseAssetConfigurationTransactionSummaryUiBuilder
            is BaseAppCallTransaction -> baseAppCallTransactionSummaryUiBuilder
            else -> throw Exception("Unknown wallet connect transaction type.")
        } as WalletConnectTransactionSummaryUIBuilder<BaseWalletConnectTransaction>
    }
}
