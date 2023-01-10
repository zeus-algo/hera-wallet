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
import network.voi.hera.models.WalletConnectTransactionAmount
import network.voi.hera.models.WalletConnectTransactionShortDetail
import network.voi.hera.models.builder.BaseAppCallSingleTransactionUiBuilder
import network.voi.hera.models.builder.BaseAssetConfigurationSingleTransactionUiBuilder
import network.voi.hera.models.builder.BaseAssetTransferSingleTransactionUiBuilder
import network.voi.hera.models.builder.BasePaymentSingleTransactionUiBuilder
import network.voi.hera.models.builder.WalletConnectSingleTransactionUiBuilder
import javax.inject.Inject

class WalletConnectSingleTransactionUiDecider @Inject constructor(
    private val basePaymentSingleTransactionUiBuilder: BasePaymentSingleTransactionUiBuilder,
    private val baseAssetTransferSingleTransactionUiBuilder: BaseAssetTransferSingleTransactionUiBuilder,
    private val baseAssetConfigurationSingleTransactionUiBuilder: BaseAssetConfigurationSingleTransactionUiBuilder,
    private val baseAppCallSingleTransactionUiBuilder: BaseAppCallSingleTransactionUiBuilder
) {

    fun buildToolbarTitleRes(txn: BaseWalletConnectTransaction): Int {
        return getTxnTypeUiBuilder(txn).buildToolbarTitleRes(txn)
    }

    fun buildTransactionAmount(txn: BaseWalletConnectTransaction): WalletConnectTransactionAmount {
        return getTxnTypeUiBuilder(txn).buildTransactionAmount(txn)
    }

    fun buildTransactionShortDetail(txn: BaseWalletConnectTransaction): WalletConnectTransactionShortDetail {
        return getTxnTypeUiBuilder(txn).buildTransactionShortDetail(txn)
    }

    private fun getTxnTypeUiBuilder(
        txn: BaseWalletConnectTransaction
    ): WalletConnectSingleTransactionUiBuilder<BaseWalletConnectTransaction> {
        return when (txn) {
            is BasePaymentTransaction -> basePaymentSingleTransactionUiBuilder
            is BaseAssetTransferTransaction -> baseAssetTransferSingleTransactionUiBuilder
            is BaseAssetConfigurationTransaction -> baseAssetConfigurationSingleTransactionUiBuilder
            is BaseAppCallTransaction -> baseAppCallSingleTransactionUiBuilder
            else -> throw Exception("Unknown wallet connect transaction type.")
        } as WalletConnectSingleTransactionUiBuilder<BaseWalletConnectTransaction>
    }
}
