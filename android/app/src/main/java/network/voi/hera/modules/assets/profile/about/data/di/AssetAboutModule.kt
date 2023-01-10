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

package network.voi.hera.modules.assets.profile.about.data.di

import network.voi.hera.mapper.AssetDetailMapper
import network.voi.hera.modules.assets.profile.about.data.local.AsaProfileDetailSingleLocalCache
import network.voi.hera.modules.assets.profile.about.data.repository.AssetAboutRepositoryImpl
import network.voi.hera.modules.assets.profile.about.domain.repository.AssetAboutRepository
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.nft.domain.mapper.SimpleCollectibleDetailMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AssetAboutModule {

    @Provides
    @Named(AssetAboutRepository.INJECTION_NAME)
    fun provideAssetAboutRepository(
        mobileAlgorandApi: MobileAlgorandApi,
        assetDetailMapper: AssetDetailMapper,
        simpleCollectibleDetailMapper: SimpleCollectibleDetailMapper,
        asaProfileDetailSingleLocalCache: AsaProfileDetailSingleLocalCache
    ): AssetAboutRepository {
        return AssetAboutRepositoryImpl(
            mobileAlgorandApi = mobileAlgorandApi,
            assetDetailMapper = assetDetailMapper,
            simpleCollectibleDetailMapper = simpleCollectibleDetailMapper,
            asaProfileDetailSingleLocalCache = asaProfileDetailSingleLocalCache
        )
    }
}
