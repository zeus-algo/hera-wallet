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

package network.voi.hera.modules.transactionhistory.ui.usecase

import androidx.paging.PagingData
import androidx.paging.map
import network.voi.hera.decider.TransactionUserUseCase
import network.voi.hera.models.DateFilter
import network.voi.hera.modules.transactionhistory.domain.usecase.TransactionHistoryUseCase
import network.voi.hera.modules.transactionhistory.ui.mapper.TransactionItemMapper
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionHistoryPreviewUseCase @Inject constructor(
    private val transactionHistoryUseCase: TransactionHistoryUseCase,
    transactionItemMapper: TransactionItemMapper,
    transactionUserUseCase: TransactionUserUseCase,
    collectibleUseCase: SimpleCollectibleUseCase,
    simpleAssetDetailUseCase: SimpleAssetDetailUseCase
) : BaseTransactionPreviewUseCase(
    transactionItemMapper,
    transactionUserUseCase,
    collectibleUseCase,
    simpleAssetDetailUseCase
) {

    fun refreshTransactionHistory() {
        transactionHistoryUseCase.refreshTransactionHistory()
    }

    suspend fun filterHistoryByDate(dateFilter: DateFilter) {
        transactionHistoryUseCase.filterHistoryByDate(dateFilter)
    }

    fun getTransactionHistoryPaginationFlow(
        publicKey: String,
        coroutineScope: CoroutineScope,
        assetId: Long? = null,
        txnType: String? = null
    ): Flow<PagingData<BaseTransactionItem>>? {
        return transactionHistoryUseCase.getTransactionPaginationFlow(publicKey, assetId, coroutineScope, txnType)
            ?.map {
                it.map { transaction ->
                    createBaseTransactionItem(transaction, publicKey)
                }
            }
    }
}
