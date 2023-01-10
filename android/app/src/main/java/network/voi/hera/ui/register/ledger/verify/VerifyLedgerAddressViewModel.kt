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

package network.voi.hera.ui.register.ledger.verify

import javax.inject.Inject
import androidx.lifecycle.MutableLiveData
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.Account
import network.voi.hera.usecase.AccountAdditionUseCase
import network.voi.hera.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import network.voi.hera.utils.analytics.CreationType

@HiltViewModel
class VerifyLedgerAddressViewModel @Inject constructor(
    private val verifyLedgerAddressQueueManager: VerifyLedgerAddressQueueManager,
    private val accountAdditionUseCase: AccountAdditionUseCase
) : BaseViewModel() {

    val currentLedgerAddressesListLiveData = MutableLiveData<List<VerifyLedgerAddressListItem>>()

    val awaitingLedgerAccountLiveData = MutableLiveData<Account?>()

    val awaitingLedgerAccount
        get() = awaitingLedgerAccountLiveData.value

    val isVerifyOperationsDoneLiveData = MutableLiveData<Event<Boolean>?>()

    private val listLock = Any()

    private val verifyLedgerAddressQueueManagerListener = object : VerifyLedgerAddressQueueManager.Listener {
        override fun onNextQueueItem(ledgerDetail: Account) {
            awaitingLedgerAccountLiveData.value = ledgerDetail
            changeCurrentOperatedAddressStatus(VerifiableLedgerAddressItemStatus.AWAITING_VERIFICATION)
        }

        override fun onQueueCompleted() {
            awaitingLedgerAccountLiveData.postValue(null)
            isVerifyOperationsDoneLiveData.postValue(Event(true))
        }
    }

    init {
        verifyLedgerAddressQueueManager.setListener(verifyLedgerAddressQueueManagerListener)
    }

    fun createListAuthLedgerAccounts(authLedgerAccounts: List<Account>) {
        val verifiableLedgerAddress: List<VerifyLedgerAddressListItem> = authLedgerAccounts.map { ledgerAccount ->
            VerifyLedgerAddressListItem.VerifiableLedgerAddressItem(ledgerAccount.address)
        }
        verifiableLedgerAddress.toMutableList().add(0, VerifyLedgerAddressListItem.VerifyLedgerHeaderItem)
        currentLedgerAddressesListLiveData.value = verifiableLedgerAddress
        verifyLedgerAddressQueueManager.fillQueue(authLedgerAccounts)
    }

    fun onCurrentOperationDone(isVerified: Boolean) {
        changeCurrentOperatedAddressStatus(
            if (isVerified) {
                VerifiableLedgerAddressItemStatus.APPROVED
            } else {
                VerifiableLedgerAddressItemStatus.REJECTED
            }
        )
        moveToNextVerification()
    }

    private fun moveToNextVerification() {
        verifyLedgerAddressQueueManager.moveQueue()
    }

    fun changeCurrentOperatedAddressStatus(newStatus: VerifiableLedgerAddressItemStatus) {
        synchronized(listLock) {
            val currentList = currentLedgerAddressesListLiveData.value
            val currentOperatedAddress = awaitingLedgerAccount?.address
            if (currentList != null && currentOperatedAddress != null) {
                val newList = mutableListOf<VerifyLedgerAddressListItem>().apply {
                    add(VerifyLedgerAddressListItem.VerifyLedgerHeaderItem)
                }
                currentList
                    .filterIsInstance<VerifyLedgerAddressListItem.VerifiableLedgerAddressItem>()
                    .forEach {
                        val changedStatus = if (it.address == currentOperatedAddress) newStatus else it.status
                        val copyItem = it.copy(status = changedStatus)
                        newList.add(copyItem)
                    }
                currentLedgerAddressesListLiveData.value = newList
            }
        }
    }

    private fun getAllApprovedAuths(): List<VerifyLedgerAddressListItem.VerifiableLedgerAddressItem> {
        return currentLedgerAddressesListLiveData.value
            ?.filterIsInstance<VerifyLedgerAddressListItem.VerifiableLedgerAddressItem>()
            ?.filter { it.status == VerifiableLedgerAddressItemStatus.APPROVED }
            .orEmpty()
    }

    fun getSelectedVerifiedAccounts(allSelectedAccounts: List<Account>): List<Account> {
        val approvedLedgerAuths = getAllApprovedAuths()
        if (approvedLedgerAuths.isEmpty()) {
            return emptyList()
        }

        return mutableListOf<Account>().apply {
            outerloop@ for (selectedAccount in allSelectedAccounts) {
                if (approvedLedgerAuths.any { it.address == selectedAccount.address }) {
                    add(selectedAccount)
                    continue
                }
                if (selectedAccount.detail is Account.Detail.RekeyedAuth) {
                    for (auth in approvedLedgerAuths) {
                        if (selectedAccount.detail.rekeyedAuthDetail.containsKey(auth.address)) {
                            add(selectedAccount)
                            continue@outerloop
                        }
                    }
                }
            }
        }
    }

    fun addNewAccount(account: Account, creationType: CreationType?) {
        // TODO: Handle error case
        accountAdditionUseCase.addNewAccount(account, creationType)
    }
}
