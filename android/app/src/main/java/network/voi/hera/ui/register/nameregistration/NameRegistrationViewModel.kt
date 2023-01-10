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

package network.voi.hera.ui.register.nameregistration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.models.Account
import network.voi.hera.models.AccountCreation
import network.voi.hera.models.ui.NameRegistrationPreview
import network.voi.hera.usecase.IsAccountLimitExceedUseCase
import network.voi.hera.usecase.NameRegistrationPreviewUseCase
import network.voi.hera.utils.analytics.CreationType
import network.voi.hera.utils.getOrThrow
import network.voi.hera.utils.toShortenedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NameRegistrationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nameRegistrationPreviewUseCase: NameRegistrationPreviewUseCase,
    private val isAccountLimitExceedUseCase: IsAccountLimitExceedUseCase
) : ViewModel() {

    private val _nameRegistrationPreviewFlow = MutableStateFlow(getInitialPreview())
    val nameRegistrationPreviewFlow: Flow<NameRegistrationPreview>
        get() = _nameRegistrationPreviewFlow

    private val accountCreation = savedStateHandle.getOrThrow<AccountCreation>(ACCOUNT_CREATION_KEY)
    private val accountAddress = accountCreation.tempAccount.address
    private val accountName = accountCreation.tempAccount.name

    val predefinedAccountName: String
        get() = accountName.takeIf { it.isNotBlank() } ?: accountAddress.toShortenedAddress()

    fun updatePreviewWithAccountCreation(accountCreation: AccountCreation?, inputName: String) {
        viewModelScope.launch {
            nameRegistrationPreviewUseCase.getPreviewWithAccountCreation(
                accountCreation = accountCreation,
                inputName = inputName
            )?.let {
                _nameRegistrationPreviewFlow.emit(it)
            }
        }
    }

    fun updateWatchAccount(accountCreation: AccountCreation) {
        viewModelScope.launch {
            nameRegistrationPreviewUseCase.updateTypeOfWatchAccount(accountCreation)
            nameRegistrationPreviewUseCase.updateNameOfWatchAccount(accountCreation)
            _nameRegistrationPreviewFlow.emit(nameRegistrationPreviewUseCase.getOnWatchAccountUpdatedPreview())
        }
    }

    fun addNewAccount(account: Account, creationType: CreationType?) {
        // TODO: Handle error case
        nameRegistrationPreviewUseCase.addNewAccount(account, creationType)
    }

    private fun getInitialPreview(): NameRegistrationPreview {
        return nameRegistrationPreviewUseCase.getInitialPreview()
    }

    fun isAccountLimitExceed(): Boolean {
        return isAccountLimitExceedUseCase.isAccountLimitExceed()
    }

    companion object {
        private const val ACCOUNT_CREATION_KEY = "accountCreation"
    }
}
