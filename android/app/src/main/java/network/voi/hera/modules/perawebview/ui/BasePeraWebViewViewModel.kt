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

package network.voi.hera.modules.perawebview.ui

import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.customviews.PeraWebView
import network.voi.hera.discover.common.ui.model.WebViewError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BasePeraWebViewViewModel : BaseViewModel() {

    private val _peraWebViewFlow: MutableStateFlow<PeraWebView?> = MutableStateFlow(null)
    private val _lastErrorFlow: MutableStateFlow<WebViewError?> = MutableStateFlow(null)

    abstract fun onPageRequested()
    abstract fun onPageFinished()
    abstract fun onError()
    abstract fun onHttpError()
    open fun onPageUrlChanged() {}

    fun saveWebView(webView: PeraWebView?) {
        viewModelScope.launch {
            _peraWebViewFlow
                .emit(webView)
        }
    }

    fun getWebView(): PeraWebView? {
        return _peraWebViewFlow.value
    }

    fun saveLastError(error: WebViewError?) {
        viewModelScope.launch {
            _lastErrorFlow
                .emit(error)
        }
    }

    fun getLastError(): WebViewError? {
        return _lastErrorFlow.value
    }

    fun clearLastError() {
        viewModelScope.launch {
            _lastErrorFlow.emit(null)
        }
    }
}
