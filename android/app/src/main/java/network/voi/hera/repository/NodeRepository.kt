/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package network.voi.hera.Repository

import network.voi.hera.database.NodeDao
import network.voi.hera.models.Node
import javax.inject.Inject

class NodeRepository @Inject constructor(
    private val nodeDao: NodeDao
) {

    suspend fun setNodeListToDatabase(nodes: List<Node>) {
        nodeDao.updateNodes(nodes)
    }

    fun getAllNodesAsFlow() = nodeDao.getAllNodeAsFlow()

    suspend fun getAllNodes() = nodeDao.getAllNode()
}
