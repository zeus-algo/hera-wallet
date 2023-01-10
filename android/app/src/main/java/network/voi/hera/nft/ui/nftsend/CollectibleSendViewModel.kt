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

package network.voi.hera.nft.ui.nftsend

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.SignedTransactionDetail
import network.voi.hera.models.TargetUser
import network.voi.hera.models.TransactionData
import network.voi.hera.nft.domain.usecase.CollectibleSendPreviewUseCase
import network.voi.hera.nft.ui.model.CollectibleSendPreview
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigInteger
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CollectibleSendViewModel @Inject constructor(
    private val collectibleSendPreviewUseCase: CollectibleSendPreviewUseCase,
    private val accountCacheManager: AccountCacheManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val accountAddress: String = savedStateHandle.getOrThrow(ACCOUNT_ADDRESS_KEY)
    val nftId: Long = savedStateHandle.getOrThrow(NFT_ID_KEY)

    private val _selectedAccountAddressFlow = MutableStateFlow<String>("")
    val selectedAccountAddressFlow: StateFlow<String> get() = _selectedAccountAddressFlow

    private val _collectibleSendPreviewFlow = MutableStateFlow<CollectibleSendPreview?>(null)
    val collectibleSendPreview: StateFlow<CollectibleSendPreview?> get() = _collectibleSendPreviewFlow

    private var cachedPreviouslySignedTransaction: SignedTransactionDetail.Send? = null

    var nftDomainAddressServiceLogoPair: Pair<String, String?>? = null
        private set

    fun updateSelectedAccountAddress(address: String) {
        _selectedAccountAddressFlow.value = address
    }

    init {
        initCollectibleSendPreview()
    }

    fun getSelectedAddress(): String = _selectedAccountAddressFlow.value

    fun checkIfSenderAndReceiverAccountSame() {
        viewModelScope.launch(Dispatchers.IO) {
            collectibleSendPreviewUseCase.checkIfSenderAndReceiverAccountSame(
                senderAccountAddress = accountAddress,
                receiverAccountAddress = _selectedAccountAddressFlow.value,
                previousState = _collectibleSendPreviewFlow.value
            ).collect { collectibleSendPreview ->
                _collectibleSendPreviewFlow.emit(collectibleSendPreview)
            }
        }
    }

    fun checkIfSelectedAccountReceiveCollectible() {
        val nftSendPreview = _collectibleSendPreviewFlow.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            collectibleSendPreviewUseCase.checkIfSelectedAccountReceiveCollectible(
                publicKey = _selectedAccountAddressFlow.value,
                collectibleId = nftId,
                previousState = nftSendPreview
            ).collect { collectibleSendPreview ->
                _collectibleSendPreviewFlow.emit(collectibleSendPreview)
            }
        }
    }

    // TODO Transaction signing & sending flow needs to be refactored
    fun createSendTransactionData(): TransactionData.Send? {
        val senderAccountCacheData = accountCacheManager.getCacheData(accountAddress) ?: return null
        val targetUser = TargetUser(publicKey = selectedAccountAddressFlow.value)
        return TransactionData.Send(
            accountCacheData = senderAccountCacheData,
            amount = BigInteger.ONE,
            assetInformation = AssetInformation(
                assetId = nftId,
                verificationTier = null
            ),
            targetUser = targetUser
        )
    }

    fun createSendAndRemoveAssetTransactionData(): TransactionData.SendAndRemoveAsset? {
        val senderAccountCacheData = accountCacheManager.getCacheData(accountAddress) ?: return null
        val targetUser = TargetUser(publicKey = selectedAccountAddressFlow.value)
        return TransactionData.SendAndRemoveAsset(
            accountCacheData = senderAccountCacheData,
            amount = BigInteger.ONE,
            assetInformation = AssetInformation(
                assetId = nftId,
                verificationTier = null
            ),
            targetUser = targetUser
        )
    }

    fun sendSignedTransaction(signedTransactionDetail: SignedTransactionDetail.Send) {
        cachedPreviouslySignedTransaction = signedTransactionDetail
        val nftSendPreview = _collectibleSendPreviewFlow.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            collectibleSendPreviewUseCase.sendSignedTransaction(
                signedTransactionDetail = signedTransactionDetail,
                previousState = nftSendPreview
            ).collect { preview ->
                _collectibleSendPreviewFlow.emit(preview)
            }
        }
    }

    fun retrySendingTransaction() {
        cachedPreviouslySignedTransaction?.let { transaction ->
            sendSignedTransaction(transaction)
        }
    }

    fun updateNftDomainInformation(nftDomainAddressServiceLogoPair: Pair<String, String?>) {
        this.nftDomainAddressServiceLogoPair = nftDomainAddressServiceLogoPair
    }

    private fun initCollectibleSendPreview() {
        viewModelScope.launch {
            collectibleSendPreviewUseCase.getInitialStateOfCollectibleSendPreview(nftId).collect {
                _collectibleSendPreviewFlow.emit(it)
            }
        }
    }

    companion object {
        private const val ACCOUNT_ADDRESS_KEY = "accountAddress"
        private const val NFT_ID_KEY = "nftId"
    }
}
