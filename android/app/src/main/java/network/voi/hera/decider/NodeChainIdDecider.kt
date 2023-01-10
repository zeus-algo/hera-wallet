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

package network.voi.hera.decider

import network.voi.hera.models.Node
import network.voi.hera.utils.BETANET_CHAIN_ID
import network.voi.hera.utils.BETANET_NETWORK_SLUG
import network.voi.hera.utils.MAINNET_CHAIN_ID
import network.voi.hera.utils.MAINNET_NETWORK_SLUG
import network.voi.hera.utils.TESTNET_CHAIN_ID
import network.voi.hera.utils.TESTNET_NETWORK_SLUG
import network.voi.hera.utils.walletconnect.DEFAULT_CHAIN_ID
import javax.inject.Inject

class NodeChainIdDecider @Inject constructor() {

    fun decideNodeChainId(node: Node): Long {
        return when (node.networkSlug) {
            MAINNET_NETWORK_SLUG -> MAINNET_CHAIN_ID
            TESTNET_NETWORK_SLUG -> TESTNET_CHAIN_ID
            BETANET_NETWORK_SLUG -> BETANET_CHAIN_ID
            else -> DEFAULT_CHAIN_ID
        }
    }
}
