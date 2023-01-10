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

package network.voi.hera.mapper

import network.voi.hera.models.BaseAssetConfigurationTransaction.BaseAssetReconfigurationTransaction
import network.voi.hera.models.BaseAssetConfigurationTransaction.BaseAssetReconfigurationTransaction.Companion.isTransactionWithCloseTo
import network.voi.hera.models.BaseAssetConfigurationTransaction.BaseAssetReconfigurationTransaction.Companion.isTransactionWithCloseToAndRekeyed
import network.voi.hera.models.BaseAssetConfigurationTransaction.BaseAssetReconfigurationTransaction.Companion.isTransactionWithRekeyed
import network.voi.hera.models.WCAlgoTransactionRequest
import network.voi.hera.models.WalletConnectAccount
import network.voi.hera.models.WalletConnectAssetInformation
import network.voi.hera.models.WalletConnectPeerMeta
import network.voi.hera.models.WalletConnectSigner
import network.voi.hera.models.WalletConnectTransactionRequest
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.walletconnect.WalletConnectTransactionErrorProvider
import javax.inject.Inject

@SuppressWarnings("ReturnCount")
class BaseAssetReconfigurationTransactionMapper @Inject constructor(
    private val accountCacheManager: AccountCacheManager,
    private val errorProvider: WalletConnectTransactionErrorProvider
) : BaseWalletConnectTransactionMapper() {

    override fun createTransaction(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseAssetReconfigurationTransaction? {
        return when {
            isTransactionWithCloseToAndRekeyed(transactionRequest) -> {
                createAssetReconfigurationTransactionWithCloseToAndRekey(peerMeta, transactionRequest, rawTxn)
            }
            isTransactionWithCloseTo(transactionRequest) -> {
                createAssetReconfigurationTransactionWithClose(peerMeta, transactionRequest, rawTxn)
            }
            isTransactionWithRekeyed(transactionRequest) -> {
                createAssetReconfigurationTransactionWithRekey(peerMeta, transactionRequest, rawTxn)
            }
            else -> {
                createAssetReconfigurationTransaction(peerMeta, transactionRequest, rawTxn)
            }
        }
    }

    private fun createAssetReconfigurationTransaction(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseAssetReconfigurationTransaction.AssetReconfigurationTransaction? {
        return with(transactionRequest) {
            val senderWalletConnectAddress = createWalletConnectAddress(senderAddress)
            val accountCacheData = accountCacheManager.getCacheData(senderWalletConnectAddress?.decodedAddress)
            BaseAssetReconfigurationTransaction.AssetReconfigurationTransaction(
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                senderAddress = senderWalletConnectAddress ?: return null,
                note = decodedNote,
                peerMeta = peerMeta,
                rawTransactionPayload = rawTxn,
                signer = WalletConnectSigner.create(rawTxn, senderWalletConnectAddress, errorProvider),
                authAddress = accountCacheData?.authAddress,
                fromAccount = WalletConnectAccount.create(accountCacheData?.account),
                assetInformation = WalletConnectAssetInformation.create(
                    accountCacheData?.assetsInformation?.find { it.assetId == assetIdBeingConfigured }
                ),
                assetId = assetIdBeingConfigured ?: return null,
                url = assetConfigParams?.url,
                managerAddress = createWalletConnectAddress(assetConfigParams?.managerAddress),
                reserveAddress = createWalletConnectAddress(assetConfigParams?.reserveAddress),
                frozenAddress = createWalletConnectAddress(assetConfigParams?.frozenAddress),
                clawbackAddress = createWalletConnectAddress(assetConfigParams?.clawbackAddress),
                groupId = groupId
            )
        }
    }

    private fun createAssetReconfigurationTransactionWithClose(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithCloseTo? {
        return with(transactionRequest) {
            val senderWalletConnectAddress = createWalletConnectAddress(senderAddress)
            val accountCacheData = accountCacheManager.getCacheData(senderWalletConnectAddress?.decodedAddress)
            BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithCloseTo(
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                senderAddress = senderWalletConnectAddress ?: return null,
                note = decodedNote,
                peerMeta = peerMeta,
                rawTransactionPayload = rawTxn,
                signer = WalletConnectSigner.create(rawTxn, senderWalletConnectAddress, errorProvider),
                authAddress = accountCacheData?.authAddress,
                fromAccount = WalletConnectAccount.create(accountCacheData?.account),
                assetInformation = WalletConnectAssetInformation.create(
                    accountCacheData?.assetsInformation?.find { it.assetId == assetIdBeingConfigured }
                ),
                closeToAddress = createWalletConnectAddress(closeToAddress) ?: return null,
                assetId = assetIdBeingConfigured ?: return null,
                url = assetConfigParams?.url,
                managerAddress = createWalletConnectAddress(assetConfigParams?.managerAddress),
                reserveAddress = createWalletConnectAddress(assetConfigParams?.reserveAddress),
                frozenAddress = createWalletConnectAddress(assetConfigParams?.frozenAddress),
                clawbackAddress = createWalletConnectAddress(assetConfigParams?.clawbackAddress),
                groupId = groupId
            )
        }
    }

    private fun createAssetReconfigurationTransactionWithRekey(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithRekey? {
        return with(transactionRequest) {
            val senderWalletConnectAddress = createWalletConnectAddress(senderAddress)
            val accountCacheData = accountCacheManager.getCacheData(senderWalletConnectAddress?.decodedAddress)
            BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithRekey(
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                senderAddress = senderWalletConnectAddress ?: return null,
                note = decodedNote,
                peerMeta = peerMeta,
                rawTransactionPayload = rawTxn,
                signer = WalletConnectSigner.create(rawTxn, senderWalletConnectAddress, errorProvider),
                authAddress = accountCacheData?.authAddress,
                fromAccount = WalletConnectAccount.create(accountCacheData?.account),
                assetInformation = WalletConnectAssetInformation.create(
                    accountCacheData?.assetsInformation?.find { it.assetId == assetIdBeingConfigured }
                ),
                rekeyAddress = createWalletConnectAddress(rekeyAddress) ?: return null,
                assetId = assetIdBeingConfigured ?: return null,
                url = assetConfigParams?.url,
                managerAddress = createWalletConnectAddress(assetConfigParams?.managerAddress),
                reserveAddress = createWalletConnectAddress(assetConfigParams?.reserveAddress),
                frozenAddress = createWalletConnectAddress(assetConfigParams?.frozenAddress),
                clawbackAddress = createWalletConnectAddress(assetConfigParams?.clawbackAddress),
                groupId = groupId
            )
        }
    }

    private fun createAssetReconfigurationTransactionWithCloseToAndRekey(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithCloseToAndRekey? {
        return with(transactionRequest) {
            val senderWalletConnectAddress = createWalletConnectAddress(senderAddress)
            val accountCacheData = accountCacheManager.getCacheData(senderWalletConnectAddress?.decodedAddress)
            BaseAssetReconfigurationTransaction.AssetReconfigurationTransactionWithCloseToAndRekey(
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                senderAddress = senderWalletConnectAddress ?: return null,
                note = decodedNote,
                peerMeta = peerMeta,
                rawTransactionPayload = rawTxn,
                signer = WalletConnectSigner.create(rawTxn, senderWalletConnectAddress, errorProvider),
                authAddress = accountCacheData?.authAddress,
                fromAccount = WalletConnectAccount.create(accountCacheData?.account),
                assetInformation = WalletConnectAssetInformation.create(
                    accountCacheData?.assetsInformation?.find { it.assetId == assetIdBeingConfigured }
                ),
                closeToAddress = createWalletConnectAddress(closeToAddress) ?: return null,
                rekeyAddress = createWalletConnectAddress(rekeyAddress) ?: return null,
                assetId = assetIdBeingConfigured ?: return null,
                url = assetConfigParams?.url,
                managerAddress = createWalletConnectAddress(assetConfigParams?.managerAddress),
                reserveAddress = createWalletConnectAddress(assetConfigParams?.reserveAddress),
                frozenAddress = createWalletConnectAddress(assetConfigParams?.frozenAddress),
                clawbackAddress = createWalletConnectAddress(assetConfigParams?.clawbackAddress),
                groupId = groupId
            )
        }
    }
}
