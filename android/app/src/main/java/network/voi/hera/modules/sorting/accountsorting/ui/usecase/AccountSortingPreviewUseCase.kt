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

package network.voi.hera.modules.sorting.accountsorting.ui.usecase

import network.voi.hera.R
import network.voi.hera.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.ButtonConfiguration
import network.voi.hera.modules.accounts.domain.usecase.GetAccountValueUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.mapper.BaseSortingListItemMapper
import network.voi.hera.modules.sorting.accountsorting.domain.mapper.SortingPreviewMapper
import network.voi.hera.modules.sorting.accountsorting.domain.model.AccountSortingPreview
import network.voi.hera.modules.sorting.accountsorting.domain.model.AccountSortingType
import network.voi.hera.modules.sorting.accountsorting.domain.model.BaseAccountSortingListItem
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import network.voi.hera.modules.sorting.utils.SortingTypeCreator
import network.voi.hera.usecase.GetSortedLocalAccountsUseCase
import network.voi.hera.usecase.SaveLocalAccountsUseCase
import javax.inject.Inject

// TODO: 8.08.2022 Move account sorting feature to UI layer
open class AccountSortingPreviewUseCase @Inject constructor(
    private val baseSortingListItemMapper: BaseSortingListItemMapper,
    private val sortingTypeCreator: SortingTypeCreator,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val sortingPreviewMapper: SortingPreviewMapper,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase,
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val getAccountValueUseCase: GetAccountValueUseCase,
    private val getSortedLocalAccountsUseCase: GetSortedLocalAccountsUseCase,
    private val saveLocalAccountsUseCase: SaveLocalAccountsUseCase,
) {

    fun getInitialSortingPreview(): AccountSortingPreview {
        return sortingPreviewMapper.mapToInitialAccountSortingPreview()
    }

    suspend fun getAccountSortingPreference(): AccountSortingType {
        return accountSortPreferenceUseCase.getAccountSortPreference()
    }

    fun saveManuallySortedAccountList(
        baseAccountSortingList: List<BaseAccountSortingListItem>,
        sortingPreferences: AccountSortingType.ManuallySort
    ) {
        val sortedAccountItems = baseAccountSortingList
            .filterIsInstance<BaseAccountSortingListItem.AccountSortListItem>()
        val localAccounts = getSortedLocalAccountsUseCase.getSortedLocalAccounts()
        val sortedAccounts = sortingPreferences.manualSort(
            currentList = sortedAccountItems.map { it.accountListItem },
            accounts = localAccounts
        )
        saveLocalAccountsUseCase.saveLocalAccounts(sortedAccounts)
    }

    suspend fun saveSortingPreferences(sortingPreferences: AccountSortingType) {
        val selectedSortingPreferencesTypeIdentifier = sortingPreferences.typeIdentifier
        accountSortPreferenceUseCase.saveAccountSortPreferences(selectedSortingPreferencesTypeIdentifier)
    }

    fun swapItemsAndUpdateList(
        currentPreview: AccountSortingPreview,
        fromPosition: Int,
        toPosition: Int,
        sortingPreferences: AccountSortingType
    ): AccountSortingPreview {
        val currentItemList = currentPreview.accountSortingListItems.toMutableList()
        val fromItem = currentItemList.removeAt(fromPosition)
        currentItemList.add(toPosition, fromItem)
        val sortTypeListItems = getSortTypeListItems(sortingPreferences)

        return sortingPreviewMapper.mapToAccountSortingPreview(
            sortTypeListItems = sortTypeListItems,
            accountSortingListItems = currentItemList
        )
    }

    fun createSortingPreview(sortingPreferences: AccountSortingType): AccountSortingPreview {
        val sortTypeListItems = getSortTypeListItems(sortingPreferences)
        val accountSortingListItems = getAccountSortingListItems(sortingPreferences)
        val isAccountListVisible = sortingPreferences == AccountSortingType.ManuallySort
        return sortingPreviewMapper.mapToAccountSortingPreview(
            sortTypeListItems = sortTypeListItems,
            accountSortingListItems = if (isAccountListVisible) accountSortingListItems else emptyList()
        )
    }

    private fun getSortTypeListItems(
        sortingPreferences: AccountSortingType
    ): List<BaseAccountSortingListItem.SortTypeListItem> {
        val sortingTypes = sortingTypeCreator.createForAccountSorting()
        return sortingTypes.map { sortingType ->
            baseSortingListItemMapper.mapToSortingTypeListItem(
                accountSortingType = sortingType,
                isChecked = sortingType == sortingPreferences
            )
        }
    }

    private fun getAccountSortingListItems(
        sortingPreferences: AccountSortingType
    ): MutableList<BaseAccountSortingListItem> {
        val headerListItem = createHeaderListItem()
        val accountSortListItems = createAccountSortListItems(sortingPreferences)
        return mutableListOf<BaseAccountSortingListItem>().apply {
            add(headerListItem)
            addAll(accountSortListItems)
        }
    }

    private fun createHeaderListItem(): BaseAccountSortingListItem.HeaderListItem {
        return baseSortingListItemMapper.mapToHeaderListItem(R.string.reorganize_accounts_manually)
    }

    private fun createAccountSortListItems(
        sortingPreferences: AccountSortingType
    ): List<BaseAccountSortingListItem.AccountSortListItem> {
        val accountListItems = getSortedAccountsByPreferenceUseCase.getSortedAccountListItems(
            sortingPreferences = sortingPreferences,
            onLoadedAccountConfiguration = {
                val accountValue = getAccountValueUseCase.getAccountValue(this)
                accountItemConfigurationMapper.mapTo(
                    accountAddress = account.address,
                    accountName = account.name,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                    accountType = account.type,
                    accountPrimaryValue = accountValue.primaryAccountValue,
                    dragButtonConfiguration = ButtonConfiguration(
                        iconDrawableResId = R.drawable.ic_reorder,
                        iconTintResId = R.color.text_gray_lighter,
                        iconBackgroundColorResId = R.color.transparent,
                        iconRippleColorResId = R.color.transparent
                    )
                )
            }, onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = address,
                        accountName = name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                        showWarningIcon = true
                    )
                }
            }
        )
        val sortedAccountList = sortingPreferences.sort(accountListItems)
        return sortedAccountList.map { sortedAccountListItem ->
            baseSortingListItemMapper.mapToAccountSortItem(accountListItem = sortedAccountListItem)
        }
    }
}
