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

package network.voi.hera.ui.common.accountselector

import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.models.AccountSelection
import network.voi.hera.usecase.AccountSelectionUseCase
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountSelectionViewModel @Inject constructor(
    private val accountSelectionUseCase: AccountSelectionUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assetId = savedStateHandle.getOrThrow<Long>(ASSET_ID_KEY)

    private val _cachedAccountFlow = MutableStateFlow<List<AccountSelection>?>(null)
    val cachedAccountFlow: StateFlow<List<AccountSelection>?> = _cachedAccountFlow

    init {
        getCachedAccountFilteredByAssetId()
    }

    private fun getCachedAccountFilteredByAssetId() {
        viewModelScope.launch {
            _cachedAccountFlow.emit(accountSelectionUseCase.getAccountFilteredByAssetId(assetId))
        }
    }

    companion object {
        private const val ASSET_ID_KEY = "assetId"
    }
}
