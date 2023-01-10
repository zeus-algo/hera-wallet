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
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.TransactionTargetUser
import network.voi.hera.models.User
import javax.inject.Inject

class TransactionTargetUserMapper @Inject constructor() {

    fun mapTo(
        publicKey: String,
        displayName: String,
        contact: User? = null,
        account: Account? = null
    ): TransactionTargetUser {
        return TransactionTargetUser(
            publicKey = publicKey,
            displayName = displayName,
            contact = contact,
            accountName = account?.name.takeIf { it.isNullOrEmpty() },
            accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account?.type)
        )
    }
}
