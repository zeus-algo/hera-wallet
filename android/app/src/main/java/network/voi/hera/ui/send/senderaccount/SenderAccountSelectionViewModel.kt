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

package network.voi.hera.ui.send.senderaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.models.AccountCacheData
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.AssetTransaction
import network.voi.hera.models.SenderAccountSelectionPreview
import network.voi.hera.usecase.SenderAccountSelectionPreviewUseCase
import network.voi.hera.usecase.SenderAccountSelectionUseCase
import network.voi.hera.utils.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SenderAccountSelectionViewModel @Inject constructor(
    private val senderAccountSelectionUseCase: SenderAccountSelectionUseCase,
    private val senderAccountSelectionPreviewUseCase: SenderAccountSelectionPreviewUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val assetTransaction: AssetTransaction = savedStateHandle.getOrElse(ASSET_TRANSACTION_KEY, AssetTransaction())

    private val _senderAccountSelectionPreviewFlow =
        MutableStateFlow(senderAccountSelectionPreviewUseCase.getInitialPreview())
    val senderAccountSelectionPreviewFlow: StateFlow<SenderAccountSelectionPreview> = _senderAccountSelectionPreviewFlow

    init {
        // If user came with deeplink or qr code then we have to filter accounts that have incoming asset id
        if (assetTransaction.assetId != -1L && assetTransaction.assetId != ALGO_ID) {
            getAccountCacheWithSpecificAsset(assetTransaction.assetId)
        } else {
            getAccounts()
        }
    }

    private fun getAccounts() {
        viewModelScope.launch {
            _senderAccountSelectionPreviewFlow.emit(
                senderAccountSelectionPreviewUseCase.getUpdatedPreviewWithAccountList(
                    preview = _senderAccountSelectionPreviewFlow.value
                )
            )
        }
    }

    private fun getAccountCacheWithSpecificAsset(assetId: Long) {
        viewModelScope.launch {
            _senderAccountSelectionPreviewFlow.emit(
                senderAccountSelectionPreviewUseCase.getUpdatedPreviewWithAccountListAndSpecificAsset(
                    assetId = assetId,
                    preview = _senderAccountSelectionPreviewFlow.value
                )
            )
        }
    }

    fun fetchFromAccountInformation(fromAccountAddress: String) {
        viewModelScope.launch {
            senderAccountSelectionPreviewUseCase.getUpdatedPreviewFlowWithAccountInformation(
                fromAccountAddress = fromAccountAddress,
                viewModelScope = viewModelScope,
                preview = _senderAccountSelectionPreviewFlow.value
            ).collectLatest {
                _senderAccountSelectionPreviewFlow.emit(it)
            }
        }
    }

    fun shouldShowTransactionTips(): Boolean {
        return senderAccountSelectionUseCase.shouldShowTransactionTips()
    }

    fun getAssetInformation(senderAddress: String): AssetInformation? {
        return senderAccountSelectionUseCase.getAssetInformation(senderAddress, assetTransaction.assetId)
    }

    fun getAccountCachedData(senderAddress: String): AccountCacheData? {
        return senderAccountSelectionUseCase.getAccountInformation(senderAddress)
    }

    companion object {
        private const val ASSET_TRANSACTION_KEY = "assetTransaction"
    }
}
