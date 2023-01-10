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

package network.voi.hera.ui.wcrawtransaction

import javax.inject.Inject
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.DecodedWalletConnectTransactionRequest
import network.voi.hera.models.WCAlgoTransactionRequest
import network.voi.hera.models.WalletConnectRawTransaction
import network.voi.hera.models.WalletConnectTransactionAssetDetail
import network.voi.hera.models.WalletConnectTransactionRequest
import network.voi.hera.utils.decodeBase64DecodedMsgPackToJsonString
import network.voi.hera.utils.getBase64DecodedPublicKey
import network.voi.hera.utils.getFormattedJsonArrayString
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class WalletConnectRawMessageViewModel @Inject constructor(
    private val gson: Gson
) : BaseViewModel() {

    fun getFormattedTransactionJson(txnRequest: WCAlgoTransactionRequest): String {
        val transaction = gson.fromJson(
            decodeBase64DecodedMsgPackToJsonString(txnRequest.transactionMsgPack),
            WalletConnectTransactionRequest::class.java
        )
        val decodedTransaction = DecodedWalletConnectTransactionRequest.create(transaction)
        val decodedSignerList = txnRequest.signers?.mapNotNull { getBase64DecodedPublicKey(it) }
        val rawTxn = WalletConnectRawTransaction(decodedTransaction, decodedSignerList)
        return getFormattedJsonArrayString(gson.toJson(rawTxn))
    }

    fun formatAssetMetadata(walletConnectTransactionAssetDetail: WalletConnectTransactionAssetDetail?): String {
        val jsonObject = gson.toJson(walletConnectTransactionAssetDetail)
        return getFormattedJsonArrayString(jsonObject)
    }
}
