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

import network.voi.hera.R
import network.voi.hera.core.BaseUseCase
import network.voi.hera.mapper.LedgerInformationAccountItemMapper
import network.voi.hera.mapper.LedgerInformationAssetItemMapper
import network.voi.hera.mapper.LedgerInformationCanSignByItemMapper
import network.voi.hera.mapper.LedgerInformationTitleItemMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountBalance
import network.voi.hera.models.AccountDetail
import network.voi.hera.models.AccountSelectionListItem
import network.voi.hera.models.LedgerInformationListItem
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.utils.formatAsCurrency
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("LongParameterList")
class LedgerInformationUseCase @Inject constructor(
    private val accountTotalBalanceUseCase: AccountTotalBalanceUseCase,
    private val parityUseCase: ParityUseCase,
    private val accountAssetAmountUseCase: AccountAssetAmountUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val accountAlgoAmountUseCase: AccountAlgoAmountUseCase,
    private val ledgerInformationTitleItemMapper: LedgerInformationTitleItemMapper,
    private val ledgerInformationAccountItemMapper: LedgerInformationAccountItemMapper,
    private val ledgerInformationAssetItemMapper: LedgerInformationAssetItemMapper,
    private val ledgerInformationCanSignByItemMapper: LedgerInformationCanSignByItemMapper,
    private val currencyUseCase: CurrencyUseCase
) : BaseUseCase() {

    suspend fun getLedgerInformationListItem(
        selectedLedgerAccount: AccountSelectionListItem.AccountItem,
        rekeyedAccountSelectionListItem: List<AccountSelectionListItem.AccountItem>?,
        authLedgerAccount: AccountSelectionListItem.AccountItem?
    ): List<LedgerInformationListItem> {
        val accountDetail = AccountDetail(selectedLedgerAccount.account, selectedLedgerAccount.accountInformation)
        return prepareLedgerInformationListItem(
            accountDetail,
            selectedLedgerAccount,
            rekeyedAccountSelectionListItem,
            authLedgerAccount
        )
    }

    private suspend fun prepareLedgerInformationListItem(
        accountDetail: AccountDetail,
        selectedLedgerAccount: AccountSelectionListItem.AccountItem,
        rekeyedAccountSelectionListItem: List<AccountSelectionListItem.AccountItem>?,
        authLedgerAccount: AccountSelectionListItem.AccountItem?
    ): List<LedgerInformationListItem> {
        return withContext(Dispatchers.Default) {
            return@withContext mutableListOf<LedgerInformationListItem>().apply {
                val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrName()
                val accountBalance = accountTotalBalanceUseCase.getAccountBalance(accountDetail)
                val portfolioValue = getPortfolioValue(accountBalance, selectedCurrencySymbol)
                addAll(createLedgerAccountItem(accountDetail, portfolioValue))
                addAll(createAssetItems(accountDetail))
                addAll(createCanSignByItems(authLedgerAccount))
                addAll(createCanSignableAccounts(selectedLedgerAccount, rekeyedAccountSelectionListItem))
            }
        }
    }

    private fun createLedgerAccountItem(
        accountDetail: AccountDetail,
        portfolioValue: String
    ): List<LedgerInformationListItem> {
        return mutableListOf<LedgerInformationListItem>().apply {
            add(ledgerInformationTitleItemMapper.mapTo(R.string.ledger_account))
            add(ledgerInformationAccountItemMapper.mapTo(accountDetail, portfolioValue))
        }
    }

    private fun createAssetItems(accountDetail: AccountDetail): List<LedgerInformationListItem> {
        return mutableListOf<LedgerInformationListItem>().apply {
            val algoAssetData = accountAlgoAmountUseCase.getAccountAlgoAmount(accountDetail)
            add(ledgerInformationTitleItemMapper.mapTo(R.string.assets))
            add(ledgerInformationAssetItemMapper.mapTo(algoAssetData))
            if (accountDetail.accountInformation.assetHoldingList.isNotEmpty()) {
                accountDetail.accountInformation.assetHoldingList.forEach {
                    val assetQueryItem = simpleAssetDetailUseCase.getCachedAssetDetail(it.assetId)?.data
                        ?: return@forEach
                    val accountAssetData = accountAssetAmountUseCase.getAssetAmount(it, assetQueryItem)
                    add(ledgerInformationAssetItemMapper.mapTo(accountAssetData))
                }
            }
        }
    }

    private fun createCanSignByItems(
        authLedgerAccount: AccountSelectionListItem.AccountItem?
    ): List<LedgerInformationListItem> {
        return mutableListOf<LedgerInformationListItem>().apply {
            authLedgerAccount?.run {
                add(ledgerInformationTitleItemMapper.mapTo(R.string.can_be_signed_by))
                add(ledgerInformationCanSignByItemMapper.mapTo(this))
            }
        }
    }

    private fun createCanSignableAccounts(
        selectedLedgerAccount: AccountSelectionListItem.AccountItem,
        rekeyedAccountSelectionListItem: List<AccountSelectionListItem.AccountItem>?
    ): List<LedgerInformationListItem> {
        return mutableListOf<LedgerInformationListItem>().apply {
            if (selectedLedgerAccount.account.type != Account.Type.REKEYED_AUTH) {
                rekeyedAccountSelectionListItem?.takeIf { it.isNotEmpty() }?.run {
                    add(ledgerInformationTitleItemMapper.mapTo(R.string.can_sign_for_these))
                    forEach { add(ledgerInformationCanSignByItemMapper.mapTo(it)) }
                }
            }
        }
    }

    private fun getPortfolioValue(
        accountBalance: AccountBalance,
        symbol: String
    ): String {
        val totalHoldings = with(accountBalance) { algoHoldingsInSelectedCurrency.add(assetHoldingsInSelectedCurrency) }
        val isSelectedPrimaryCurrencyFiat = !currencyUseCase.isPrimaryCurrencyAlgo()
        return totalHoldings.formatAsCurrency(symbol, isFiat = isSelectedPrimaryCurrencyFiat)
    }
}
