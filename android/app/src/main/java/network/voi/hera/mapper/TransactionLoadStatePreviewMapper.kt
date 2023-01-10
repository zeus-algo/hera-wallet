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

package network.voi.hera.mapper

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import network.voi.hera.decider.TransactionScreenStateViewTypeDecider
import network.voi.hera.decider.TransactionsScreenStateViewVisibilityDecider
import network.voi.hera.models.ui.TransactionLoadStatePreview
import javax.inject.Inject

class TransactionLoadStatePreviewMapper @Inject constructor(
    private val transactionsScreenStateVisibilityDecider: TransactionsScreenStateViewVisibilityDecider,
    private val transactionScreenStateViewTypeDecider: TransactionScreenStateViewTypeDecider,
) {

    fun mapToTransactionLoadStatePreview(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean
    ): TransactionLoadStatePreview {
        return TransactionLoadStatePreview(
            isTransactionListVisible = (combinedLoadStates.refresh is LoadState.Error).not() &&
                (isLastStateError && combinedLoadStates.refresh is Loading).not() &&
                itemCount != 0,
            isScreenStateViewVisible = transactionsScreenStateVisibilityDecider.decideScreenStateViewVisibility(
                combinedLoadStates,
                itemCount,
                isLastStateError
            ),
            screenStateViewType = transactionScreenStateViewTypeDecider.decideScreenStateViewType(
                combinedLoadStates,
                itemCount
            ),
            isLoading = (combinedLoadStates.refresh is Loading) || (combinedLoadStates.append is Loading),
        )
    }
}
