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

package network.voi.hera.ui.send.transferpreview

import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.R
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetTransferPreview
import network.voi.hera.models.SignedTransactionDetail
import network.voi.hera.models.TransactionData
import network.voi.hera.usecase.AssetTransferPreviewUseCase
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.Event
import network.voi.hera.utils.Resource
import network.voi.hera.utils.Resource.Error.GlobalWarning
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class AssetTransferPreviewViewModel @Inject constructor(
    private val assetTransferPreviewUserCase: AssetTransferPreviewUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var sendAlgoJob: Job? = null
    private val transactionData =
        savedStateHandle.getOrThrow<TransactionData.Send>(TRANSACTION_DATA_KEY)

    private val _sendAlgoResponseFlow = MutableStateFlow<Event<Resource<String>>?>(null)
    val sendAlgoResponseFlow: StateFlow<Event<Resource<String>>?> = _sendAlgoResponseFlow

    private val _assetTransferPreviewFlow = MutableStateFlow<AssetTransferPreview?>(null)
    val assetTransferPreviewFlow: StateFlow<AssetTransferPreview?> = _assetTransferPreviewFlow

    init {
        getAssetTransferPreview()
    }

    private fun getAssetTransferPreview() {
        viewModelScope.launch {
            val signedTransactionPreview = assetTransferPreviewUserCase.getAssetTransferPreview(transactionData)
            _assetTransferPreviewFlow.emit(signedTransactionPreview)
        }
    }

    fun sendSignedTransaction(signedTransactionDetail: SignedTransactionDetail.Send) {
        if (sendAlgoJob?.isActive == true) {
            return
        }
        sendAlgoJob = viewModelScope.launch {
            assetTransferPreviewUserCase.sendSignedTransaction(signedTransactionDetail).collectLatest {
                when (it) {
                    is DataResource.Loading -> _sendAlgoResponseFlow.emit(Event(Resource.Loading))
                    is DataResource.Error -> {
                        if (it.exception != null) {
                            _sendAlgoResponseFlow.emit(Event(Resource.Error.Api(it.exception!!)))
                        } else {
                            _sendAlgoResponseFlow.emit(
                                Event(GlobalWarning(R.string.error, AnnotatedString(R.string.an_error_occured)))
                            )
                        }
                    }
                    is DataResource.Success -> _sendAlgoResponseFlow.emit(Event(Resource.Success(it.data)))
                }
            }
        }
    }

    fun onNoteUpdate(newNote: String) {
        viewModelScope.launch {
            if (_assetTransferPreviewFlow.value?.isNoteEditable == true) {
                val newPreview = _assetTransferPreviewFlow.value?.copy(note = newNote)
                _assetTransferPreviewFlow.emit(newPreview)
            }
        }
    }

    fun getTransactionData(): TransactionData.Send {
        return if (_assetTransferPreviewFlow.value?.isNoteEditable == true) {
            transactionData.copy(
                note = _assetTransferPreviewFlow.value?.note,
                xnote = null
            )
        } else {
            transactionData.copy(
                note = null,
                xnote = _assetTransferPreviewFlow.value?.note
            )
        }
    }

    companion object {
        private const val TRANSACTION_DATA_KEY = "transactionData"
    }
}
