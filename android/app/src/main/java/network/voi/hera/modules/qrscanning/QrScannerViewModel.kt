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

package network.voi.hera.modules.qrscanning

import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.modules.deeplink.ui.DeeplinkHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val deeplinkHandler: DeeplinkHandler
) : BaseViewModel() {

    private val _isQrCodeInProgressFlow = MutableSharedFlow<Boolean>()
    val isQrCodeInProgressFlow: SharedFlow<Boolean> = _isQrCodeInProgressFlow

    fun setQrCodeInProgress(isInProgress: Boolean) {
        viewModelScope.launch {
            _isQrCodeInProgressFlow.emit(isInProgress)
        }
    }

    fun setDeeplinkHandlerListener(listener: DeeplinkHandler.Listener) {
        deeplinkHandler.setListener(listener)
    }

    fun handleDeeplink(uri: String) {
        deeplinkHandler.handleDeepLink(uri)
    }

    fun removeDeeplinkHandlerListener() {
        deeplinkHandler.setListener(null)
    }
}
