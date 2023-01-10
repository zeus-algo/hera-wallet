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

package network.voi.hera.utils.walletconnect

import network.voi.hera.models.WalletConnectPeerMeta
import network.voi.hera.models.WalletConnectSession
import network.voi.hera.models.WalletConnectSessionMeta
import network.voi.hera.utils.getZonedDateTimeAsSec
import javax.inject.Inject
import org.walletconnect.Session

class WCWalletConnectMapper @Inject constructor() {

    fun createSessionConfig(sessionMeta: WalletConnectSessionMeta): Session.Config {
        return with(sessionMeta) {
            Session.Config(
                handshakeTopic = topic,
                bridge = bridge,
                key = key,
                version = version.toInt()
            )
        }
    }

    fun createWalletConnectPeerMeta(peerMeta: Session.PeerMeta): WalletConnectPeerMeta {
        return with(peerMeta) {
            WalletConnectPeerMeta(
                name = name.orEmpty(),
                url = url.orEmpty(),
                description = description.orEmpty(),
                icons = icons.orEmpty()
            )
        }
    }

    fun createWalletConnectSessionMeta(config: Session.Config): WalletConnectSessionMeta {
        return with(config) {
            WalletConnectSessionMeta(
                bridge = bridge.orEmpty(),
                key = key.orEmpty(),
                topic = handshakeTopic,
                version = version.toString()
            )
        }
    }

    fun createWalletConnectSession(
        sessionCachedData: WalletConnectSessionCachedData,
        accountsNames: List<String?> = emptyList()
    ): WalletConnectSession? {
        with(sessionCachedData) {
            val peerMeta = createWalletConnectPeerMeta(session.peerMeta() ?: return null)
            val sessionMeta = createWalletConnectSessionMeta(sessionConfig)
            return WalletConnectSession(
                id = sessionId,
                peerMeta = peerMeta,
                sessionMeta = sessionMeta,
                dateTimeStamp = getZonedDateTimeAsSec(),
                connectedAccountsAddresses = approvedAccounts,
                isConnected = true,
                accountsNames = accountsNames,
                fallbackBrowserGroupResponse = fallbackBrowserGroupResponse,
                isSubscribed = false
            )
        }
    }
}
