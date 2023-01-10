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

package network.voi.hera.modules.swap.assetselection.toasset.data.di

import network.voi.hera.modules.swap.assetselection.toasset.data.mapper.AvailableSwapAssetDTOMapper
import network.voi.hera.modules.swap.assetselection.toasset.data.repository.AvailableTargetSwapAssetsRepositoryImpl
import network.voi.hera.modules.swap.assetselection.toasset.domain.repository.AvailableTargetSwapAssetsRepository
import network.voi.hera.network.MobileAlgorandApi
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AvailableSwapAssetRepositoryModule {

    @Named(AvailableTargetSwapAssetsRepository.INJECTION_NAME)
    @Provides
    fun provideAvailableTargetSwapAssetsRepository(
        mobileAlgorandApi: MobileAlgorandApi,
        availableSwapAssetDTOMapper: AvailableSwapAssetDTOMapper,
        hipoErrorHandler: RetrofitErrorHandler
    ): AvailableTargetSwapAssetsRepository {
        return AvailableTargetSwapAssetsRepositoryImpl(
            mobileAlgorandApi,
            availableSwapAssetDTOMapper,
            hipoErrorHandler
        )
    }
}
