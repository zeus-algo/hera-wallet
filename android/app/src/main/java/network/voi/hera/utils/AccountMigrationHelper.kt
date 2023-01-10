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
import javax.inject.Inject

class AccountMigrationHelper @Inject constructor(
    private val accountIndexMigrationHelper: AccountIndexMigrationHelper
) {

    fun isMigrationNeed(accountList: List<Account>): Boolean {
        return accountIndexMigrationHelper.isMigrationNeeded(accountList)
    }

    fun migrateAccounts(accountList: List<Account>): List<Account> {
        return accountIndexMigrationHelper.getMigratedValues(accountList)
    }
}
