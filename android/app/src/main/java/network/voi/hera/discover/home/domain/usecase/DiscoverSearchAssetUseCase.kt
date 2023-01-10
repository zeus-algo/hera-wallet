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

package network.voi.hera.discover.home.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import network.voi.hera.assetsearch.domain.model.AssetSearchQuery
import network.voi.hera.assetsearch.domain.model.BaseSearchedAsset.DiscoverSearchedAsset
import network.voi.hera.assetsearch.domain.pagination.AssetSearchPagerBuilder
import network.voi.hera.assetsearch.domain.pagination.AssetSearchPagination
import network.voi.hera.assetsearch.domain.repository.AssetSearchRepository
import network.voi.hera.discover.home.domain.mapper.DiscoverSearchedAssetMapper
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiscoverSearchAssetUseCase @Inject constructor(
    @Named(AssetSearchRepository.REPOSITORY_INJECTION_NAME) private val assetSearchRepository: AssetSearchRepository,
    private val assetSearchPagination: AssetSearchPagination,
    private val discoverSearchedAssetMapper: DiscoverSearchedAssetMapper
) {

    fun createPaginationFlow(
        builder: AssetSearchPagerBuilder,
        scope: CoroutineScope,
        defaultQuery: AssetSearchQuery
    ): Flow<PagingData<DiscoverSearchedAsset>> {
        return assetSearchPagination
            .initPagination(builder, scope, assetSearchRepository, defaultQuery)
            .map { pagingData -> pagingData.map { discoverSearchedAssetMapper.mapToDiscoverSearchedAsset(it) } }
    }

    suspend fun searchAsset(query: AssetSearchQuery) {
        assetSearchPagination.searchAsset(query)
    }
}
