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

package network.voi.hera.modules.collectibles.filter.data.di

import network.voi.hera.modules.collectibles.filter.data.local.CollectibleFilterNotOwnedLocalSource
import network.voi.hera.modules.collectibles.filter.data.local.NFTFilterDisplayWatchAccountNFTsLocalSource
import network.voi.hera.modules.collectibles.filter.data.repository.CollectibleFiltersRepositoryImpl
import network.voi.hera.modules.collectibles.filter.domain.repository.CollectibleFiltersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CollectibleFilterRepositoryModule {

    @Provides
    @Singleton
    @Named(CollectibleFiltersRepository.COLLECTIBLE_FILTERS_REPOSITORY_INJECTION_NAME)
    internal fun provideCollectibleFiltersRepository(
        collectibleFilterNotOwnedLocalSource: CollectibleFilterNotOwnedLocalSource,
        nftFilterDisplayWatchAccountNFTsLocalSource: NFTFilterDisplayWatchAccountNFTsLocalSource
    ): CollectibleFiltersRepository {
        return CollectibleFiltersRepositoryImpl(
            collectibleFilterNotOwnedLocalSource = collectibleFilterNotOwnedLocalSource,
            nftFilterDisplayWatchAccountNFTsLocalSource = nftFilterDisplayWatchAccountNFTsLocalSource
        )
    }
}
