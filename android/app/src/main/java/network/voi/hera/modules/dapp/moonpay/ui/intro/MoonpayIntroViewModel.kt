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

package network.voi.hera.modules.dapp.moonpay.ui.intro

import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.modules.dapp.moonpay.data.remote.model.SignMoonpayUrlResponse
import network.voi.hera.modules.dapp.moonpay.domain.usecase.MoonpaySignUrlUseCase
import network.voi.hera.modules.tracking.moonpay.MoonpayAlgoBuyTapEventTracker
import network.voi.hera.network.AlgodInterceptor
import network.voi.hera.utils.MAINNET_NETWORK_SLUG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class MoonpayIntroViewModel @Inject constructor(
    private val algodInterceptor: AlgodInterceptor,
    private val moonpaySignUrlUseCase: MoonpaySignUrlUseCase,
    private val moonpayAlgoBuyTapEventTracker: MoonpayAlgoBuyTapEventTracker
) : BaseViewModel() {

    val signMoonpayUrlFlow = MutableStateFlow<SignMoonpayUrlResponse?>(null)

    fun signMoonpayUrl(walletAddress: String) {
        viewModelScope.launch {
            moonpaySignUrlUseCase(walletAddress).collect { response ->
                signMoonpayUrlFlow?.emit(response)
            }
        }
    }

    fun isMainNet(): Boolean {
        return algodInterceptor.currentActiveNode?.networkSlug == MAINNET_NETWORK_SLUG
    }

    fun logBuyAlgoTapEvent() {
        viewModelScope.launch {
            moonpayAlgoBuyTapEventTracker.logMoonpayAlgoBuyTapEvent()
        }
    }
}
