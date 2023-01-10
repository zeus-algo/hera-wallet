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

package network.voi.hera.utils

import network.voi.hera.models.Account
import network.voi.hera.models.User
import network.voi.hera.modules.transaction.common.domain.model.TransactionDTO

fun getUserIfSavedLocally(
    contactList: List<User>,
    accountList: List<Account>,
    nonOwnerPublicKey: String?
): User? {
    if (nonOwnerPublicKey == null) {
        return null
    }

    val foundContact = contactList.firstOrNull { it.publicKey == nonOwnerPublicKey }
    if (foundContact != null) {
        return foundContact
    }

    val foundAccount = accountList.firstOrNull { it.address == nonOwnerPublicKey }
    if (foundAccount != null) {
        return User(foundAccount.name, foundAccount.address, null, -1)
    }

    return null
}

fun getAllNestedTransactions(transactionDTO: TransactionDTO): Sequence<TransactionDTO> {
    return sequence {
        if (transactionDTO.innerTransactions != null) {
            yieldAll(transactionDTO.innerTransactions)
            transactionDTO.innerTransactions.forEach { yieldAll(getAllNestedTransactions(it)) }
        }
    }
}
