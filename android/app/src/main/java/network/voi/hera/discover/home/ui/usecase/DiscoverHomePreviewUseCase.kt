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

package network.voi.hera.discover.home.ui.usecase

import android.content.SharedPreferences
import androidx.paging.PagingData
import androidx.paging.map
import network.voi.hera.assetsearch.domain.mapper.AssetSearchQueryMapper
import network.voi.hera.assetsearch.domain.pagination.AssetSearchPagerBuilder
import network.voi.hera.discover.common.ui.model.WebViewError
import network.voi.hera.discover.home.domain.model.DappInfo
import network.voi.hera.discover.home.domain.model.TokenDetailInfo
import network.voi.hera.discover.home.domain.usecase.DiscoverSearchAssetUseCase
import network.voi.hera.discover.home.ui.mapper.DiscoverAssetItemMapper
import network.voi.hera.discover.home.ui.model.DiscoverAssetItem
import network.voi.hera.discover.home.ui.model.DiscoverHomePreview
import network.voi.hera.utils.Event
import network.voi.hera.utils.preference.getSavedThemePreference
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiscoverHomePreviewUseCase @Inject constructor(
    private val discoverSearchAssetUseCase: DiscoverSearchAssetUseCase,
    private val assetSearchQueryMapper: AssetSearchQueryMapper,
    private val discoverAssetItemMapper: DiscoverAssetItemMapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {

    fun getSearchPaginationFlow(
        searchPagerBuilder: AssetSearchPagerBuilder,
        scope: CoroutineScope,
        queryText: String
    ): Flow<PagingData<DiscoverAssetItem>> {
        val assetSearchQuery = assetSearchQueryMapper.mapToAssetSearchQuery(
            queryText = queryText,
            hasCollectibles = null,
            availableOnDiscoverMobile = true,
            defaultToTrending = true
        )
        val searchedAssetsFlow = discoverSearchAssetUseCase.createPaginationFlow(
            builder = searchPagerBuilder,
            scope = scope,
            defaultQuery = assetSearchQuery
        )

        return searchedAssetsFlow.map { baseSearchedAssetPagination ->
            baseSearchedAssetPagination.map { discoverSearchedAsset ->
                discoverAssetItemMapper.mapToDiscoverAssetItem(discoverSearchedAsset)
            }
        }
    }

    suspend fun searchAsset(queryText: String) {
        val assetSearchQuery = assetSearchQueryMapper.mapToAssetSearchQuery(
            queryText = queryText,
            hasCollectibles = null,
            availableOnDiscoverMobile = true,
            defaultToTrending = true
        )
        discoverSearchAssetUseCase.searchAsset(assetSearchQuery)
    }

    fun getInitialStatePreview() = DiscoverHomePreview(
        themePreference = sharedPreferences.getSavedThemePreference(),
        isLoading = true,
        tokenDetailScreenRequestEvent = null,
        dappViewerScreenRequestEvent = null,
        reloadPageEvent = Event(Unit)
    )

    fun updateSearchScreenLoadState(
        isListEmpty: Boolean,
        isCurrentStateError: Boolean,
        isLoading: Boolean,
        previousState: DiscoverHomePreview
    ) = previousState.copy(
        isListEmpty = isListEmpty &&
            !isCurrentStateError &&
            !isLoading &&
            previousState.isSearchActivated
    )

    fun requestSearchVisible(
        isVisible: Boolean,
        previousState: DiscoverHomePreview
    ) = previousState.copy(
        isListEmpty = if (isVisible) previousState.isListEmpty else false,
        isSearchActivated = isVisible
    )

    fun requestLoadHomepage(previousState: DiscoverHomePreview) = previousState.copy(
        isLoading = true,
        reloadPageEvent = Event(Unit)
    )

    fun onPageRequested(previousState: DiscoverHomePreview) = previousState.copy(
        isLoading = true
    )

    fun onPageFinished(previousState: DiscoverHomePreview) = previousState.copy(
        isLoading = false
    )

    fun onError(previousState: DiscoverHomePreview) = previousState.copy(
        isLoading = false,
        loadingErrorEvent = Event(WebViewError.NO_CONNECTION)
    )

    fun onHttpError(previousState: DiscoverHomePreview) = previousState.copy(
        isLoading = false,
        loadingErrorEvent = Event(WebViewError.HTTP_ERROR)
    )

    fun pushDappViewerScreen(
        data: String,
        previousState: DiscoverHomePreview
    ) = previousState.copy(
        dappViewerScreenRequestEvent = Event(
            gson.fromJson(data, DappInfo::class.java)
        )
    )

    fun pushTokenDetailScreen(
        data: String,
        previousState: DiscoverHomePreview
    ) = previousState.copy(
        tokenDetailScreenRequestEvent = Event(
            gson.fromJson(data, TokenDetailInfo::class.java)
        )
    )
}
