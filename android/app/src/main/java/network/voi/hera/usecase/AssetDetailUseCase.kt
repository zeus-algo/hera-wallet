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

package network.voi.hera.usecase

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import network.voi.hera.core.BaseUseCase
import network.voi.hera.decider.DateFilterUseCase
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.DateFilter
import network.voi.hera.models.ui.DateFilterPreview
import network.voi.hera.models.ui.TransactionLoadStatePreview
import network.voi.hera.modules.transaction.common.domain.model.TransactionTypeDTO
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem
import network.voi.hera.modules.transactionhistory.ui.usecase.PendingTransactionsPreviewUseCase
import network.voi.hera.modules.transactionhistory.ui.usecase.TransactionHistoryPreviewUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@SuppressWarnings("LongParameterList")
class AssetDetailUseCase @Inject constructor(
    private val transactionHistoryPreviewUseCase: TransactionHistoryPreviewUseCase,
    private val pendingTransactionsPreviewUseCase: PendingTransactionsPreviewUseCase,
    private val dateFilterUseCase: DateFilterUseCase,
    private val transactionLoadStateUseCase: TransactionLoadStateUseCase,
    private val accountTotalBalanceUseCase: AccountTotalBalanceUseCase
) : BaseUseCase() {

    val pendingTransactionDistinctUntilChangedListener
        get() = pendingTransactionsPreviewUseCase.pendingFlowDistinctUntilChangedListener

    fun getAccountBalanceFlow(publicKey: String) = accountTotalBalanceUseCase.getAccountBalanceFlow(publicKey)

    fun getTransactionFlow(
        publicKey: String,
        assetIdFilter: Long,
        cacheInScope: CoroutineScope
    ): Flow<PagingData<BaseTransactionItem>>? {
        return transactionHistoryPreviewUseCase.getTransactionHistoryPaginationFlow(
            publicKey,
            cacheInScope,
            assetIdFilter,
            TransactionTypeDTO.PAY_TRANSACTION.takeIf { assetIdFilter == ALGO_ID }?.value
        )
    }

    suspend fun setDateFilter(dateFilter: DateFilter) {
        transactionHistoryPreviewUseCase.filterHistoryByDate(dateFilter)
    }

    fun createDateFilterPreview(dateFilter: DateFilter): DateFilterPreview {
        return dateFilterUseCase.createDateFilterPreview(dateFilter)
    }

    fun createTransactionLoadStatePreview(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean
    ): TransactionLoadStatePreview {
        return transactionLoadStateUseCase.createTransactionLoadStatePreview(
            combinedLoadStates = combinedLoadStates,
            itemCount = itemCount,
            isLastStateError = isLastStateError
        )
    }

    fun refreshTransactionHistory() {
        transactionHistoryPreviewUseCase.refreshTransactionHistory()
    }

    suspend fun fetchPendingTransactions(publicKey: String, assetId: Long): List<BaseTransactionItem> {
        return pendingTransactionsPreviewUseCase.getPendingTransactionItems(publicKey, assetId)
    }
}
