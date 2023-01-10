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
 *
 */

package network.voi.hera.mapper

import network.voi.hera.models.Account
import network.voi.hera.models.AccountDetail
import network.voi.hera.models.AccountDetailSummary
import network.voi.hera.models.AccountIconResource
import network.voi.hera.utils.AccountDisplayName
import javax.inject.Inject

class AccountSummaryMapper @Inject constructor() {

    fun mapTo(accountDetail: AccountDetail, canSignTransaction: Boolean): AccountDetailSummary {
        return with(accountDetail) {
            AccountDetailSummary(
                accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                accountDisplayName = AccountDisplayName.create(account.address, account.name, account.type),
                publicKey = accountInformation.address,
                canSignTransaction = canSignTransaction
            )
        }
    }

    fun mapToAccountDetailSummary(account: Account, canSignTransaction: Boolean): AccountDetailSummary {
        return AccountDetailSummary(
            accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
            accountDisplayName = AccountDisplayName.create(account.address, account.name, account.type),
            publicKey = account.address,
            canSignTransaction = canSignTransaction
        )
    }
}
