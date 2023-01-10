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

package network.voi.hera.modules.nftdomain.ui.usecase

import network.voi.hera.mapper.AccountSelectionListItemMapper
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.modules.nftdomain.domain.usecase.GetNftDomainSearchResultUseCase
import javax.inject.Inject

class GetAccountSelectionNftDomainItemsUseCase @Inject constructor(
    private val getNftDomainSearchResultUseCase: GetNftDomainSearchResultUseCase,
    private val accountSelectionListItemMapper: AccountSelectionListItemMapper
) {

    suspend fun getAccountSelectionNftDomainAccounts(
        query: String
    ): List<BaseAccountSelectionListItem.BaseAccountItem.NftDomainAccountItem> {
        return if (query.isNotBlank()) {
            getNftDomainSearchResultUseCase.getNftDomainSearchResults(query).map {
                accountSelectionListItemMapper.mapToNftDomainAccountItem(it)
            }
        } else emptyList()
    }
}
