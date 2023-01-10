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

package network.voi.hera.ui.wctransactionrequest

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.R
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.Account.Type.LEDGER
import network.voi.hera.models.Account.Type.REKEYED
import network.voi.hera.models.Account.Type.REKEYED_AUTH
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.WalletConnectSignResult
import network.voi.hera.models.WalletConnectTransaction
import network.voi.hera.models.builder.WalletConnectTransactionListBuilder
import network.voi.hera.utils.Event
import network.voi.hera.utils.Resource
import network.voi.hera.utils.getOrElse
import network.voi.hera.utils.preference.getFirstWalletConnectRequestBottomSheetShown
import network.voi.hera.utils.preference.setFirstWalletConnectRequestBottomSheetShown
import network.voi.hera.utils.walletconnect.WalletConnectManager
import network.voi.hera.utils.walletconnect.WalletConnectSignManager
import network.voi.hera.utils.walletconnect.WalletConnectTransactionErrorProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WalletConnectTransactionRequestViewModel @Inject constructor(
    private val walletConnectManager: WalletConnectManager,
    private val errorProvider: WalletConnectTransactionErrorProvider,
    private val sharedPreferences: SharedPreferences,
    private val walletConnectSignManager: WalletConnectSignManager,
    private val transactionListBuilder: WalletConnectTransactionListBuilder,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val requestResultLiveData: LiveData<Event<Resource<AnnotatedString>>>
        get() = walletConnectManager.requestResultLiveData

    val signResultLiveData: LiveData<WalletConnectSignResult>
        get() = walletConnectSignManager.signResultLiveData

    val transaction: WalletConnectTransaction?
        get() = walletConnectManager.transaction

    val fallbackBrowserGroupResponse: String
        get() = transaction?.session?.fallbackBrowserGroupResponse.orEmpty()

    val peerMetaName: String
        get() = transaction?.session?.peerMeta?.name.orEmpty()

    val shouldSkipConfirmation = savedStateHandle.getOrElse(SHOULD_SKIP_CONFIRMATION_KEY, false)

    fun setupWalletConnectSignManager(lifecycle: Lifecycle) {
        walletConnectSignManager.setup(lifecycle)
    }

    fun rejectRequest() {
        transaction?.let {
            walletConnectManager.rejectRequest(it.session.id, it.requestId, errorProvider.rejected.userRejection)
        }
    }

    fun shouldShowFirstRequestBottomSheet(): Boolean {
        return !sharedPreferences.getFirstWalletConnectRequestBottomSheetShown().also {
            sharedPreferences.setFirstWalletConnectRequestBottomSheetShown()
        }
    }

    fun signTransactionRequest(transaction: WalletConnectTransaction) {
        viewModelScope.launch {
            walletConnectSignManager.signTransaction(transaction)
        }
    }

    fun processWalletConnectSignResult(result: WalletConnectSignResult) {
        walletConnectManager.processWalletConnectSignResult(result)
    }

    fun stopAllResources() {
        walletConnectSignManager.manualStopAllResources()
    }

    fun isBluetoothNeededToSignTxns(transaction: WalletConnectTransaction): Boolean {
        return transaction.transactionList.flatten().any {
            val accountDetail = it.fromAccount?.type ?: return false
            accountDetail == LEDGER || accountDetail == REKEYED || accountDetail == REKEYED_AUTH
        }
    }

    fun handleStartDestinationAndArgs(transactionList: List<WalletConnectTransactionListItem>): Pair<Int, Bundle?> {
        val startDestination = if (
            transactionList.count() == 1 &&
            transactionList.first() is WalletConnectTransactionListItem.SingleTransactionItem
        ) {
            R.id.walletConnectSingleTransactionFragment
        } else {
            R.id.walletConnectMultipleTransactionFragment
        }

        val startDestinationArgs = when (startDestination) {
            R.id.walletConnectSingleTransactionFragment -> {
                Bundle().apply { putParcelable(SINGLE_TRANSACTION_KEY, transactionList.first()) }
            }
            R.id.walletConnectMultipleTransactionFragment -> {
                Bundle().apply { putParcelableArray(MULTIPLE_TRANSACTION_KEY, transactionList.toTypedArray()) }
            }
            else -> null
        }

        return Pair(startDestination, startDestinationArgs)
    }

    fun createTransactionListItems(
        transactionList: List<List<BaseWalletConnectTransaction>>
    ): List<WalletConnectTransactionListItem> {
        return transactionListBuilder.createTransactionItems(transactionList)
    }

    companion object {
        private const val MULTIPLE_TRANSACTION_KEY = "transactions"
        private const val SINGLE_TRANSACTION_KEY = "transaction"
        private const val SHOULD_SKIP_CONFIRMATION_KEY = "shouldSkipConfirmation"
    }
}
