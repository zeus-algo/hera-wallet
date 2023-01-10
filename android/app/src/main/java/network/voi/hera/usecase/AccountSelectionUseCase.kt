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

import network.voi.hera.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import network.voi.hera.mapper.AccountSelectionMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AccountSelection
import network.voi.hera.modules.accounts.domain.usecase.GetAccountValueUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import network.voi.hera.utils.formatAsCurrency
import javax.inject.Inject

class AccountSelectionUseCase @Inject constructor(
    private val accountSelectionMapper: AccountSelectionMapper,
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val getAccountValueUseCase: GetAccountValueUseCase,
    private val parityUseCase: ParityUseCase,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase
) {

    suspend fun getAccountFilteredByAssetId(assetId: Long): List<AccountSelection> {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrEmpty()
        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase
            .getFilteredSortedAccountListItemsByAssetIdAndAccountType(
                sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
                excludedAccountTypes = listOf(Account.Type.WATCH),
                accountFilterAssetId = assetId,
                onLoadedAccountConfiguration = {
                    val accountValue = getAccountValueUseCase.getAccountValue(this)
                    accountItemConfigurationMapper.mapTo(
                        accountName = account.name,
                        accountAddress = account.address,
                        accountType = account.type,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                        accountPrimaryValue = accountValue.primaryAccountValue,
                        accountPrimaryValueText = accountValue.primaryAccountValue.formatAsCurrency(
                            symbol = selectedCurrencySymbol,
                            isCompact = true,
                            isFiat = true
                        ),
                        accountAssetCount = this.accountInformation.getOptedInAssetsCount()
                    )
                },
                onFailedAccountConfiguration = {
                    this?.run {
                        accountItemConfigurationMapper.mapTo(
                            accountName = name,
                            accountAddress = address,
                            accountType = type,
                            accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                            showWarningIcon = true
                        )
                    }
                }
            )
        return sortedAccountListItems.map { accountListItem ->
            accountSelectionMapper.mapToAccountSelection(
                accountDisplayName = accountListItem.itemConfiguration.accountDisplayName,
                accountIconResource = accountListItem.itemConfiguration.accountIconResource,
                accountAddress = accountListItem.itemConfiguration.accountAddress,
                accountAssetCount = accountListItem.itemConfiguration.accountAssetCount
            )
        }
    }
}
