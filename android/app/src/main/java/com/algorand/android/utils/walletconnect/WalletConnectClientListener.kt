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

package com.algorand.android.utils.walletconnect

import com.algorand.android.models.WalletConnectSession
import org.walletconnect.Session

interface WalletConnectClientListener {
    fun onSessionRequest(sessionId: Long, requestId: Long, session: WalletConnectSession, chainId: Long?)
    fun onSessionUpdate(sessionId: Long, accounts: List<String>?, chainId: Long?)
    fun onCustomRequest(sessionId: Long, requestId: Long, payloadList: List<*>)
    fun onFailure(sessionId: Long, error: Session.Status.Error)
    fun onDisconnected(sessionId: Long)
    fun onSessionKilled(sessionId: Long)
    fun onSessionApproved(sessionId: Long, session: WalletConnectSession, clientId: String)
    fun onConnected(sessionId: Long, session: WalletConnectSession?, clientId: String)
}
