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

package network.voi.hera.usecase

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import network.voi.hera.mapper.AssetAdditionLoadStatePreviewMapper
import network.voi.hera.mapper.AssetHoldingsMapper
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.ui.AssetAdditionLoadStatePreview
import network.voi.hera.modules.assets.addition.ui.model.AssetAdditionType
import network.voi.hera.utils.CacheResult
import javax.inject.Inject

class AssetAdditionUseCase @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetHoldingsMapper: AssetHoldingsMapper,
    private val assetAdditionLoadStatePreviewMapper: AssetAdditionLoadStatePreviewMapper
) {

    suspend fun addAssetAdditionToAccountCache(publicKey: String, assetInformation: AssetInformation) {
        val cachedAccount = accountDetailUseCase.getCachedAccountDetail(publicKey)?.data ?: return
        val pendingAssetHolding = assetHoldingsMapper.mapToPendingAdditionAssetHoldings(assetInformation)
        cachedAccount.accountInformation.addPendingAssetHolding(pendingAssetHolding)
        accountDetailUseCase.cacheAccountDetail(CacheResult.Success.create(cachedAccount))
    }

    fun createAssetAdditionLoadStatePreview(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean,
        assetAdditionType: AssetAdditionType
    ): AssetAdditionLoadStatePreview {
        val actualAssetItemCount = if (assetAdditionType == AssetAdditionType.ASSET) {
            itemCount
        } else {
            itemCount - NFT_RECEIVE_CONSTANT_ITEM_COUNT
        }
        val isLoading = with(combinedLoadStates) { refresh is LoadState.Loading || append is LoadState.Loading }
        val isFailed = combinedLoadStates.refresh is LoadState.Error

        val isAssetListVisible = (isLoading && isLastStateError) || (itemCount > 0 && !isFailed)

        return assetAdditionLoadStatePreviewMapper.mapToAssetAdditionLoadStatePreview(
            combinedLoadStates = combinedLoadStates,
            itemCount = actualAssetItemCount,
            assetAdditionType = assetAdditionType,
            isAssetListVisible = isAssetListVisible,
            isLoading = isLoading
        )
    }

    companion object {
        private const val NFT_RECEIVE_CONSTANT_ITEM_COUNT = 2
    }
}
