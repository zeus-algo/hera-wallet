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

package network.voi.hera.modules.transactionhistory.data.mapper

import network.voi.hera.modules.transaction.common.data.mapper.TransactionDTOMapper
import network.voi.hera.modules.transactionhistory.data.model.PaginatedTransactionsResponse
import network.voi.hera.modules.transactionhistory.domain.model.PaginatedTransactionsDTO
import javax.inject.Inject

class PaginatedTransactionsDTOMapper @Inject constructor(
    private val transactionMapper: TransactionDTOMapper
) {

    fun mapToPaginatedTransactionsDTO(
        paginatedTransactionsResponse: PaginatedTransactionsResponse
    ): PaginatedTransactionsDTO {
        return PaginatedTransactionsDTO(
            nextToken = paginatedTransactionsResponse.nextToken,
            transactionList = paginatedTransactionsResponse.transactionList.map {
                transactionMapper.mapToTransactionDTO(it)
            }
        )
    }
}
