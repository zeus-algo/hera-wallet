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

package network.voi.hera.modules.assets.manage

import androidx.lifecycle.SavedStateHandle
import network.voi.hera.core.BaseViewModel
import network.voi.hera.utils.getOrElse
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageAssetsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val accountAddress = savedStateHandle.getOrThrow<String>(ACCOUNT_ADDRESS_KEY)
    val canSignTransaction = savedStateHandle.getOrElse(CAN_SIGN_TRANSACTION_KEY, false)

    companion object {
        private const val ACCOUNT_ADDRESS_KEY = "publicKey"
        private const val CAN_SIGN_TRANSACTION_KEY = "canSignTransaction"
    }
}
