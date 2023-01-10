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

package network.voi.hera.modules.walletconnect.connectedapps.ui.domain

import network.voi.hera.models.WalletConnectSession
import network.voi.hera.modules.walletconnect.connectedapps.ui.mapper.WalletConnectConnectedAppsPreviewMapper
import network.voi.hera.modules.walletconnect.connectedapps.ui.model.WalletConnectConnectedAppsPreview
import network.voi.hera.modules.walletconnect.domain.ConnectToExistingSessionUseCase
import network.voi.hera.modules.walletconnect.domain.GetWalletConnectLocalSessionsUseCase
import network.voi.hera.modules.walletconnect.domain.KillWalletConnectSessionUseCase
import network.voi.hera.modules.walletconnect.sessions.ui.mapper.WalletConnectSessionItemMapper
import network.voi.hera.modules.walletconnect.sessions.ui.model.WalletConnectSessionItem
import network.voi.hera.utils.Event
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletConnectConnectedAppsPreviewUseCase @Inject constructor(
    private val connectToExistingSessionUseCase: ConnectToExistingSessionUseCase,
    private val killWalletConnectSessionUseCase: KillWalletConnectSessionUseCase,
    private val getWalletConnectLocalSessionsUseCase: GetWalletConnectLocalSessionsUseCase,
    private val walletConnectSessionItemMapper: WalletConnectSessionItemMapper,
    private val walletConnectConnectedAppsPreviewMapper: WalletConnectConnectedAppsPreviewMapper
) {

    fun killWalletConnectSession(sessionId: Long) {
        killWalletConnectSessionUseCase(sessionId)
    }

    fun connectToExistingSession(sessionId: Long) {
        connectToExistingSessionUseCase(sessionId)
    }

    fun getWalletConnectConnectedAppsPreviewFlow(): Flow<WalletConnectConnectedAppsPreview> {
        return getWalletConnectLocalSessionsUseCase().map {
            val walletConnectSessionList = createWalletConnectSessionList(it)
            walletConnectConnectedAppsPreviewMapper.mapToWalletConnectConnectedAppsPreview(
                walletConnectSessionList = walletConnectSessionList,
                navigateBackEvent = if (walletConnectSessionList.isEmpty()) Event(Unit) else null
            )
        }
    }

    private fun createWalletConnectSessionList(
        walletConnectSessions: List<WalletConnectSession>
    ): List<WalletConnectSessionItem> {
        return walletConnectSessions.map { walletConnectSession ->
            with(walletConnectSession) {
                walletConnectSessionItemMapper.mapToWalletConnectSessionItem(
                    sessionId = id,
                    dAppLogoUrl = peerMeta.peerIconUri,
                    dAppName = peerMeta.name,
                    dAppDescription = null,
                    connectionDate = null,
                    connectedAccountItems = null,
                    isConnected = isConnected
                )
            }
        }
    }
}
