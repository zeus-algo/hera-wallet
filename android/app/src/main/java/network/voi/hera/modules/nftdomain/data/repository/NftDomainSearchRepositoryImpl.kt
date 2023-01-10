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

package network.voi.hera.modules.nftdomain.data.repository

import network.voi.hera.models.Result
import network.voi.hera.modules.nftdomain.data.mapper.NftDomainSearchResultDTOMapper
import network.voi.hera.modules.nftdomain.domain.model.NftDomainSearchResultDTO
import network.voi.hera.modules.nftdomain.domain.repository.NftDomainSearchRepository
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.requestWithHipoErrorHandler
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler

class NftDomainSearchRepositoryImpl(
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val hipoErrorHandler: RetrofitErrorHandler,
    private val nftDomainSearchResultDTOMapper: NftDomainSearchResultDTOMapper
) : NftDomainSearchRepository {

    override suspend fun getSearchResults(query: String): Result<List<NftDomainSearchResultDTO>> {
        return requestWithHipoErrorHandler(hipoErrorHandler) {
            mobileAlgorandApi.getNftDomainAccountAddresses(query)
        }.map { nftDomainSearchResponse ->
            nftDomainSearchResponse.results?.map { nftDomainSearchResultResponse ->
                nftDomainSearchResultDTOMapper.mapToNftDomainSearchDTO(nftDomainSearchResultResponse)
            }.orEmpty()
        }
    }
}
