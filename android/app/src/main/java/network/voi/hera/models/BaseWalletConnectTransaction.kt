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

package network.voi.hera.models

import android.os.Parcelable
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.decodeBase64
import network.voi.hera.utils.isRekeyedToAnotherAccount
import network.voi.hera.utils.isValidAddress
import java.math.BigInteger

abstract class BaseWalletConnectTransaction : Parcelable {

    abstract val walletConnectTransactionParams: WalletConnectTransactionParams
    abstract val senderAddress: WalletConnectAddress
    abstract val note: String?
    abstract val peerMeta: WalletConnectPeerMeta
    abstract val rawTransactionPayload: WCAlgoTransactionRequest
    abstract val signer: WalletConnectSigner
    abstract val groupId: String?
    abstract val fee: Long

    var requestedBlockCurrentRound: Long = -1

    open val warningCount: Int? = null

    open val authAddress: String? = null
    open val fromAccount: WalletConnectAccount? = null
    open val toAccount: WalletConnectAccount? = null
    open val assetInformation: WalletConnectAssetInformation? = null

    fun isRekeyedToAnotherAccount(): Boolean {
        return isRekeyedToAnotherAccount(authAddress, fromAccount?.address)
    }

    fun createAccountIconResource(): AccountIconResource? {
        return fromAccount?.accountIconResource
    }

    fun getAccountImageResource(): Int {
        return fromAccount?.accountIconResource?.iconResId
            ?: AccountIconResource.getAccountIconResourceByAccountType(Account.defaultAccountType).iconResId
    }

    open val assetDecimal: Int = DEFAULT_ASSET_DECIMAL

    open val transactionAmount: BigInteger? = null

    val transactionMessage: String?
        get() = rawTransactionPayload.message

    val formattedRekeyToAccountAddress: String
        get() = getRekeyToAccountAddress()?.decodedAddress.orEmpty()

    val formattedCloseToAccountAddress: String
        get() = getCloseToAccountAddress()?.decodedAddress.orEmpty()

    val decodedTransaction: ByteArray?
        get() = rawTransactionPayload.transactionMsgPack.decodeBase64()

    protected val signerAddressList: List<WalletConnectAddress>?
        get() = rawTransactionPayload.signers?.map { addressBase64 ->
            WalletConnectAddress.create(addressBase64)
        }

    fun isAuthAddressValid(): Boolean {
        val authAddress = rawTransactionPayload.authAccountAddress
        return authAddress == null || authAddress.isValidAddress()
    }

    abstract fun getAllAddressPublicKeysTxnIncludes(): List<WalletConnectAddress>

    open fun getRekeyToAccountAddress(): WalletConnectAddress? = null
    open fun getCloseToAccountAddress(): WalletConnectAddress? = null

    fun getFromAddressAsDisplayAddress(address: String): BaseWalletConnectDisplayedAddress {
        return BaseWalletConnectDisplayedAddress.create(address, fromAccount)
    }

    fun getToAddressAsDisplayAddress(address: String): BaseWalletConnectDisplayedAddress {
        return BaseWalletConnectDisplayedAddress.create(address, toAccount)
    }
}
