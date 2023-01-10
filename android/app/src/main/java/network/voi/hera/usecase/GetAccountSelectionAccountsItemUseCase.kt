/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 */

package network.voi.hera.usecase

import network.voi.hera.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import network.voi.hera.mapper.AccountSelectionListItemMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.modules.accounts.domain.usecase.GetAccountValueUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import network.voi.hera.utils.formatAsCurrency
import javax.inject.Inject

class GetAccountSelectionAccountsItemUseCase @Inject constructor(
    private val parityUseCase: ParityUseCase,
    private val accountSelectionListItemMapper: AccountSelectionListItemMapper,
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val getAccountValueUseCase: GetAccountValueUseCase,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase
) {

    // TODO: 11.03.2022 Use flow here to get realtime updates
    suspend fun getAccountSelectionAccounts(
        showHoldings: Boolean,
        shouldIncludeWatchAccounts: Boolean,
        showFailedAccounts: Boolean,
        assetId: Long? = null,
    ): List<BaseAccountSelectionListItem.BaseAccountItem> {
        val excludedAccountTypes = if (shouldIncludeWatchAccounts) null else listOf(Account.Type.WATCH)
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrEmpty()

        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase
            .getFilteredSortedAccountListItemsByAssetIdAndAccountType(
                sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
                excludedAccountTypes = excludedAccountTypes,
                accountFilterAssetId = assetId,
                onLoadedAccountConfiguration = {
                    val accountValue = getAccountValueUseCase.getAccountValue(this)
                    val primaryAccountValue = accountValue.primaryAccountValue
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = account.address,
                        accountName = account.name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                        accountPrimaryValueText = if (showHoldings) {
                            primaryAccountValue.formatAsCurrency(
                                symbol = selectedCurrencySymbol,
                                isCompact = true,
                                isFiat = true
                            )
                        } else {
                            null
                        },
                        accountPrimaryValue = primaryAccountValue,
                        accountType = account.type
                    )
                },
                onFailedAccountConfiguration = {
                    this?.run {
                        accountItemConfigurationMapper.mapTo(
                            accountAddress = address,
                            accountName = name,
                            accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                            showWarningIcon = true,
                            accountType = type
                        )
                    }
                }
            )
        return sortedAccountListItems.map { accountListItem ->
            if (accountListItem.itemConfiguration.showWarning == true) {
                accountSelectionListItemMapper.mapToErrorAccountItem(accountListItem)
            } else {
                accountSelectionListItemMapper.mapToAccountItem(accountListItem)
            }
        }.filter {
            if (showFailedAccounts) true else it !is BaseAccountSelectionListItem.BaseAccountItem.AccountErrorItem
        }
    }
}
