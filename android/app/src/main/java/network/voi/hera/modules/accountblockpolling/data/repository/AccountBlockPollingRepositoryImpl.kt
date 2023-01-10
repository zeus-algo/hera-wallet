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

package network.voi.hera.modules.accountblockpolling.data.repository

import network.voi.hera.modules.accountblockpolling.data.mapper.ShouldRefreshRequestBodyMapper
import network.voi.hera.models.Result
import network.voi.hera.modules.accountblockpolling.data.model.ShouldRefreshResponse
import network.voi.hera.modules.accountblockpolling.data.local.AccountBlockPollingSingleLocalCache
import network.voi.hera.modules.accountblockpolling.domain.repository.AccountBlockPollingRepository
import network.voi.hera.utils.CacheResult

class AccountBlockPollingRepositoryImpl(
    private val accountBlockPollingSingleLocalCache: AccountBlockPollingSingleLocalCache,
    private val shouldRefreshRequestBodyMapper: ShouldRefreshRequestBodyMapper,
) : AccountBlockPollingRepository {

    override fun clearLastKnownAccountBlockNumber() {
        accountBlockPollingSingleLocalCache.clear()
    }

    override fun updateLastKnownAccountBlockNumber(blockNumber: CacheResult<Long>) {
        accountBlockPollingSingleLocalCache.put(blockNumber)
    }

    override fun getLastKnownAccountBlockNumber(): CacheResult<Long>? {
        return accountBlockPollingSingleLocalCache.getOrNull()
    }

    override suspend fun getResultWhetherAccountsUpdateIsRequired(
        localAccountAddresses: List<String>,
        latestKnownRound: Long?
    ): Result<ShouldRefreshResponse> {
        val shouldRefreshAccountInformationRequestBody = shouldRefreshRequestBodyMapper.mapToShouldRefreshRequestBody(
            accountAddresses = localAccountAddresses,
            lastKnownRound = latestKnownRound
        )
//        return requestWithHipoErrorHandler(retrofitErrorHandler) {
//            mobileAlgorandApi.shouldRefresh(shouldRefreshAccountInformationRequestBody)
//        }
        return Result.Success(ShouldRefreshResponse(shouldRefresh = true))
    }
}
