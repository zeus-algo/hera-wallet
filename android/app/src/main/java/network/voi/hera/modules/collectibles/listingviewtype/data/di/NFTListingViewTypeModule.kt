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

package network.voi.hera.modules.collectibles.listingviewtype.data.di

import network.voi.hera.modules.collectibles.listingviewtype.data.local.NFTListingViewTypeLocalSource
import network.voi.hera.modules.collectibles.listingviewtype.data.repository.NFTListingViewTypeRepositoryImpl
import network.voi.hera.modules.collectibles.listingviewtype.domain.repository.NFTListingViewTypeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NFTListingViewTypeModule {

    @Provides
    @Named(NFTListingViewTypeRepository.INJECTION_NAME)
    fun provideNFTListingViewTypeRepository(
        nftListingViewTypeLocalSource: NFTListingViewTypeLocalSource
    ): NFTListingViewTypeRepository {
        return NFTListingViewTypeRepositoryImpl(nftListingViewTypeLocalSource = nftListingViewTypeLocalSource)
    }
}
