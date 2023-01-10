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

package network.voi.hera.modules.assets.addition.base.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import network.voi.hera.assetsearch.domain.pagination.AssetSearchPagerBuilder
import network.voi.hera.assetsearch.ui.model.BaseAssetSearchListItem
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.ui.AssetAdditionLoadStatePreview
import network.voi.hera.modules.assets.addition.base.ui.domain.BaseAddAssetPreviewUseCase
import network.voi.hera.modules.assets.addition.ui.model.AssetAdditionType
import kotlinx.coroutines.flow.Flow

abstract class BaseAddAssetViewModel(
    private val baseAddAssetPreviewUseCase: BaseAddAssetPreviewUseCase
) : BaseViewModel() {

    protected abstract val searchPaginationFlow: Flow<PagingData<BaseAssetSearchListItem>>

    protected val assetSearchPagerBuilder = AssetSearchPagerBuilder.create()

    val assetSearchPaginationFlow
        get() = searchPaginationFlow

    fun createAssetAdditionLoadStatePreview(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean,
        assetAdditionType: AssetAdditionType
    ): AssetAdditionLoadStatePreview {
        return baseAddAssetPreviewUseCase.createAssetAdditionLoadStatePreview(
            combinedLoadStates = combinedLoadStates,
            itemCount = itemCount,
            isLastStateError = isLastStateError,
            assetAdditionType = assetAdditionType
        )
    }

    companion object {
        const val SEARCH_RESULT_LIMIT = 50
    }
}
