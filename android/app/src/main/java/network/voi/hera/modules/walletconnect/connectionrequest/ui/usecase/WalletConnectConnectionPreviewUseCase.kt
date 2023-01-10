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

package network.voi.hera.modules.walletconnect.connectionrequest.ui.usecase

import network.voi.hera.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.BaseAccountAndAssetListItem
import network.voi.hera.models.WalletConnectPeerMeta
import network.voi.hera.models.WalletConnectSession
import network.voi.hera.models.ui.AccountAssetItemButtonState.CHECKED
import network.voi.hera.models.ui.AccountAssetItemButtonState.UNCHECKED
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import network.voi.hera.modules.walletconnect.connectionrequest.ui.mapper.BaseWalletConnectConnectionItemMapper
import network.voi.hera.modules.walletconnect.connectionrequest.ui.mapper.WCSessionRequestResultMapper
import network.voi.hera.modules.walletconnect.connectionrequest.ui.mapper.WalletConnectConnectionPreviewMapper
import network.voi.hera.modules.walletconnect.connectionrequest.ui.model.BaseWalletConnectConnectionItem
import network.voi.hera.modules.walletconnect.connectionrequest.ui.model.WCSessionRequestResult
import network.voi.hera.modules.walletconnect.connectionrequest.ui.model.WalletConnectConnectionPreview
import javax.inject.Inject

class WalletConnectConnectionPreviewUseCase @Inject constructor(
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase,
    private val baseWalletConnectConnectionItemMapper: BaseWalletConnectConnectionItemMapper,
    private val walletConnectConnectionPreviewMapper: WalletConnectConnectionPreviewMapper,
    private val wCSessionRequestResultMapper: WCSessionRequestResultMapper
) {

    suspend fun getWalletConnectConnectionPreview(
        walletConnectPeerMeta: WalletConnectPeerMeta
    ): WalletConnectConnectionPreview {
        val sortedAccountList = createSortedAccountList()
        val isThereOnlyOneAccount = sortedAccountList.count() == 1
        val preSelectedButtonState = if (isThereOnlyOneAccount) CHECKED else UNCHECKED

        val accountItems = sortedAccountList.map { accountListItem ->
            baseWalletConnectConnectionItemMapper.mapToAccountItem(
                accountAddress = accountListItem.itemConfiguration.accountAddress,
                accountIconResource = accountListItem.itemConfiguration.accountIconResource,
                accountDisplayName = accountListItem.itemConfiguration.accountDisplayName,
                buttonState = preSelectedButtonState,
                isChecked = isThereOnlyOneAccount
            )
        }

        val dAppInfoItem = baseWalletConnectConnectionItemMapper.mapToDappInfoItem(
            name = walletConnectPeerMeta.name,
            url = walletConnectPeerMeta.url,
            peerIconUri = walletConnectPeerMeta.peerIconUri.toString()
        )

        val accountsTitleItem = baseWalletConnectConnectionItemMapper.mapToAccountsTitleItem(
            accountCount = accountItems.count()
        )

        val baseWalletConnectConnectionItems = mutableListOf<BaseWalletConnectConnectionItem>().apply {
            add(dAppInfoItem)
            add(accountsTitleItem)
            addAll(accountItems)
        }

        return walletConnectConnectionPreviewMapper.mapToWalletConnectConnectionPreview(
            baseWalletConnectConnectionItems = baseWalletConnectConnectionItems,
            isConfirmationButtonEnabled = accountItems.any { it.isChecked }
        )
    }

    private suspend fun createSortedAccountList(): List<BaseAccountAndAssetListItem.AccountListItem> {
        return getSortedAccountsByPreferenceUseCase.getFilteredSortedAccountListItemsByAccountType(
            sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
            excludedAccountTypes = listOf(Account.Type.WATCH),
            onLoadedAccountConfiguration = {
                accountItemConfigurationMapper.mapTo(
                    accountName = account.name,
                    accountAddress = account.address,
                    accountType = account.type,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type)
                )
            },
            onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountName = name,
                        accountAddress = address,
                        accountType = type,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type)
                    )
                }
            }
        )
    }

    fun updatePreviewStateAccordingToAccountSelection(
        preview: WalletConnectConnectionPreview,
        accountAddress: String
    ): WalletConnectConnectionPreview {
        val baseWalletConnectConnectionItems = preview.baseWalletConnectConnectionItems.map {
            when (it) {
                is BaseWalletConnectConnectionItem.AccountsTitleItem,
                is BaseWalletConnectConnectionItem.DappInfoItem -> it
                is BaseWalletConnectConnectionItem.AccountItem -> updateSelectedItemButtonState(it, accountAddress)
            }
        }
        val isConfirmationButtonEnabled = baseWalletConnectConnectionItems
            .filterIsInstance<BaseWalletConnectConnectionItem.AccountItem>()
            .any { it.isChecked }

        return walletConnectConnectionPreviewMapper.mapToWalletConnectConnectionPreview(
            baseWalletConnectConnectionItems = baseWalletConnectConnectionItems,
            isConfirmationButtonEnabled = isConfirmationButtonEnabled
        )
    }

    private fun updateSelectedItemButtonState(
        accountItem: BaseWalletConnectConnectionItem.AccountItem,
        updatedAccountAddress: String
    ): BaseWalletConnectConnectionItem.AccountItem {
        return if (accountItem.accountAddress == updatedAccountAddress) {
            val oppositeState = !accountItem.isChecked
            accountItem.copy(isChecked = oppositeState, buttonState = if (oppositeState) CHECKED else UNCHECKED)
        } else {
            accountItem
        }
    }

    fun getApprovedWalletConnectSessionResult(
        accountAddresses: List<String>,
        wcSessionRequest: WalletConnectSession
    ): WCSessionRequestResult.ApproveRequest {
        return wCSessionRequestResultMapper.mapToApproveRequest(
            accountAddresses = accountAddresses,
            wcSessionRequest = wcSessionRequest
        )
    }

    fun getRejectedWalletConnectSessionResult(
        wcSessionRequest: WalletConnectSession
    ): WCSessionRequestResult.RejectRequest {
        return wCSessionRequestResultMapper.mapToRejectRequest(wcSessionRequest = wcSessionRequest)
    }
}
