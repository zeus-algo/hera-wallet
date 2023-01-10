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

package network.voi.hera.modules.transaction.detail.data.repository

import network.voi.hera.models.Result
import network.voi.hera.modules.transaction.common.data.mapper.TransactionDTOMapper
import network.voi.hera.modules.transaction.common.domain.model.TransactionDTO
import network.voi.hera.modules.transaction.detail.data.cache.InnerTransactionLocalStackCache
import network.voi.hera.modules.transaction.detail.domain.model.BaseTransactionDetail
import network.voi.hera.modules.transaction.detail.domain.repository.TransactionDetailRepository
import network.voi.hera.network.IndexerApi
import network.voi.hera.network.request
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionDetailRepositoryImpl @Inject constructor(
    private val indexerApi: IndexerApi,
    private val transactionDTOMapper: TransactionDTOMapper,
    private val innerTransactionLocalStackCache: InnerTransactionLocalStackCache
) : TransactionDetailRepository {
    override fun fetchTransactionDetail(transactionId: String): Flow<Result<TransactionDTO>> = flow {
        request { indexerApi.getTransactionDetail(transactionId) }.use(
            onSuccess = {
                val transactionDetailDTO = transactionDTOMapper.mapToTransactionDTO(it.transaction)
                emit(Result.Success(transactionDetailDTO))
            },
            onFailed = { exception, _ ->
                emit(Result.Error(exception))
            }
        )
    }

    override suspend fun putInnerTransactionToStackCache(transactions: List<BaseTransactionDetail>) {
        innerTransactionLocalStackCache.put(transactions)
    }

    override suspend fun popInnerTransactionFromStackCache() {
        innerTransactionLocalStackCache.pop()
    }

    override suspend fun peekInnerTransactionFromStackCache(): List<BaseTransactionDetail>? {
        return innerTransactionLocalStackCache.peek()
    }

    override suspend fun clearInnerTransactionStackCache() {
        innerTransactionLocalStackCache.clear()
    }
}
