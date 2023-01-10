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

import network.voi.hera.R
import network.voi.hera.decider.TransactionUserUseCase
import network.voi.hera.modules.transactionhistory.domain.usecase.PendingTransactionsUseCase
import network.voi.hera.modules.transactionhistory.ui.mapper.TransactionItemMapper
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import javax.inject.Inject

class PendingTransactionsPreviewUseCase @Inject constructor(
    private val pendingTransactionsUseCase: PendingTransactionsUseCase,
    private val transactionUserUseCase: TransactionUserUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val collectibleUseCase: SimpleCollectibleUseCase,
    private val transactionItemMapper: TransactionItemMapper
) : BaseTransactionPreviewUseCase(
    transactionItemMapper,
    transactionUserUseCase,
    collectibleUseCase,
    simpleAssetDetailUseCase
) {

    val pendingFlowDistinctUntilChangedListener: (
        oldTransactions: List<BaseTransactionItem>?,
        newTransactions: List<BaseTransactionItem>?
    ) -> Boolean = { oldTransactions, newTransactions ->
        newTransactions?.filterIsInstance<BaseTransactionItem.TransactionItem>()?.any { new ->
            oldTransactions?.filterIsInstance<BaseTransactionItem.TransactionItem>()?.any { old ->
                old.isSameTransaction(new)
            } == true
        } == true
    }

    suspend fun getPendingTransactionItems(
        publicKey: String,
        assetId: Long? = null
    ): List<BaseTransactionItem> {
        val transactionItems = mutableListOf<BaseTransactionItem>()
        val pendingTransactions = pendingTransactionsUseCase.fetchPendingTransactions(publicKey, assetId)
        pendingTransactions.use(
            onSuccess = { pendingList ->
                pendingList.map { transaction ->
                    createBaseTransactionItem(transaction, publicKey)
                }.let {
                    if (it.isNotEmpty()) {
                        transactionItems.add(createPendingTransactionTitleItem())
                        transactionItems.addAll(it)
                    }
                }
            }
        )
        return transactionItems
    }

    private fun createPendingTransactionTitleItem(): BaseTransactionItem.ResourceTitleItem {
        return BaseTransactionItem.ResourceTitleItem(R.string.pending_transactions)
    }
}
