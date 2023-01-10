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

package network.voi.hera.modules.algosdk.data.repository

import com.algorand.algosdk.mobile.Mobile
import com.algorand.algosdk.v2.client.Utils
import com.algorand.algosdk.v2.client.common.AlgodClient
import network.voi.hera.modules.algosdk.data.mapper.AlgorandAddressDTOMapper
import network.voi.hera.modules.algosdk.data.mapper.PendingTransactionResponseDTOMapper
import network.voi.hera.modules.algosdk.data.mapper.rawtransaction.RawTransactionDTOMapper
import network.voi.hera.modules.algosdk.data.model.AlgorandAddressDTO
import network.voi.hera.modules.algosdk.data.model.PendingTransactionResponseDTO
import network.voi.hera.modules.algosdk.data.model.rawtransaction.RawTransactionPayload
import network.voi.hera.modules.algosdk.data.service.AlgorandSDKUtils
import network.voi.hera.modules.algosdk.domain.model.dto.RawTransactionDTO
import network.voi.hera.utils.decodeBase64
import network.voi.hera.utils.fromJson
import com.google.gson.Gson
import javax.inject.Inject

// Provided by hilt
internal class AlgorandSDKUtilsImpl @Inject constructor(
    private val pendingTransactionResponseDTOMapper: PendingTransactionResponseDTOMapper,
    private val rawTransactionDTOMapper: RawTransactionDTOMapper,
    private val algorandAddressDTOMapper: AlgorandAddressDTOMapper,
    private val algodClient: AlgodClient?,
    private val gson: Gson
) : AlgorandSDKUtils {

    @Throws(Exception::class)
    override suspend fun waitForConfirmation(txnId: String, maxRoundToWait: Int): PendingTransactionResponseDTO {
        val pendingTransactionResponse = Utils.waitForConfirmation(algodClient, txnId, maxRoundToWait)
        return pendingTransactionResponseDTOMapper.mapToPendingTransactionResponseDTO(
            pendingTransactionResponse
        )
    }

    override fun parseRawTransaction(txnByteArray: ByteArray): RawTransactionDTO? {
        val transactionJson = Mobile.transactionMsgpackToJson(txnByteArray)
        val rawTransactionPayload = gson.fromJson<RawTransactionPayload>(transactionJson) ?: return null
        return rawTransactionDTOMapper.mapToRawTransactionDTO(rawTransactionPayload)
    }

    override fun generateAccountAddressFromPublicKey(addressBase64: String): AlgorandAddressDTO? {
        val publicKey = addressBase64.decodeBase64() ?: return null
        val accountAddress = Mobile.generateAddressFromPublicKey(publicKey)
        return algorandAddressDTOMapper.mapToAlgorandAddressDTO(publicKey, accountAddress)
    }
}
