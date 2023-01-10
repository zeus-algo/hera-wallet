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

package network.voi.hera.modules.parity.data.repository

import network.voi.hera.cache.SelectedCurrencyDetailSingleLocalCache
import network.voi.hera.models.Result
import network.voi.hera.modules.parity.data.mapper.CurrencyDetailDTOMapper
import network.voi.hera.modules.parity.domain.model.CurrencyDetailDTO
import network.voi.hera.modules.parity.domain.model.SelectedCurrencyDetail
import network.voi.hera.modules.parity.domain.repository.ParityRepository
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.requestWithHipoErrorHandler
import network.voi.hera.utils.CacheResult
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import kotlinx.coroutines.flow.StateFlow

class ParityRepositoryImpl(
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val hipoApiErrorHandler: RetrofitErrorHandler,
    private val selectedCurrencyDetailSingleLocalCache: SelectedCurrencyDetailSingleLocalCache,
    private val currencyDetailDTOMapper: CurrencyDetailDTOMapper
) : ParityRepository {

    override fun cacheSelectedCurrencyDetail(
        selectedCurrencyDetail: CacheResult<SelectedCurrencyDetail>
    ) {
        selectedCurrencyDetailSingleLocalCache.put(selectedCurrencyDetail)
    }

    override fun clearSelectedCurrencyDetailCache() {
        selectedCurrencyDetailSingleLocalCache.clear()
    }

    override fun getCachedSelectedCurrencyDetail(): CacheResult<SelectedCurrencyDetail>? {
        return selectedCurrencyDetailSingleLocalCache.getOrNull()
    }

    override fun getSelectedCurrencyDetailCacheFlow(): StateFlow<CacheResult<SelectedCurrencyDetail>?> {
        return selectedCurrencyDetailSingleLocalCache.cacheFlow
    }

    override suspend fun fetchCurrencyDetailDTO(
        currencyPreference: String
    ): Result<CurrencyDetailDTO> {
        return requestWithHipoErrorHandler(hipoApiErrorHandler) {
            mobileAlgorandApi.getCurrencyDetail(currencyPreference)
        }.map { response ->
            currencyDetailDTOMapper.mapToCurrencyDetailDTO(response)
        }
    }
}
