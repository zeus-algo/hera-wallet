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

package network.voi.hera.usecase

import network.voi.hera.core.BaseUseCase
import network.voi.hera.mapper.AccountInformationMapper
import network.voi.hera.mapper.LedgerAccountSelectionAccountItemMapper
import network.voi.hera.mapper.LedgerAccountSelectionInstructionItemMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountInformation
import network.voi.hera.models.AccountSelectionListItem
import network.voi.hera.Repository.AccountRepository
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.Resource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow

class LedgerAccountSelectionUseCase @Inject constructor(
    private val assetFetchAndCacheUseCase: AssetFetchAndCacheUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val accountCacheManager: AccountCacheManager,
    private val accountRepository: AccountRepository,
    private val ledgerAccountSelectionInstructionItemMapper: LedgerAccountSelectionInstructionItemMapper,
    private val ledgerAccountSelectionAccountItemMapper: LedgerAccountSelectionAccountItemMapper,
    private val accountInformationMapper: AccountInformationMapper
) : BaseUseCase() {

    fun getAuthAccountOf(
        accountSelectionListItem: AccountSelectionListItem.AccountItem,
        accountSelectionAccountList: List<AccountSelectionListItem.AccountItem>?
    ): AccountSelectionListItem.AccountItem? {
        return accountSelectionAccountList?.run {
            if (accountSelectionListItem.accountInformation.isRekeyed()) {
                val rekeyAdminAddress = accountSelectionListItem.accountInformation.rekeyAdminAddress
                firstOrNull { rekeyAdminAddress == it.account.address }
            } else {
                null
            }
        }
    }

    fun getRekeyedAccountOf(
        accountSelectionListItem: AccountSelectionListItem.AccountItem,
        accountSelectionAccountList: List<AccountSelectionListItem.AccountItem>?
    ): Array<AccountSelectionListItem.AccountItem>? {
        val accountAddress = accountSelectionListItem.account.address
        return accountSelectionAccountList?.filter {
            it.account.address != accountAddress && it.accountInformation.rekeyAdminAddress == accountAddress
        }?.toTypedArray()
    }

    suspend fun getAccountSelectionListItems(
        ledgerAccountsInformation: Array<AccountInformation>,
        bluetoothAddress: String,
        bluetoothName: String?,
        coroutineScope: CoroutineScope
    ) = flow<Resource<List<AccountSelectionListItem>>> {
        emit(Resource.Loading)
        mutableListOf<AccountSelectionListItem>().apply {

            val instructionItem = ledgerAccountSelectionInstructionItemMapper.mapTo(ledgerAccountsInformation.size)
            add(instructionItem)

            ledgerAccountsInformation.forEachIndexed { index, ledgerAccountInformation ->

                // Cache ledger accounts assets
                cacheLedgerAccountAssets(ledgerAccountInformation, coroutineScope)

                val authAccountDetail = Account.Detail.Ledger(bluetoothAddress, bluetoothName, index)
                val authAccountSelectionListItem = ledgerAccountSelectionAccountItemMapper.mapTo(
                    accountInformation = ledgerAccountInformation,
                    accountDetail = authAccountDetail,
                    accountCacheManager = accountCacheManager
                )
                add(authAccountSelectionListItem)
                val rekeyedAccountSelectionListItems = getRekeyedAccountsOfAuthAccount(
                    ledgerAccountInformation.address, authAccountDetail, coroutineScope
                )
                addAll(rekeyedAccountSelectionListItems)
            }
            emit(Resource.Success(this))
        }
    }

    private suspend fun getRekeyedAccountsOfAuthAccount(
        rekeyAdminAddress: String,
        ledgerDetail: Account.Detail.Ledger,
        coroutineScope: CoroutineScope
    ): List<AccountSelectionListItem.AccountItem> {
        val deferredAccountSelectionListItems = mutableListOf<AccountSelectionListItem.AccountItem>()
        accountRepository.getRekeyedAccounts(rekeyAdminAddress).use(
            onSuccess = { rekeyedAccountsListResponse ->
                val rekeyedAccounts = rekeyedAccountsListResponse.accountInformationList
                    ?.filterNot { it.address == rekeyAdminAddress }
                    ?.map {
                        accountInformationMapper.mapToAccountInformation(it, rekeyedAccountsListResponse.currentRound)
                    }
                    ?.map { accountInformation ->

                        // Cache rekeyed accounts assets
                        cacheLedgerAccountAssets(accountInformation, coroutineScope)

                        val detail = Account.Detail.RekeyedAuth.create(null, mapOf(rekeyAdminAddress to ledgerDetail))
                        ledgerAccountSelectionAccountItemMapper.mapTo(
                            accountInformation = accountInformation,
                            accountDetail = detail,
                            accountCacheManager = accountCacheManager
                        )
                    }
                    .orEmpty()
                deferredAccountSelectionListItems.addAll(rekeyedAccounts)
            }
        )
        return deferredAccountSelectionListItems
    }

    private suspend fun cacheLedgerAccountAssets(
        accountInformation: AccountInformation,
        coroutineScope: CoroutineScope
    ) {
        val assetIds = accountInformation.assetHoldingList.map { it.assetId }.toSet()
        val filteredAssetList = simpleAssetDetailUseCase.getChunkedAndFilteredAssetList(assetIds)
        assetFetchAndCacheUseCase.processFilteredAssetIdList(filteredAssetList, coroutineScope)
    }
}
