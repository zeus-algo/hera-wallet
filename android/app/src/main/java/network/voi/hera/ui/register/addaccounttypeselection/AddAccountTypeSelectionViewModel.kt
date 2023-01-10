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

package network.voi.hera.ui.register.addaccounttypeselection

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.AccountManager
import network.voi.hera.modules.tracking.onboarding.register.addaccounttypeselection.AddAccountTypeSelectionFragmentEventTracker
import network.voi.hera.utils.preference.setRegisterSkip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel
class AddAccountTypeSelectionViewModel @Inject constructor(
    private val sharedPref: SharedPreferences,
    private val accountManager: AccountManager,
    private val addAccountTypeSelectionFragmentEventTracker: AddAccountTypeSelectionFragmentEventTracker
) : ViewModel() {

    fun setRegisterSkip() {
        sharedPref.setRegisterSkip()
    }

    fun hasAccount(): Boolean {
        return accountManager.accounts.value.isNotEmpty()
    }

    fun logOnboardingCreateNewAccountClickEvent() {
        viewModelScope.launch {
            addAccountTypeSelectionFragmentEventTracker.logOnboardingCreateNewAccountEvent()
        }
    }

    fun logOnboardingCreateWatchAccountClickEvent() {
        viewModelScope.launch {
            addAccountTypeSelectionFragmentEventTracker.logOnboardingCreateWatchAccountEvent()
        }
    }
}
