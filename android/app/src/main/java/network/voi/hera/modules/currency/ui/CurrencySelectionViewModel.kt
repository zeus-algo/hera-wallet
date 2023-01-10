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

package network.voi.hera.modules.currency.ui

import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.ui.CurrencySelectionPreview
import network.voi.hera.modules.currency.domain.model.SelectedCurrency
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.currency.ui.usecase.CurrencySelectionPreviewUseCase
import network.voi.hera.ui.settings.selection.CurrencyListItem
import network.voi.hera.utils.analytics.logCurrencyChange
import network.voi.hera.utils.coremanager.ParityManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val currencyUseCase: CurrencyUseCase,
    private val currencySelectionPreviewUseCase: CurrencySelectionPreviewUseCase,
    private val parityManager: ParityManager
) : BaseViewModel() {

    val currencySelectionPreviewFlow: Flow<CurrencySelectionPreview?>
        get() = _currencySelectionPreviewFlow
    private val _currencySelectionPreviewFlow = MutableStateFlow<CurrencySelectionPreview?>(null)

    val selectedCurrencyFlow: Flow<SelectedCurrency>
        get() = _selectedCurrencyFlow
    private val _selectedCurrencyFlow = MutableStateFlow(currencyUseCase.getSelectedCurrency())

    private var previewJob: Job? = null

    private var searchKeyword = ""

    init {
        initPreviewFlow()
    }

    private fun initPreviewFlow() {
        previewJob = getPreviewJob()
    }

    fun refreshPreview() {
        previewJob?.cancel()
        previewJob = getPreviewJob()
    }

    fun updateSearchKeyword(searchKeyword: String) {
        this.searchKeyword = searchKeyword
        refreshPreview()
    }

    private fun getPreviewJob(): Job {
        return viewModelScope.launch {
            currencySelectionPreviewUseCase.getCurrencySelectionPreviewFlow(searchKeyword).collectLatest {
                _currencySelectionPreviewFlow.value = it
            }
        }
    }

    fun setCurrencySelected(currencyListItem: CurrencyListItem) {
        currencyUseCase.setPrimaryCurrency(currencyListItem.currencyId)
        logCurrencyChange(currencyListItem.currencyId)
        _selectedCurrencyFlow.value = currencyUseCase.getSelectedCurrency()
        viewModelScope.launch {
            parityManager.refreshSelectedCurrencyDetailCache()
        }
    }

    private fun logCurrencyChange(newCurrencyId: String) {
        firebaseAnalytics.logCurrencyChange(newCurrencyId)
    }
}
