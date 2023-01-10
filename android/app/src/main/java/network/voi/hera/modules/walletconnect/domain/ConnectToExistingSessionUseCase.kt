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

package network.voi.hera.modules.walletconnect.domain

import network.voi.hera.utils.walletconnect.WalletConnectManager
import javax.inject.Inject

class ConnectToExistingSessionUseCase @Inject constructor(
    private val walletConnectManager: WalletConnectManager
) {

    operator fun invoke(sessionId: Long) {
        val walletConnectSession = walletConnectManager.getWalletConnectSession(sessionId) ?: return
        walletConnectManager.connectToExistingSession(walletConnectSession)
    }
}
