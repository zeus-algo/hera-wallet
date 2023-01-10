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

package network.voi.hera.discover.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.discover.common.ui.BaseDiscoverViewModel
import network.voi.hera.discover.detail.ui.model.DiscoverDetailPreview
import network.voi.hera.discover.detail.ui.usecase.DiscoverDetailPreviewUseCase
import network.voi.hera.discover.home.domain.model.TokenDetailInfo
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.utils.getOrThrow
import network.voi.hera.utils.preference.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverDetailViewModel @Inject constructor(
    private val discoverDetailPreviewUseCase: DiscoverDetailPreviewUseCase,
    private val currencyUseCase: CurrencyUseCase,
    savedStateHandle: SavedStateHandle
) : BaseDiscoverViewModel() {

    private val tokenDetail = savedStateHandle.getOrThrow<TokenDetailInfo>(TOKEN_DETAIL_KEY)

    private val _discoverDetailPreviewFlow = MutableStateFlow(
        discoverDetailPreviewUseCase.getInitialStatePreview(tokenDetail)
    )
    val discoverDetailPreviewFlow: StateFlow<DiscoverDetailPreview>
        get() = _discoverDetailPreviewFlow

    fun reloadPage() {
        clearLastError()
        viewModelScope.launch {
            _discoverDetailPreviewFlow
                .emit(discoverDetailPreviewUseCase.getInitialStatePreview(tokenDetail))
        }
    }

    fun getPrimaryCurrencyId(): String {
        return currencyUseCase.getPrimaryCurrencyId()
    }

    override fun onPageRequested() {
        viewModelScope.launch {
            _discoverDetailPreviewFlow
                .emit(discoverDetailPreviewUseCase.onPageRequested(_discoverDetailPreviewFlow.value))
        }
    }

    override fun onPageFinished() {
        viewModelScope.launch {
            _discoverDetailPreviewFlow
                .emit(discoverDetailPreviewUseCase.onPageFinished(_discoverDetailPreviewFlow.value))
        }
    }

    override fun onError() {
        viewModelScope.launch {
            _discoverDetailPreviewFlow
                .emit(discoverDetailPreviewUseCase.onError(_discoverDetailPreviewFlow.value))
        }
    }

    override fun onHttpError() {
        viewModelScope.launch {
            _discoverDetailPreviewFlow
                .emit(discoverDetailPreviewUseCase.onHttpError(_discoverDetailPreviewFlow.value))
        }
    }

    fun handleTokenDetailActionButtonClick(data: String) {
        viewModelScope.launch {
            discoverDetailPreviewUseCase.logTokenDetailActionButtonClick(data)
            _discoverDetailPreviewFlow
                .emit(
                    discoverDetailPreviewUseCase.handleTokenDetailActionButtonClick(
                        data,
                        _discoverDetailPreviewFlow.value
                    )
                )
        }
    }

    override fun getDiscoverThemePreference(): ThemePreference {
        return discoverDetailPreviewFlow.value.themePreference
    }

    companion object {
        private const val TOKEN_DETAIL_KEY = "tokenDetail"
    }
}
