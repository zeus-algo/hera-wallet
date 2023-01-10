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

package network.voi.hera.mapper

import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.WCAlgoTransactionRequest
import network.voi.hera.models.WalletConnectAddress
import network.voi.hera.models.WalletConnectPeerMeta
import network.voi.hera.models.WalletConnectTransactionParams
import network.voi.hera.models.WalletConnectTransactionRequest

@SuppressWarnings("ReturnCount")
abstract class BaseWalletConnectTransactionMapper {

    abstract fun createTransaction(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseWalletConnectTransaction?

    protected fun createTransactionParams(
        transactionRequest: WalletConnectTransactionRequest
    ): WalletConnectTransactionParams {
        return with(transactionRequest) {
            WalletConnectTransactionParams(
                fee = fee ?: 0L,
                firstValidRound = firstValidRound,
                lastValidRound = lastValidRound,
                genesisId = genesisId,
                genesisHash = genesisHash
            )
        }
    }

    protected fun createWalletConnectAddress(addressBase64: String?): WalletConnectAddress? {
        if (addressBase64 == null) return null
        return WalletConnectAddress.create(addressBase64)
    }
}
