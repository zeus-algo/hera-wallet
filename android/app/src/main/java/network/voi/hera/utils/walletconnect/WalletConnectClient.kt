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

import network.voi.hera.models.WalletConnectSession
import network.voi.hera.models.WalletConnectSessionMeta
import network.voi.hera.models.WalletConnectTransactionErrorResponse

const val LEGACY_ALGO_CHAIN_ID = 4160L
const val DEFAULT_CHAIN_ID = LEGACY_ALGO_CHAIN_ID

interface WalletConnectClient {

    fun connect(uri: String)
    fun connect(sessionId: Long, sessionMeta: WalletConnectSessionMeta, fallbackBrowserGroupResponse: String?)

    fun setListener(listener: WalletConnectClientListener)

    fun approveSession(id: Long, accountAddresses: List<String>, chainId: Long?)
    fun updateSession(id: Long, accountAddresses: List<String>?, chainId: Long?)
    fun rejectSession(id: Long)

    fun disconnectFromSession(id: Long)
    fun killSession(id: Long)

    fun rejectRequest(sessionId: Long, requestId: Long, errorResponse: WalletConnectTransactionErrorResponse)
    fun approveRequest(sessionId: Long, requestId: Long, payload: Any)

    fun getWalletConnectSession(sessionId: Long): WalletConnectSession?

    fun getSessionRetryCount(sessionId: Long): Int
    fun setSessionRetryCount(sessionId: Long, retryCount: Int)
    fun clearSessionRetryCount(sessionId: Long)
}
