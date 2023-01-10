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

package network.voi.hera.migration

import android.content.SharedPreferences
import network.voi.hera.models.Account
import network.voi.hera.usecase.GetLocalAccountsFromSharedPrefUseCase
import network.voi.hera.utils.AccountMigrationHelper
import network.voi.hera.utils.preference.saveAlgorandAccounts
import com.google.crypto.tink.Aead
import com.google.gson.Gson
import javax.inject.Inject

class AccountMigrationManager @Inject constructor(
    private val aead: Aead,
    private val sharedPref: SharedPreferences,
    private val gson: Gson,
    private val accountMigrationHelper: AccountMigrationHelper,
    private val getLocalAccountsFromSharedPrefUseCase: GetLocalAccountsFromSharedPrefUseCase
) : BaseMigrationManager<List<Account>>() {

    override fun isMigrationNeeded(): Boolean {
        val localAccounts = getLocalAccountsFromSharedPrefUseCase.getLocalAccountsFromSharedPref() ?: return false
        return accountMigrationHelper.isMigrationNeed(localAccounts)
    }

    override fun getDataToBeMigrated(): List<Account> {
        return getLocalAccountsFromSharedPrefUseCase.getLocalAccountsFromSharedPref() ?: emptyList()
    }

    override fun createMigratedData(data: List<Account>): List<Account> {
        return accountMigrationHelper.migrateAccounts(data)
    }

    override fun handleMigratedData(migratedData: List<Account>) {
        sharedPref.saveAlgorandAccounts(gson, migratedData, aead)
    }
}
