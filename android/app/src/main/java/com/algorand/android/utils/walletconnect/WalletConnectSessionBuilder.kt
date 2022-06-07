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

import com.algorand.android.models.WalletConnectSessionMeta
import com.algorand.android.utils.HTTPS_PROTOCOL
import com.algorand.android.utils.HTTP_PROTOCOL
import com.algorand.android.utils.walletconnect.peermeta.WalletConnectPeerMetaBuilder
import com.algorand.android.utils.walletconnect.peermeta.WalletConnectPeraPeerMeta
import com.squareup.moshi.Moshi
import javax.inject.Inject
import okhttp3.OkHttpClient
import org.walletconnect.Session.Config
import org.walletconnect.impls.FileWCSessionStore
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import org.walletconnect.impls.WCSession

class WalletConnectSessionBuilder @Inject constructor(
    private val moshi: Moshi,
    private val okHttpClient: OkHttpClient,
    private val storage: FileWCSessionStore,
    private val walletConnectMapper: WCWalletConnectMapper
) {

    fun createSession(url: String): WalletConnectSessionCachedData? {
        val sessionConfig = createSessionConfigFromUrl(url) ?: return null
        return createWCSession(sessionConfig)
    }

    fun createSession(sessionId: Long, sessionMeta: WalletConnectSessionMeta): WalletConnectSessionCachedData? {
        val sessionConfig = walletConnectMapper.createSessionConfig(sessionMeta)
        return createWCSession(sessionConfig, sessionId)
    }

    private fun createWCSession(sessionConfig: Config, sessionId: Long? = null): WalletConnectSessionCachedData? {
        val fullyQualifiedConfig = createFullyQualifiedSessionConfig(sessionConfig) ?: return null

        if (checkIfWalletConnectConfigHasInvalidBridgeProtocol(fullyQualifiedConfig.bridge)) return null

        val session = WCSession(
            config = fullyQualifiedConfig,
            payloadAdapter = MoshiPayloadAdapter(moshi),
            sessionStore = storage,
            transportBuilder = OkHttpTransport.Builder(okHttpClient, moshi),
            clientMeta = WalletConnectPeerMetaBuilder.createPeerMeta(WalletConnectPeraPeerMeta)
        ).apply {
            init()
        }

        return WalletConnectSessionCachedData.create(session, sessionConfig, sessionId)
    }

    private fun checkIfWalletConnectConfigHasInvalidBridgeProtocol(bridge: String): Boolean {
        return !bridge.contains(HTTPS_PROTOCOL, true) && !bridge.contains(HTTP_PROTOCOL, true)
    }
}
