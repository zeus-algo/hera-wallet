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

package network.voi.hera.assetsearch.domain.pagination

import network.voi.hera.assetsearch.domain.model.AssetSearchDTO
import network.voi.hera.assetsearch.domain.model.AssetSearchQuery
import network.voi.hera.assetsearch.domain.repository.AssetSearchRepository
import network.voi.hera.models.Pagination
import network.voi.hera.models.Result
import network.voi.hera.utils.PeraPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetSearchDataSource(
    private val assetSearchRepository: AssetSearchRepository,
    private val currentQuery: AssetSearchQuery?
) : PeraPagingSource<String, AssetSearchDTO>() {

    override val logTag: String = AssetSearchDataSource::class.java.simpleName

    override suspend fun loadMore(loadUrl: String): LoadResult<String, AssetSearchDTO> {
        return withContext(Dispatchers.IO) {
            try {
                parseResult(getAssetsByUrl(loadUrl))
            } catch (exception: Exception) {
                LoadResult.Error<String, AssetSearchDTO>(exception)
            }
        }
    }

    override suspend fun initializeData(): LoadResult<String, AssetSearchDTO> {
        return parseResult(searchAssets())
    }

    private suspend fun getAssetsByUrl(currentUrlToFetch: String): Result<Pagination<AssetSearchDTO>> {
        return assetSearchRepository.getAssetsByUrl(currentUrlToFetch)
    }

    private suspend fun searchAssets(): Result<Pagination<AssetSearchDTO>> {
        val queryText = currentQuery?.queryText ?: DEFAULT_ASSET_QUERY.queryText
        val hasCollectibles = currentQuery?.hasCollectibles
        val availableOnDiscoverMobile = currentQuery?.availableOnDiscoverMobile
        return if (queryText.isBlank() && currentQuery?.defaultToTrending == true) {
            assetSearchRepository.getTrendingAssets()
        } else {
            assetSearchRepository.searchAsset(queryText, hasCollectibles, availableOnDiscoverMobile)
        }
    }

    private fun parseResult(result: Result<Pagination<AssetSearchDTO>>): LoadResult<String, AssetSearchDTO> {
        return when (result) {
            is Result.Success -> {
                LoadResult.Page(data = result.data.results, prevKey = null, nextKey = result.data.next)
            }
            is Result.Error -> LoadResult.Error<String, AssetSearchDTO>(result.exception)
        }
    }

    companion object {
        val DEFAULT_ASSET_QUERY = AssetSearchQuery.createDefaultQuery()
    }
}
