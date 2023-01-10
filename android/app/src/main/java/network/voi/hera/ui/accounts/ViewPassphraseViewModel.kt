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

package network.voi.hera.ui.accounts

import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import network.voi.hera.core.AccountManager
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ViewPassphraseViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountPublicKeyArg by lazy { savedStateHandle.get<String>(ACCOUNT_PUBLIC_KEY).orEmpty() }

    fun getAccountSecretKey(): ByteArray? {
        return accountManager.getAccount(accountPublicKeyArg)?.getSecretKey()
    }

    companion object {
        private const val ACCOUNT_PUBLIC_KEY = "accountPublicKey"
    }
}
