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
import network.voi.hera.mapper.BaseAccountAndAssetListItemMapper
import network.voi.hera.models.Account.Companion.defaultAccountType
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.utils.toShortenedAddress
import javax.inject.Inject

class CreateAccountSelectionAccountItemUseCase @Inject constructor(
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val accountSelectionListItemMapper: AccountSelectionListItemMapper,
    private val baseAccountAndAssetListItemMapper: BaseAccountAndAssetListItemMapper
) {

    fun createAccountSelectionAccountItemFromAccountAddress(
        accountAddress: String
    ): BaseAccountSelectionListItem.BaseAccountItem.AccountItem {
        val accountType = defaultAccountType
        val accountItemConfiguration = accountItemConfigurationMapper.mapTo(
            accountAddress = accountAddress,
            accountName = accountAddress.toShortenedAddress(),
            accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(accountType),
            accountType = accountType
        )
        val accountListItem = baseAccountAndAssetListItemMapper.mapToAccountListItem(accountItemConfiguration)
        return accountSelectionListItemMapper.mapToAccountItem(accountListItem)
    }
}
