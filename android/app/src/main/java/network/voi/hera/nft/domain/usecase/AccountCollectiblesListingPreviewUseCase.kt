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

package network.voi.hera.nft.domain.usecase

import network.voi.hera.models.Account
import network.voi.hera.models.AccountDetail
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.modules.collectibles.filter.domain.usecase.ClearCollectibleFiltersPreferencesUseCase
import network.voi.hera.modules.collectibles.filter.domain.usecase.ShouldDisplayOptedInNFTPreferenceUseCase
import network.voi.hera.modules.collectibles.listingviewtype.domain.model.NFTListingViewType
import network.voi.hera.modules.collectibles.listingviewtype.domain.usecase.AddOnListingViewTypeChangeListenerUseCase
import network.voi.hera.modules.collectibles.listingviewtype.domain.usecase.GetNFTListingViewTypePreferenceUseCase
import network.voi.hera.modules.collectibles.listingviewtype.domain.usecase.RemoveOnListingViewTypeChangeListenerUseCase
import network.voi.hera.modules.collectibles.listingviewtype.domain.usecase.SaveNFTListingViewTypePreferenceUseCase
import network.voi.hera.modules.sorting.nftsorting.ui.usecase.CollectibleItemSortUseCase
import network.voi.hera.nft.mapper.CollectibleListingItemMapper
import network.voi.hera.nft.ui.model.BaseCollectibleListData
import network.voi.hera.nft.ui.model.BaseCollectibleListItem
import network.voi.hera.nft.ui.model.CollectiblesListingPreview
import network.voi.hera.nft.utils.CollectibleUtils
import network.voi.hera.Repository.FailedAssetRepository
import network.voi.hera.usecase.AccountCollectibleDataUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.CacheResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@SuppressWarnings("LongParameterList")
class AccountCollectiblesListingPreviewUseCase @Inject constructor(
    private val collectibleListingItemMapper: CollectibleListingItemMapper,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val failedAssetRepository: FailedAssetRepository,
    private val accountCollectibleDataUseCase: AccountCollectibleDataUseCase,
    private val collectibleItemSortUseCase: CollectibleItemSortUseCase,
    private val getNFTListingViewTypePreferenceUseCase: GetNFTListingViewTypePreferenceUseCase,
    clearCollectibleFiltersPreferencesUseCase: ClearCollectibleFiltersPreferencesUseCase,
    shouldDisplayOptedInNFTPreferenceUseCase: ShouldDisplayOptedInNFTPreferenceUseCase,
    collectibleUtils: CollectibleUtils,
    addOnListingViewTypeChangeListenerUseCase: AddOnListingViewTypeChangeListenerUseCase,
    removeOnListingViewTypeChangeListenerUseCase: RemoveOnListingViewTypeChangeListenerUseCase,
    saveNFTListingViewTypePreferenceUseCase: SaveNFTListingViewTypePreferenceUseCase
) : BaseCollectiblesListingPreviewUseCase(
    collectibleListingItemMapper,
    saveNFTListingViewTypePreferenceUseCase,
    addOnListingViewTypeChangeListenerUseCase,
    removeOnListingViewTypeChangeListenerUseCase,
    shouldDisplayOptedInNFTPreferenceUseCase,
    collectibleUtils,
    clearCollectibleFiltersPreferencesUseCase
) {

    fun getCollectiblesListingPreviewFlow(searchKeyword: String, publicKey: String): Flow<CollectiblesListingPreview> {
        return combine(
            accountDetailUseCase.getAccountDetailCacheFlow(publicKey),
            failedAssetRepository.getFailedAssetCacheFlow(),
            accountCollectibleDataUseCase.getAccountAllCollectibleDataFlow(publicKey)
        ) { accountDetail, failedAssets, accountCollectibleData ->
            val canAccountSignTransaction = canAccountSignTransaction(accountDetail)
            val nftListingType = getNFTListingViewTypePreferenceUseCase()
            val collectibleListData = prepareCollectiblesListItems(
                searchKeyword = searchKeyword,
                cachedAccountDetail = accountDetail,
                accountCollectibleData = accountCollectibleData,
                nftListingType = nftListingType
            )
            val isAllCollectiblesFilteredOut = isAllCollectiblesFilteredOut(collectibleListData, searchKeyword)
            val isEmptyStateVisible = accountCollectibleData.isEmpty() || isAllCollectiblesFilteredOut
            val itemList = mutableListOf<BaseCollectibleListItem>().apply {
                if (!isEmptyStateVisible) {
                    add(createSearchViewItem(query = searchKeyword, nftListingType = nftListingType))
                    add(
                        ACCOUNT_COLLECTIBLES_LIST_CONFIGURATION_HEADER_ITEM_INDEX,
                        createInfoViewItem(
                            displayedCollectibleCount = collectibleListData.displayedCollectibleCount,
                            isAddButtonVisible = canAccountSignTransaction
                        )
                    )
                }
                addAll(collectibleListData.baseCollectibleItemList)
            }
            collectibleListingItemMapper.mapToPreviewItem(
                isLoadingVisible = false,
                isEmptyStateVisible = isEmptyStateVisible,
                isErrorVisible = failedAssets.isNotEmpty(),
                itemList = itemList,
                isReceiveButtonVisible = isEmptyStateVisible && canAccountSignTransaction,
                filteredCollectibleCount = collectibleListData.filteredOutCollectibleCount,
                isClearFilterButtonVisible = isAllCollectiblesFilteredOut,
                isAccountFabVisible = canAccountSignTransaction,
                isAddCollectibleFloatingActionButtonVisible = canAccountSignTransaction
            )
        }
    }

    private suspend fun prepareCollectiblesListItems(
        searchKeyword: String,
        cachedAccountDetail: CacheResult<AccountDetail>?,
        accountCollectibleData: List<BaseAccountAssetData>,
        nftListingType: NFTListingViewType
    ): BaseCollectibleListData {
        val accountDetail = cachedAccountDetail?.data
        var displayedCollectibleCount = 0
        var filteredOutCollectibleCount = 0
        val collectibleList = mutableListOf<BaseCollectibleListItem>().apply {
            accountCollectibleData.filter { nftData ->
                filterOptedInNFTIfNeed(accountDetail, nftData).also { isNotFiltered ->
                    if (!isNotFiltered) filteredOutCollectibleCount++
                }
            }.forEach { collectibleData ->
                filteredOutCollectibleCount++
                if (filterNFTBaseOnSearch(searchKeyword, collectibleData)) return@forEach
                val collectibleListItem = createCollectibleListItem(
                    accountAssetData = collectibleData,
                    optedInAccountAddress = accountDetail?.account?.address.orEmpty(),
                    nftListingType = nftListingType,
                    isOwnedByWatchAccount = accountDetail?.account?.type == Account.Type.WATCH
                ) ?: return@forEach
                add(collectibleListItem)
                displayedCollectibleCount++
            }
        }
        val sortedCollectibleItemList = collectibleItemSortUseCase.sortCollectibles(collectibleList)
        return collectibleListingItemMapper.mapToBaseCollectibleListData(
            sortedCollectibleItemList,
            displayedCollectibleCount,
            filteredOutCollectibleCount
        )
    }

    private fun canAccountSignTransaction(accountDetail: CacheResult<AccountDetail>?): Boolean {
        val publicKey = accountDetail?.data?.account?.address ?: return false
        return accountDetailUseCase.canAccountSignTransaction(publicKey)
    }

    companion object {
        const val ACCOUNT_COLLECTIBLES_LIST_CONFIGURATION_HEADER_ITEM_INDEX = 0
    }
}
