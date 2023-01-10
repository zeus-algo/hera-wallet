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

package network.voi.hera.dependencyinjection

import network.voi.hera.core.AccountManager
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.usecase.AccountCacheStatusUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.AssetFetchAndCacheUseCase
import network.voi.hera.modules.accountblockpolling.domain.usecase.ClearLastKnownBlockForAccountsUseCase
import network.voi.hera.modules.accountblockpolling.domain.usecase.GetResultWhetherAccountsUpdateIsRequiredUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.modules.accountblockpolling.domain.usecase.UpdateLastKnownBlockUseCase
import network.voi.hera.utils.AccountDetailUpdateHelper
import network.voi.hera.utils.coremanager.AccountDetailCacheManager
import network.voi.hera.utils.coremanager.AssetCacheManager
import network.voi.hera.utils.coremanager.ParityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

    @Singleton
    @Provides
    fun provideAlgoPriceManager(
        parityUseCase: ParityUseCase,
        currencyUseCase: CurrencyUseCase
    ): ParityManager {
        return ParityManager(parityUseCase, currencyUseCase)
    }

    @Singleton
    @Provides
    fun provideAccountDetailCacheManager(
        getResultWhetherAccountsUpdateIsRequiredUseCase: GetResultWhetherAccountsUpdateIsRequiredUseCase,
        updateLastKnownBlockUseCase: UpdateLastKnownBlockUseCase,
        clearLastKnownBlockForAccountsUseCase: ClearLastKnownBlockForAccountsUseCase,
        accountDetailUseCase: AccountDetailUseCase,
        accountManager: AccountManager,
        accountDetailUpdateHelper: AccountDetailUpdateHelper
    ): AccountDetailCacheManager {
        return AccountDetailCacheManager(
            getResultWhetherAccountsUpdateIsRequiredUseCase,
            updateLastKnownBlockUseCase,
            clearLastKnownBlockForAccountsUseCase,
            accountDetailUseCase,
            accountManager,
            accountDetailUpdateHelper
        )
    }

    @Singleton
    @Provides
    fun provideAssetCacheManager(
        accountCacheStatusUseCase: AccountCacheStatusUseCase,
        simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
        accountDetailUseCase: AccountDetailUseCase,
        assetFetchAndCacheUseCase: AssetFetchAndCacheUseCase
    ): AssetCacheManager {
        return AssetCacheManager(
            accountCacheStatusUseCase,
            simpleAssetDetailUseCase,
            accountDetailUseCase,
            assetFetchAndCacheUseCase
        )
    }
}
