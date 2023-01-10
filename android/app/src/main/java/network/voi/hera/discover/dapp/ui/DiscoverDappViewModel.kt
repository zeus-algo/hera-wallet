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

package network.voi.hera.discover.dapp.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.discover.common.ui.BaseDiscoverViewModel
import network.voi.hera.discover.dapp.ui.model.DiscoverDappPreview
import network.voi.hera.discover.dapp.ui.usecase.DiscoverDappPreviewUseCase
import network.voi.hera.modules.tracking.discover.dapp.DiscoverDappEventTracker
import network.voi.hera.utils.getOrThrow
import network.voi.hera.utils.preference.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverDappViewModel @Inject constructor(
    private val discoverDappPreviewUseCase: DiscoverDappPreviewUseCase,
    private val discoverDappEventTracker: DiscoverDappEventTracker,
    savedStateHandle: SavedStateHandle
) : BaseDiscoverViewModel() {

    private val dappUrl = savedStateHandle.getOrThrow<String>(DAPP_URL_KEY)
    private val dappTitle = savedStateHandle.getOrThrow<String>(DAPP_TITLE_KEY)

    private val _discoverDappPreviewFlow = MutableStateFlow(
        discoverDappPreviewUseCase.getInitialStatePreview(dappUrl, dappTitle)
    )
    val discoverDappPreviewFlow: StateFlow<DiscoverDappPreview>
        get() = _discoverDappPreviewFlow

    init {
        logDappVisitEvent()
    }

    private fun logDappVisitEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            discoverDappEventTracker.logDappVisitEvent(
                dappTitle = dappTitle,
                dappUrl = dappUrl
            )
        }
    }

    fun reloadPage() {
        clearLastError()
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.getInitialStatePreview(dappUrl, dappTitle))
        }
    }

    fun onHomeNavButtonClicked() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.requestLoadHomepage(_discoverDappPreviewFlow.value))
        }
    }

    fun onPreviousNavButtonClicked() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onPreviousNavButtonClicked(_discoverDappPreviewFlow.value))
        }
    }

    fun onNextNavButtonClicked() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onNextNavButtonClicked(_discoverDappPreviewFlow.value))
        }
    }

    override fun onPageRequested() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onPageRequested(_discoverDappPreviewFlow.value))
        }
    }

    override fun onPageFinished() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onPageFinished(_discoverDappPreviewFlow.value))
        }
    }

    override fun onError() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onError(_discoverDappPreviewFlow.value))
        }
    }

    override fun onHttpError() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onHttpError(_discoverDappPreviewFlow.value))
        }
    }

    override fun onPageUrlChanged() {
        viewModelScope.launch {
            _discoverDappPreviewFlow
                .emit(discoverDappPreviewUseCase.onPageUrlChanged(_discoverDappPreviewFlow.value))
        }
    }

    override fun getDiscoverThemePreference(): ThemePreference {
        return _discoverDappPreviewFlow.value.themePreference
    }

    companion object {
        private const val DAPP_URL_KEY = "dappUrl"
        private const val DAPP_TITLE_KEY = "dappTitle"
    }
}
