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

package network.voi.hera.modules.swap.assetswap.data.di

import network.voi.hera.modules.swap.confirmswap.data.mapper.CreateSwapQuoteTransactionsRequestBodyMapper
import network.voi.hera.modules.swap.assetswap.data.mapper.PeraFeeDTOMapper
import network.voi.hera.modules.swap.assetswap.data.mapper.PeraFeeRequestBodyMapper
import network.voi.hera.modules.swap.assetswap.data.mapper.SwapQuoteDTOMapper
import network.voi.hera.modules.swap.assetswap.data.mapper.SwapQuoteRequestBodyMapper
import network.voi.hera.modules.swap.confirmswap.data.mapper.SwapQuoteTransactionDTOMapper
import network.voi.hera.modules.swap.assetswap.data.mapper.decider.SwapQuoteProviderResponseDecider
import network.voi.hera.modules.swap.assetswap.data.repository.AssetSwapRepositoryImpl
import network.voi.hera.modules.swap.assetswap.domain.repository.AssetSwapRepository
import network.voi.hera.network.MobileAlgorandApi
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AssetSwapRepositoryModule {

    @Named(AssetSwapRepository.INJECTION_NAME)
    @Provides
    fun provideAssetSwapRepository(
        mobileAlgorandApi: MobileAlgorandApi,
        hipoErrorHandler: RetrofitErrorHandler,
        swapQuoteDTOMapper: SwapQuoteDTOMapper,
        swapQuoteRequestBodyMapper: SwapQuoteRequestBodyMapper,
        swapQuoteProviderResponseDecider: SwapQuoteProviderResponseDecider,
        peraFeeRequestBodyMapper: PeraFeeRequestBodyMapper,
        peraFeeDTOMapper: PeraFeeDTOMapper,
        swapQuoteTransactionDTOMapper: SwapQuoteTransactionDTOMapper,
        createSwapQuoteTransactionsRequestBodyMapper: CreateSwapQuoteTransactionsRequestBodyMapper
    ): AssetSwapRepository {
        return AssetSwapRepositoryImpl(
            mobileAlgorandApi = mobileAlgorandApi,
            hipoErrorHandler = hipoErrorHandler,
            swapQuoteDTOMapper = swapQuoteDTOMapper,
            swapQuoteRequestBodyMapper = swapQuoteRequestBodyMapper,
            swapQuoteProviderResponseDecider = swapQuoteProviderResponseDecider,
            peraFeeRequestBodyMapper = peraFeeRequestBodyMapper,
            peraFeeDTOMapper = peraFeeDTOMapper,
            swapQuoteTransactionDTOMapper = swapQuoteTransactionDTOMapper,
            createSwapQuoteTransactionsRequestBodyMapper = createSwapQuoteTransactionsRequestBodyMapper
        )
    }
}
