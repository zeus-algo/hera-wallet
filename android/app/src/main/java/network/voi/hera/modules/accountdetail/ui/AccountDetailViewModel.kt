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

package network.voi.hera.modules.accountdetail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.models.AccountDetailSummary
import network.voi.hera.models.AccountDetailTab
import network.voi.hera.modules.accountdetail.ui.model.AccountDetailPreview
import network.voi.hera.modules.accountdetail.ui.usecase.AccountDetailPreviewUseCase
import network.voi.hera.modules.tracking.accountdetail.AccountDetailFragmentEventTracker
import network.voi.hera.usecase.AccountDeletionUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.Event
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val accountDeletionUseCase: AccountDeletionUseCase,
    savedStateHandle: SavedStateHandle,
    private val accountDetailFragmentEventTracker: AccountDetailFragmentEventTracker,
    private val accountDetailPreviewUseCase: AccountDetailPreviewUseCase
) : ViewModel() {

    val accountPublicKey: String = savedStateHandle.getOrThrow(ACCOUNT_PUBLIC_KEY)
    private val accountDetailTab = savedStateHandle.get<AccountDetailTab?>(ACCOUNT_DETAIL_TAB)

    val accountDetailSummaryFlow: StateFlow<AccountDetailSummary?> get() = _accountDetailSummaryFlow
    private val _accountDetailSummaryFlow = MutableStateFlow<AccountDetailSummary?>(null)

    private val _accountDetailTabArgFlow = MutableStateFlow<Event<Int>?>(null)
    val accountDetailTabArgFlow: StateFlow<Event<Int>?> get() = _accountDetailTabArgFlow

    // TODO Combine accountDetailSummaryFlow and accountDetailPreviewFlow
    private val _accountDetailPreviewFlow = MutableStateFlow<AccountDetailPreview?>(null)
    val accountDetailPreviewFlow: StateFlow<AccountDetailPreview?>
        get() = _accountDetailPreviewFlow

    init {
        initAccountDetailSummary()
        initAccountDetailPreview()
        checkAccountDetailTabArg()
    }

    private fun checkAccountDetailTabArg() {
        viewModelScope.launch {
            accountDetailTab?.tabIndex?.run {
                _accountDetailTabArgFlow.emit(Event(this))
            }
        }
    }

    fun removeAccount(publicKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDeletionUseCase.removeAccount(publicKey)
        }
    }

    private fun initAccountDetailPreview() {
        _accountDetailPreviewFlow.value = accountDetailPreviewUseCase.getInitialPreview()
    }

    fun initAccountDetailSummary() {
        viewModelScope.launch {
            _accountDetailSummaryFlow.emit(accountDetailUseCase.getAccountSummary(accountPublicKey))
        }
    }

    fun logAccountDetailAssetsTapEventTracker() {
        viewModelScope.launch {
            accountDetailFragmentEventTracker.logAccountDetailAssetsTapEvent()
        }
    }

    fun logAccountDetailCollectiblesTapEventTracker() {
        viewModelScope.launch {
            accountDetailFragmentEventTracker.logAccountDetailCollectiblesTapEvent()
        }
    }

    fun logAccountDetailTransactionHistoryTapEventTracker() {
        viewModelScope.launch {
            accountDetailFragmentEventTracker.logAccountDetailTransactionHistoryTapEvent()
        }
    }

    fun getCanSignTransaction(): Boolean {
        return accountDetailSummaryFlow.value?.canSignTransaction ?: false
    }

    fun onSwapClick() {
        viewModelScope.launch {
            with(_accountDetailPreviewFlow) {
                accountDetailFragmentEventTracker.logAccountDetailSwapButtonClickEvent()
                val newState = accountDetailPreviewUseCase.getSwapNavigationUpdatedPreview(
                    accountPublicKey, value ?: return@with
                )
                emit(newState)
            }
        }
    }

    fun onAssetLongClick(assetId: Long) {
        viewModelScope.launch {
            with(_accountDetailPreviewFlow) {
                emit(accountDetailPreviewUseCase.getAssetLongClickUpdatedPreview(value ?: return@with, assetId))
            }
        }
    }

    companion object {
        private const val ACCOUNT_PUBLIC_KEY = "publicKey"
        private const val ACCOUNT_DETAIL_TAB = "accountDetailTab"
    }
}
