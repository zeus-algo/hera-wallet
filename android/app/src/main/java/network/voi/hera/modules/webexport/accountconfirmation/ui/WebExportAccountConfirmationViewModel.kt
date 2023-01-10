/*
 *  Copyright 2022 Pera Wallet, LDA
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package network.voi.hera.modules.webexport.accountconfirmation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.modules.webexport.accountconfirmation.ui.model.WebExportAccountConfirmationPreview
import network.voi.hera.modules.webexport.accountconfirmation.ui.usecase.WebExportAccountConfirmationPreviewUseCase
import network.voi.hera.modules.webexport.utils.NAVIGATION_ACCOUNT_LIST_KEY
import network.voi.hera.modules.webexport.utils.NAVIGATION_BACKUP_ID_KEY
import network.voi.hera.modules.webexport.utils.NAVIGATION_ENCRYPTION_KEY
import network.voi.hera.modules.webexport.utils.NAVIGATION_MODIFICATION_KEY
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WebExportAccountConfirmationViewModel @Inject constructor(
    private val webExportAccountConfirmationPreviewUseCase: WebExportAccountConfirmationPreviewUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val backupId = savedStateHandle.getOrThrow<String>(NAVIGATION_BACKUP_ID_KEY)
    private val modificationKey = savedStateHandle.getOrThrow<String>(NAVIGATION_MODIFICATION_KEY)
    private val encryptionKey = savedStateHandle.getOrThrow<String>(NAVIGATION_ENCRYPTION_KEY)
    private val accountList = savedStateHandle.getOrThrow<Array<String>>(NAVIGATION_ACCOUNT_LIST_KEY).toList()

    val webExportAccountConfirmationPreviewFlow: StateFlow<WebExportAccountConfirmationPreview>
        get() = _webExportAccountConfirmationPreviewFlow
    private val _webExportAccountConfirmationPreviewFlow = MutableStateFlow(getInitialPreview())

    private fun getInitialPreview(): WebExportAccountConfirmationPreview {
        return webExportAccountConfirmationPreviewUseCase.getInitialPreview(
            backupId = backupId,
            modificationKey = modificationKey,
            encryptionKey = encryptionKey,
            accountAddresses = accountList
        )
    }

    fun onConfirmExport() {
        viewModelScope.launch {
            webExportAccountConfirmationPreviewUseCase.exportEncryptedBackup(
                backupId = backupId,
                modificationKey = modificationKey,
                encryptionKey = encryptionKey,
                accountAddresses = accountList,
                previousState = _webExportAccountConfirmationPreviewFlow.value
            ).collect { preview ->
                _webExportAccountConfirmationPreviewFlow.emit(preview)
            }
        }
    }
}
