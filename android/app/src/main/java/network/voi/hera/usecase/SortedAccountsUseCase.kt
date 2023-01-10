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

import network.voi.hera.core.AccountManager
import network.voi.hera.mapper.SortedAccountsMapper
import network.voi.hera.models.Account
import network.voi.hera.models.SortedAccounts
import javax.inject.Inject

class SortedAccountsUseCase @Inject constructor(
    private val accountManager: AccountManager,
    private val sortedAccountsMapper: SortedAccountsMapper
) {

    fun getSortedLocalAccounts(): SortedAccounts {
        val localAccounts = accountManager.getAccounts()
        val (sortedNormalLocalAccounts, sortedWatchLocalAccounts) = getSortedSplittedAccounts(localAccounts)
        return sortedAccountsMapper.mapToSortedAccounts(sortedNormalLocalAccounts, sortedWatchLocalAccounts)
    }

    fun getSortedLocalAccounts(localAccounts: List<Account>): List<Account> {
        return localAccounts.sortedBy { it.index }
    }

    private fun getSortedSplittedAccounts(accountList: List<Account>): Pair<List<Account>, List<Account>> {
        return accountList
            .sortedBy { it.index }
            .partition { it.type != Account.Type.WATCH }
    }
}
