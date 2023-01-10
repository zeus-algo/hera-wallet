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

package network.voi.hera.modules.walletconnect.sessions.ui.domain

import network.voi.hera.R
import network.voi.hera.models.WalletConnectSession
import network.voi.hera.modules.walletconnect.domain.ConnectToExistingSessionUseCase
import network.voi.hera.modules.walletconnect.domain.GetWalletConnectLocalSessionsUseCase
import network.voi.hera.modules.walletconnect.domain.KillAllWalletConnectSessionsUseCase
import network.voi.hera.modules.walletconnect.domain.KillWalletConnectSessionUseCase
import network.voi.hera.modules.walletconnect.sessions.ui.mapper.WalletConnectSessionItemMapper
import network.voi.hera.modules.walletconnect.sessions.ui.mapper.WalletConnectSessionsPreviewMapper
import network.voi.hera.modules.walletconnect.sessions.ui.model.WalletConnectSessionItem
import network.voi.hera.modules.walletconnect.sessions.ui.model.WalletConnectSessionsPreview
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.formatAsDateAndTime
import network.voi.hera.utils.getZonedDateTimeFromSec
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletConnectSessionsPreviewUseCase @Inject constructor(
    private val killWalletConnectSessionUseCase: KillWalletConnectSessionUseCase,
    private val connectToExistingSessionUseCase: ConnectToExistingSessionUseCase,
    private val killAllWalletConnectSessionsUseCase: KillAllWalletConnectSessionsUseCase,
    private val getWalletConnectLocalSessionsUseCase: GetWalletConnectLocalSessionsUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val walletConnectSessionsPreviewMapper: WalletConnectSessionsPreviewMapper,
    private val walletConnectSessionItemMapper: WalletConnectSessionItemMapper
) {

    fun killWalletConnectSession(sessionId: Long) {
        killWalletConnectSessionUseCase(sessionId)
    }

    fun connectToExistingSession(sessionId: Long) {
        connectToExistingSessionUseCase(sessionId)
    }

    fun killAllWalletConnectSessions() {
        killAllWalletConnectSessionsUseCase()
    }

    fun getWalletConnectSessionsPreviewFlow(): Flow<WalletConnectSessionsPreview> {
        return getWalletConnectLocalSessionsUseCase().map {
            val walletConnectSessionList = createWalletConnectSessionList(it)
            walletConnectSessionsPreviewMapper.mapToWalletConnectSessionsPreview(
                walletConnectSessionList = walletConnectSessionList,
                isDisconnectAllSessionVisible = walletConnectSessionList.isNotEmpty(),
                isEmptyStateVisible = walletConnectSessionList.isEmpty()
            )
        }
    }

    private fun createWalletConnectSessionList(
        walletConnectSessions: List<WalletConnectSession>,
    ): List<WalletConnectSessionItem> {
        return walletConnectSessions.map { walletConnectSession ->
            with(walletConnectSession) {
                walletConnectSessionItemMapper.mapToWalletConnectSessionItem(
                    sessionId = id,
                    dAppLogoUrl = peerMeta.peerIconUri,
                    dAppName = peerMeta.name,
                    dAppDescription = peerMeta.description,
                    connectionDate = getZonedDateTimeFromSec(dateTimeStamp)?.formatAsDateAndTime(),
                    connectedAccountItems = createConnectedAccountItems(connectedAccountsAddresses, isConnected),
                    isConnected = isConnected
                )
            }
        }
    }

    private fun createConnectedAccountItems(
        connectedAccounts: List<String>,
        isConnected: Boolean
    ): List<WalletConnectSessionItem.ConnectedSessionAccountItem> {
        val backgroundResource = if (isConnected) R.drawable.bg_connected_session_indicator else null
        val textColor = if (isConnected) R.color.link_primary else R.color.gray_400
        return connectedAccounts.map {
            walletConnectSessionItemMapper.mapToConnectedSessionAccountItem(
                backgroundResource = backgroundResource,
                textColor = textColor,
                displayName = accountDetailUseCase.getAccountName(it)
            )
        }
    }
}
