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

package network.voi.hera.modules.sorting.accountsorting.domain.usecase

import network.voi.hera.customviews.accountandassetitem.model.BaseItemConfiguration
import network.voi.hera.mapper.BaseAccountAndAssetListItemMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountDetail
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.BaseAccountAndAssetListItem
import network.voi.hera.modules.sorting.accountsorting.domain.model.AccountSortingType
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetSortedLocalAccountsUseCase
import javax.inject.Inject

class GetSortedAccountsByPreferenceUseCase @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val getSortedLocalAccountsUseCase: GetSortedLocalAccountsUseCase,
    private val baseAccountAndAssetListItemMapper: BaseAccountAndAssetListItemMapper,
) {

    fun getSortedAccountListItems(
        sortingPreferences: AccountSortingType,
        onLoadedAccountConfiguration: AccountDetail.() -> BaseItemConfiguration.AccountItemConfiguration,
        onFailedAccountConfiguration: Account?.() -> BaseItemConfiguration.AccountItemConfiguration?
    ): List<BaseAccountAndAssetListItem.AccountListItem> {
        val localAccounts = getSortedLocalAccountsUseCase.getSortedLocalAccounts()
        val accountListItems = localAccounts.asSequence()
            .map { account -> accountDetailUseCase.getCachedAccountDetail(account.address)?.data }
            .mapIndexedNotNull { index, accountDetail ->
                configureListItem(
                    accountDetail = accountDetail,
                    account = localAccounts.getOrNull(index),
                    onLoadedAccountConfiguration = onLoadedAccountConfiguration,
                    onFailedAccountConfiguration = onFailedAccountConfiguration
                )
            }.map { accountItemConfiguration ->
                baseAccountAndAssetListItemMapper.mapToAccountListItem(accountItemConfiguration)
            }.toList()
        return sortingPreferences.sort(accountListItems)
    }

    fun getFilteredSortedAccountListItemsByAccountType(
        sortingPreferences: AccountSortingType,
        excludedAccountTypes: List<Account.Type>?,
        onLoadedAccountConfiguration: AccountDetail.() -> BaseItemConfiguration.AccountItemConfiguration,
        onFailedAccountConfiguration: Account?.() -> BaseItemConfiguration.AccountItemConfiguration?
    ): List<BaseAccountAndAssetListItem.AccountListItem> {
        val localAccounts = getSortedLocalAccountsUseCase.getSortedLocalAccounts()
            .filterNot { account -> account.type in (excludedAccountTypes ?: return@filterNot false) }
        val accountListItems = localAccounts.asSequence()
            .map { account -> accountDetailUseCase.getCachedAccountDetail(account.address)?.data }
            .mapIndexedNotNull { index, accountDetail ->
                configureListItem(
                    accountDetail = accountDetail,
                    account = localAccounts.getOrNull(index),
                    onLoadedAccountConfiguration = onLoadedAccountConfiguration,
                    onFailedAccountConfiguration = onFailedAccountConfiguration
                )
            }.map { accountItemConfiguration ->
                baseAccountAndAssetListItemMapper.mapToAccountListItem(accountItemConfiguration)
            }.toList()
        return sortingPreferences.sort(accountListItems)
    }

    fun getFilteredSortedAccountListItemsByAssetIdAndAccountType(
        sortingPreferences: AccountSortingType,
        excludedAccountTypes: List<Account.Type>?,
        accountFilterAssetId: Long?,
        onLoadedAccountConfiguration: AccountDetail.() -> BaseItemConfiguration.AccountItemConfiguration,
        onFailedAccountConfiguration: Account?.() -> BaseItemConfiguration.AccountItemConfiguration?
    ): List<BaseAccountAndAssetListItem.AccountListItem> {
        val localAccounts = getSortedLocalAccountsUseCase.getSortedLocalAccounts()
        val accountListItems = localAccounts.asSequence()
            .filterNot { account -> account.type in (excludedAccountTypes ?: return@filterNot false) }
            .map { account -> accountDetailUseCase.getCachedAccountDetail(account.address)?.data }
            .filter { accountDetail ->
                accountFilterAssetId == null ||
                    accountFilterAssetId == ALGO_ID ||
                    accountDetail?.accountInformation?.assetHoldingList?.any { assetHolding ->
                        assetHolding.assetId == accountFilterAssetId
                    } == true
            }.mapIndexedNotNull { index, accountDetail ->
                configureListItem(
                    accountDetail = accountDetail,
                    account = localAccounts.getOrNull(index),
                    onLoadedAccountConfiguration = onLoadedAccountConfiguration,
                    onFailedAccountConfiguration = onFailedAccountConfiguration
                )
            }.map { accountItemConfiguration ->
                baseAccountAndAssetListItemMapper.mapToAccountListItem(accountItemConfiguration)
            }.toList()
        return sortingPreferences.sort(accountListItems)
    }

    private fun configureListItem(
        accountDetail: AccountDetail?,
        account: Account?,
        onLoadedAccountConfiguration: AccountDetail.() -> BaseItemConfiguration.AccountItemConfiguration,
        onFailedAccountConfiguration: Account?.() -> BaseItemConfiguration.AccountItemConfiguration?
    ): BaseItemConfiguration.AccountItemConfiguration? {
        return if (accountDetail != null) {
            onLoadedAccountConfiguration(accountDetail)
        } else {
            onFailedAccountConfiguration.invoke((account))
        }
    }
}
