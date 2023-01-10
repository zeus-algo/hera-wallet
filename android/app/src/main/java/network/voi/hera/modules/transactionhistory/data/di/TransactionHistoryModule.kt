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

package network.voi.hera.modules.transactionhistory.data.di

import network.voi.hera.modules.transactionhistory.data.mapper.PaginatedTransactionsDTOMapper
import network.voi.hera.modules.transactionhistory.data.mapper.PendingTransactionDTOMapper
import network.voi.hera.modules.transactionhistory.data.repository.PendingTransactionsRepositoryImpl
import network.voi.hera.modules.transactionhistory.data.repository.TransactionHistoryRepositoryImpl
import network.voi.hera.modules.transactionhistory.domain.repository.PendingTransactionsRepository
import network.voi.hera.modules.transactionhistory.domain.repository.TransactionHistoryRepository
import network.voi.hera.network.AlgodApi
import network.voi.hera.network.IndexerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TransactionHistoryModule {

    @Provides
    @Named(TransactionHistoryRepository.INJECTION_NAME)
    fun provideTransactionHistoryRepository(
        indexerApi: IndexerApi,
        paginatedTransactionsMapper: PaginatedTransactionsDTOMapper
    ): TransactionHistoryRepository {
        return TransactionHistoryRepositoryImpl(indexerApi, paginatedTransactionsMapper)
    }

    @Provides
    @Named(PendingTransactionsRepository.INJECTION_NAME)
    fun providePendingTransactionsRepository(
        algodApi: AlgodApi,
        pendingTransactionMapper: PendingTransactionDTOMapper
    ): PendingTransactionsRepository {
        return PendingTransactionsRepositoryImpl(algodApi, pendingTransactionMapper)
    }
}
