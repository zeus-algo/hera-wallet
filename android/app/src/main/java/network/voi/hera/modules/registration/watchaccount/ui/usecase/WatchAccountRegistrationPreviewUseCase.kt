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

package network.voi.hera.modules.registration.watchaccount.ui.usecase

import network.voi.hera.R
import network.voi.hera.models.Account
import network.voi.hera.models.AccountCreation
import network.voi.hera.modules.nftdomain.domain.usecase.GetNftDomainSearchResultUseCase
import network.voi.hera.modules.registration.watchaccount.ui.mapper.BasePasteableWatchAccountItemMapper
import network.voi.hera.modules.registration.watchaccount.ui.mapper.WatchAccountRegistrationPreviewMapper
import network.voi.hera.modules.registration.watchaccount.ui.model.BasePasteableWatchAccountItem
import network.voi.hera.modules.registration.watchaccount.ui.model.WatchAccountRegistrationPreview
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.Event
import network.voi.hera.utils.analytics.CreationType
import network.voi.hera.utils.isValidAddress
import network.voi.hera.utils.isValidNFTDomain
import network.voi.hera.utils.toShortenedAddress
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class WatchAccountRegistrationPreviewUseCase @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val getNftDomainSearchResultUseCase: GetNftDomainSearchResultUseCase,
    private val basePasteableWatchAccountItemMapper: BasePasteableWatchAccountItemMapper,
    private val watchAccountRegistrationPreviewMapper: WatchAccountRegistrationPreviewMapper
) {

    fun updatePreviewAccordingAccountAddress(
        currentPreview: WatchAccountRegistrationPreview,
        accountAddress: String,
        nfDomainName: String?
    ): WatchAccountRegistrationPreview {
        return when {
            !accountAddress.isValidAddress() -> {
                currentPreview.copy(showAccountIsNotValidErrorEvent = Event(Unit))
            }
            accountDetailUseCase.isThereAnyAccountWithPublicKey(accountAddress) -> {
                currentPreview.copy(showAccountAlreadyExistErrorEvent = Event(Unit))
            }
            else -> {
                val tempAccount = Account.create(
                    publicKey = accountAddress,
                    detail = Account.Detail.Watch,
                    accountName = nfDomainName.orEmpty()
                )
                val newAccount = AccountCreation(
                    tempAccount = tempAccount,
                    creationType = CreationType.WATCH
                )
                currentPreview.copy(navToNameRegistrationEvent = Event(newAccount))
            }
        }
    }

    fun initWatchAccountRegistrationPreviewFlow(copiedMessage: String, query: String) = flow {
        val nfDomainItemList = createNfDomainItemList(query)
        val accountAddressItem = createAccountAddressItem(copiedMessage)
        val pasteableAccounts = mutableListOf<BasePasteableWatchAccountItem>().apply {
            accountAddressItem?.let { add(it) }
            addAll(nfDomainItemList)
        }
        val preview = watchAccountRegistrationPreviewMapper.mapToWatchAccountRegistrationPreview(
            pasteableAccounts = pasteableAccounts,
            isActionButtonEnabled = query.isValidAddress(),
            errorMessageResId = R.string.account_not_found.takeIf {
                query.isValidNFTDomain() && nfDomainItemList.isEmpty()
            }
        )
        emit(preview)
    }

    private suspend fun createNfDomainItemList(query: String): List<BasePasteableWatchAccountItem.NfDomainItem> {
        return getNftDomainSearchResultUseCase.getNftDomainSearchResults(query).map {
            basePasteableWatchAccountItemMapper.mapToNfDomainItem(
                nfDomainName = it.name,
                nfDomainAccountAddress = it.accountAddress,
                formattedNfDomainAccountAddress = it.accountAddress.toShortenedAddress(),
                nfDomainLogoUrl = it.service?.logoUrl
            )
        }
    }

    private fun createAccountAddressItem(copiedMessage: String): BasePasteableWatchAccountItem.AccountAddressItem? {
        if (!copiedMessage.isValidAddress()) return null
        return basePasteableWatchAccountItemMapper.mapToAccountAddressItem(
            accountAddress = copiedMessage,
            formattedAccountAddress = copiedMessage.toShortenedAddress()
        )
    }
}
