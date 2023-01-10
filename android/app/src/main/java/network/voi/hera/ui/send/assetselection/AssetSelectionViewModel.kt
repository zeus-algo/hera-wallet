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

package network.voi.hera.ui.send.assetselection

import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.models.AssetTransaction
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.nft.ui.model.AssetSelectionPreview
import network.voi.hera.usecase.AssetSelectionUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class AssetSelectionViewModel @Inject constructor(
    private val assetSelectionUseCase: AssetSelectionUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val simpleCollectibleUseCase: SimpleCollectibleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val assetTransaction = savedStateHandle.getOrThrow<AssetTransaction>(ASSET_TRANSACTION_KEY)

    val assetSelectionPreview: StateFlow<AssetSelectionPreview>
        get() = _assetSelectionPreview
    private val _assetSelectionPreview = MutableStateFlow(
        assetSelectionUseCase.getInitialStateOfAssetSelectionPreview(assetTransaction)
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            assetSelectionUseCase.getAssetSelectionListFlow(assetTransaction.senderAddress).collectLatest { list ->
                _assetSelectionPreview.emit(
                    _assetSelectionPreview.value.copy(
                        assetList = list,
                        isAssetListLoadingVisible = false
                    )
                )
            }
        }
    }

    fun shouldShowTransactionTips(): Boolean {
        return assetSelectionUseCase.shouldShowTransactionTips()
    }

    fun isReceiverAccountSet(): Boolean {
        return assetTransaction.receiverUser != null
    }

    fun updatePreviewWithSelectedAsset(assetId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            assetSelectionUseCase.getUpdatedPreviewFlowWithSelectedAsset(
                assetId = assetId,
                previousState = _assetSelectionPreview.value
            ).collectLatest { assetSelectionPreview ->
                _assetSelectionPreview.emit(assetSelectionPreview)
            }
        }
    }

    fun getAssetOrCollectibleNameOrNull(assetId: Long): String? {
        return simpleAssetDetailUseCase.getCachedAssetDetail(assetId)?.data?.fullName
            ?: simpleCollectibleUseCase.getCachedCollectibleById(assetId)?.data?.fullName
    }

    companion object {
        private const val ASSET_TRANSACTION_KEY = "assetTransaction"
    }
}
