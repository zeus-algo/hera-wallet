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

package network.voi.hera.Repository

import network.voi.hera.cache.BlockPollingSingleLocalCache
import network.voi.hera.models.NextBlockResponse
import network.voi.hera.models.Result
import network.voi.hera.network.AlgodApi
import network.voi.hera.network.safeApiCall
import network.voi.hera.utils.CacheResult
import javax.inject.Inject

class BlockRepository @Inject constructor(
    private val algodApi: AlgodApi,
    private val blockPollingLocalCache: BlockPollingSingleLocalCache
) {

    fun cacheBlockNumber(blockNumber: CacheResult<Long>) {
        blockPollingLocalCache.put(blockNumber)
    }

    fun getBlockPollingCacheFlow() = blockPollingLocalCache.cacheFlow

    fun getLatestCachedBlockNumber() = blockPollingLocalCache.getOrNull()

    fun clearBlockCache() {
        blockPollingLocalCache.clear()
    }

    suspend fun getWaitForBlock(waitedBlockNumber: Long): Result<NextBlockResponse> = safeApiCall {
        with(algodApi.getWaitForBlock(waitedBlockNumber)) {
            if (isSuccessful && body() != null) {
                Result.Success(body() as NextBlockResponse)
            } else {
                Result.Error(Exception())
            }
        }
    }
}
