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

package network.voi.hera.modules.accounts.ui

import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.modules.accounts.domain.model.AccountPreview
import network.voi.hera.modules.accounts.domain.usecase.AccountsPreviewUseCase
import network.voi.hera.modules.tracking.accounts.AccountsEventTracker
import network.voi.hera.usecase.IsAccountLimitExceedUseCase
import network.voi.hera.utils.coremanager.ParityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountsPreviewUseCase: AccountsPreviewUseCase,
    private val accountsEventTracker: AccountsEventTracker,
    private val parityManager: ParityManager,
    private val isAccountLimitExceedUseCase: IsAccountLimitExceedUseCase
) : BaseViewModel() {

    private val _accountPreviewFlow = MutableStateFlow<AccountPreview?>(null)
    val accountPreviewFlow: Flow<AccountPreview?>
        get() = _accountPreviewFlow

    init {
        initializeAccountPreviewFlow()
    }

    fun refreshCachedAlgoPrice() {
        viewModelScope.launch {
            parityManager.refreshSelectedCurrencyDetailCache()
        }
    }

    fun onCloseBannerClick(bannerId: Long) {
        viewModelScope.launch {
            accountsPreviewUseCase.onCloseBannerClick(bannerId)
        }
    }

    private fun initializeAccountPreviewFlow() {
        viewModelScope.launch {
            val initialAccountPreview = accountsPreviewUseCase.getInitialAccountPreview()
            _accountPreviewFlow.emit(initialAccountPreview)
            accountsPreviewUseCase.getAccountsPreview(initialAccountPreview).collectLatest {
                _accountPreviewFlow.emit(it)
            }
        }
    }

    fun logQrScanTapEvent() {
        viewModelScope.launch {
            accountsEventTracker.logQrScanTapEvent()
        }
    }

    fun logAddAccountTapEvent() {
        viewModelScope.launch {
            accountsEventTracker.logAddAccountTapEvent()
        }
    }

    fun logAccountsFragmentAlgoBuyTapEvent() {
        viewModelScope.launch {
            accountsEventTracker.logAccountsFragmentAlgoBuyTapEvent()
        }
    }

    fun onBannerActionButtonClick(isGovernance: Boolean) {
        if (isGovernance) {
            viewModelScope.launch {
                accountsEventTracker.logVisitGovernanceEvent()
            }
        }
    }

    fun isAccountLimitExceed(): Boolean {
        return isAccountLimitExceedUseCase.isAccountLimitExceed()
    }

    fun dismissTutorial(tutorialId: Int) {
        viewModelScope.launch {
            accountsPreviewUseCase.dismissTutorial(tutorialId)
        }
    }

    fun onSwapClick() {
        viewModelScope.launch {
            accountsEventTracker.logSwapClickEvent()
            updatePreviewForSwapNavigation()
        }
    }

    fun onSwapClickFromTutorialDialog() {
        viewModelScope.launch {
            accountsEventTracker.logSwapTutorialTrySwapClickEvent()
            updatePreviewForSwapNavigation()
        }
    }

    fun onSwapLaterClick() {
        viewModelScope.launch {
            accountsEventTracker.logSwapLaterClickEvent()
        }
    }

    private suspend fun updatePreviewForSwapNavigation() {
        with(_accountPreviewFlow) {
            val newState = accountsPreviewUseCase.getSwapNavigationUpdatedPreview(value ?: return@with)
            emit(newState)
        }
    }
}
