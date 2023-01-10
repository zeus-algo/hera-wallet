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

package network.voi.hera.usecase

import network.voi.hera.models.Account
import network.voi.hera.models.SignedTransactionDetail
import network.voi.hera.models.SignedTransactionDetail.AssetOperation.AssetAddition
import network.voi.hera.models.SignedTransactionDetail.Send
import network.voi.hera.models.TrackTransactionRequest
import network.voi.hera.modules.transaction.confirmation.domain.usecase.TransactionConfirmationUseCase
import network.voi.hera.network.AlgodInterceptor
import network.voi.hera.Repository.TransactionsRepository
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.MAINNET_NETWORK_SLUG
import network.voi.hera.utils.analytics.logTransactionEvent
import network.voi.hera.utils.exception.AccountAlreadyOptedIntoAssetException
import network.voi.hera.utils.exceptions.TransactionConfirmationAwaitException
import network.voi.hera.utils.exceptions.TransactionIdNullException
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest

class SendSignedTransactionUseCase @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val algodInterceptor: AlgodInterceptor,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetAdditionUseCase: AssetAdditionUseCase,
    private val accountAssetRemovalUseCase: AccountAssetRemovalUseCase,
    private val transactionConfirmationUseCase: TransactionConfirmationUseCase
) {

    suspend fun sendSignedTransaction(
        signedTransactionDetail: SignedTransactionDetail,
        shouldLogTransaction: Boolean = true
    ) = channelFlow<DataResource<String>> {
        send(DataResource.Loading())
        if (signedTransactionDetail is AssetAddition && isAccountAlreadyOptedIntoAsset(signedTransactionDetail)) {
            send(DataResource.Error.Local(AccountAlreadyOptedIntoAssetException()))
        } else {
            transactionsRepository.sendSignedTransaction(signedTransactionDetail.signedTransactionData).use(
                onSuccess = { sendTransactionResponse ->
                    val txnId = sendTransactionResponse.taxId
                    if (signedTransactionDetail.shouldWaitForConfirmation) {
                        if (txnId.isNullOrBlank()) {
                            send(DataResource.Error.Local(TransactionIdNullException()))
                            return@use
                        }
                        transactionConfirmationUseCase.waitForConfirmation(txnId).collectLatest {
                            it.useSuspended(
                                onSuccess = {
                                    send(getSendTransactionResult(signedTransactionDetail, shouldLogTransaction, txnId))
                                },
                                onFailed = { error ->
                                    error.exception?.let { exception ->
                                        send(DataResource.Error.Api(exception, error.code))
                                    } ?: send(DataResource.Error.Api(TransactionConfirmationAwaitException(), null))
                                }
                            )
                        }
                    } else {
                        send(getSendTransactionResult(signedTransactionDetail, shouldLogTransaction, txnId))
                    }
                },
                onFailed = { exception, code ->
                    send(DataResource.Error.Api(exception, code))
                }
            )
        }
    }

    private suspend fun getSendTransactionResult(
        signedTransactionDetail: SignedTransactionDetail,
        shouldLogTransaction: Boolean,
        txnId: String?
    ): DataResource<String> {
        txnId?.let { transactionId ->
            transactionsRepository.postTrackTransaction(TrackTransactionRequest(transactionId))
            if (shouldLogTransaction && signedTransactionDetail is SignedTransactionDetail.Send) {
                logTransactionEvent(signedTransactionDetail, transactionId)
            }
        }
        cacheAssetIfAssetOperationTransaction(signedTransactionDetail)
        return DataResource.Success(txnId.orEmpty())
    }

    private fun logTransactionEvent(signedTransactionDetail: Send, taxId: String?) {
        if (algodInterceptor.currentActiveNode?.networkSlug == MAINNET_NETWORK_SLUG) {
            with(signedTransactionDetail) {
                firebaseAnalytics.logTransactionEvent(
                    amount = amount,
                    assetId = assetInformation.assetId,
                    accountType = accountCacheData.account.type ?: Account.Type.STANDARD,
                    isMax = isMax,
                    transactionId = taxId
                )
            }
        }
    }

    private fun isAccountAlreadyOptedIntoAsset(transaction: AssetAddition): Boolean {
        return accountDetailUseCase.isAssetOwnedByAccount(
            publicKey = transaction.accountCacheData.account.address,
            assetId = transaction.assetInformation.assetId
        )
    }

    private suspend fun cacheAssetIfAssetOperationTransaction(signedTransactionDetail: SignedTransactionDetail) {
        when (signedTransactionDetail) {
            is AssetAddition -> {
                assetAdditionUseCase.addAssetAdditionToAccountCache(
                    publicKey = signedTransactionDetail.accountCacheData.account.address,
                    assetInformation = signedTransactionDetail.assetInformation
                )
            }
            is SignedTransactionDetail.AssetOperation.AssetRemoval -> {
                accountAssetRemovalUseCase.addAssetDeletionToAccountCache(
                    publicKey = signedTransactionDetail.accountCacheData.account.address,
                    assetId = signedTransactionDetail.assetInformation.assetId
                )
            }
            else -> Unit
        }
    }
}
