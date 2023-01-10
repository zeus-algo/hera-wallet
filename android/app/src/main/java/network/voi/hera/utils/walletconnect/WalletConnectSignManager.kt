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

package network.voi.hera.utils.walletconnect

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import network.voi.hera.ledger.CustomScanCallback
import network.voi.hera.ledger.LedgerBleOperationManager
import network.voi.hera.ledger.LedgerBleSearchManager
import network.voi.hera.ledger.operations.WalletConnectTransactionOperation
import network.voi.hera.models.Account
import network.voi.hera.models.Account.Detail.Ledger
import network.voi.hera.models.Account.Detail.RekeyedAuth
import network.voi.hera.models.Account.Detail.Standard
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.LedgerBleResult
import network.voi.hera.models.LedgerBleResult.AppErrorResult
import network.voi.hera.models.LedgerBleResult.LedgerErrorResult
import network.voi.hera.models.LedgerBleResult.OperationCancelledResult
import network.voi.hera.models.LedgerBleResult.SignedTransactionResult
import network.voi.hera.models.WalletConnectSignResult
import network.voi.hera.models.WalletConnectSignResult.Error.Api
import network.voi.hera.models.WalletConnectSignResult.Error.Defined
import network.voi.hera.models.WalletConnectSignResult.LedgerWaitingForApproval
import network.voi.hera.models.WalletConnectSignResult.Success
import network.voi.hera.models.WalletConnectSignResult.TransactionCancelled
import network.voi.hera.models.WalletConnectTransaction
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.Event
import network.voi.hera.utils.LifecycleScopedCoroutineOwner
import network.voi.hera.utils.ListQueuingHelper
import network.voi.hera.utils.sendErrorLog
import network.voi.hera.utils.signTx
import javax.inject.Inject
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class WalletConnectSignManager @Inject constructor(
    private val walletConnectSignValidator: WalletConnectSignValidator,
    private val ledgerBleSearchManager: LedgerBleSearchManager,
    private val ledgerBleOperationManager: LedgerBleOperationManager,
    private val signHelper: WalletConnectSigningHelper,
    private val accountDetailUseCase: AccountDetailUseCase
) : LifecycleScopedCoroutineOwner() {

    val signResultLiveData: LiveData<WalletConnectSignResult>
        get() = _signResultLiveData
    private val _signResultLiveData = MutableLiveData<WalletConnectSignResult>()

    private var transaction: WalletConnectTransaction? = null

    private val signHelperListener = object : ListQueuingHelper.Listener<BaseWalletConnectTransaction, ByteArray> {
        override fun onAllItemsDequeued(signedTransactions: List<ByteArray?>) {
            transaction?.run {
                _signResultLiveData.postValue(Success(session.id, requestId, signedTransactions))
            }
        }

        override fun onNextItemToBeDequeued(
            transaction: BaseWalletConnectTransaction,
            currentItemIndex: Int,
            totalItemCount: Int
        ) {
            val accountType = getSignerAccountType(transaction.signer.address?.decodedAddress)
            if (accountType == null) {
                signHelper.cacheDequeuedItem(null)
            } else {
                transaction.signTransaction(
                    accountDetail = accountType,
                    currentTransactionIndex = currentItemIndex,
                    totalTransactionCount = totalItemCount
                )
            }
        }
    }

    private val scanCallback = object : CustomScanCallback() {

        override fun onLedgerScanned(
            device: BluetoothDevice,
            currentTransactionIndex: Int?,
            totalTransactionCount: Int?
        ) {
            ledgerBleSearchManager.stop()
            currentScope.launch {
                signHelper.currentItem?.run {
                    val walletConnectTransactionOperation = WalletConnectTransactionOperation(device, this)
                    ledgerBleOperationManager.startLedgerOperation(
                        walletConnectTransactionOperation,
                        currentTransactionIndex,
                        totalTransactionCount
                    )
                }
            }
        }

        override fun onScanError(errorMessageResId: Int, titleResId: Int) {
            postResult(WalletConnectSignResult.LedgerScanFailed)
        }
    }

    private val operationManagerCollectorAction: (suspend (Event<LedgerBleResult>?) -> Unit) = { ledgerBleResultEvent ->
        ledgerBleResultEvent?.consume()?.run {
            if (transaction == null) return@run
            when (this) {
                is LedgerBleResult.LedgerWaitingForApproval -> {
                    LedgerWaitingForApproval(
                        ledgerName = bluetoothName,
                        currentTransactionIndex = currentTransactionIndex,
                        totalTransactionCount = totalTransactionCount,
                        isTransactionIndicatorVisible = totalTransactionCount != null &&
                            currentTransactionIndex != null &&
                            totalTransactionCount > 1
                    ).apply(::postResult)
                }
                is SignedTransactionResult -> signHelper.cacheDequeuedItem(transactionByteArray)
                is LedgerErrorResult -> postResult(Api(errorMessage))
                is AppErrorResult -> postResult(Defined(AnnotatedString(errorMessageId), titleResId))
                is OperationCancelledResult -> postResult(TransactionCancelled())
                else -> {
                    sendErrorLog("Unhandled else case in WalletConnectSignManager.operationManagerCollectorAction")
                }
            }
        }
    }

    fun setup(lifecycle: Lifecycle) {
        assignToLifecycle(lifecycle)
        setupLedgerOperationManager(lifecycle)
        signHelper.initListener(signHelperListener)
    }

    fun signTransaction(transaction: WalletConnectTransaction) {
        postResult(WalletConnectSignResult.Loading)
        this.transaction = transaction
        with(transaction) {
            when (val result = walletConnectSignValidator.canTransactionBeSigned(this)) {
                is WalletConnectSignResult.CanBeSigned -> {
                    val signableTransactions = transactionList.flatten().filter {
                        getSignerAccountType(it.signer.address?.decodedAddress) != null
                    }
                    signHelper.initItemsToBeEnqueued(signableTransactions)
                }
                is WalletConnectSignResult.Error -> postResult(result)
                else -> {
                    sendErrorLog("Unhandled else case in WalletConnectSignManager.signTransaction")
                }
            }
        }
    }

    private fun BaseWalletConnectTransaction.signTransaction(
        accountDetail: Account.Detail,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?,
        checkIfRekeyed: Boolean = true
    ) {
        if (checkIfRekeyed && isRekeyedToAnotherAccount()) {
            when (accountDetail) {
                is RekeyedAuth -> {
                    accountDetail.rekeyedAuthDetail[authAddress].let { rekeyedAuthDetail ->
                        if (rekeyedAuthDetail != null) {
                            signTransaction(
                                accountDetail = rekeyedAuthDetail,
                                checkIfRekeyed = false,
                                currentTransactionIndex = currentTransactionIndex,
                                totalTransactionCount = totalTransactionCount
                            )
                        } else {
                            processWithCheckingOtherAccounts(
                                currentTransactionIndex = currentTransactionIndex,
                                totalTransactionCount = totalTransactionCount
                            )
                        }
                    }
                }
                else -> {
                    processWithCheckingOtherAccounts(
                        currentTransactionIndex = currentTransactionIndex,
                        totalTransactionCount = totalTransactionCount
                    )
                }
            }
        } else {
            when (accountDetail) {
                is Ledger -> {
                    sendTransactionWithLedger(
                        ledgerDetail = accountDetail,
                        currentTransactionIndex = currentTransactionIndex,
                        totalTransactionCount = totalTransactionCount
                    )
                }
                is RekeyedAuth -> {
                    if (accountDetail.authDetail != null) {
                        signTransaction(
                            accountDetail = accountDetail.authDetail,
                            checkIfRekeyed = false,
                            currentTransactionIndex = currentTransactionIndex,
                            totalTransactionCount = totalTransactionCount
                        )
                    } else {
                        signHelper.cacheDequeuedItem(null)
                    }
                }
                is Standard -> signHelper.cacheDequeuedItem(decodedTransaction?.signTx(accountDetail.secretKey))
                else -> signHelper.cacheDequeuedItem(null)
            }
        }
    }

    private fun sendTransactionWithLedger(
        ledgerDetail: Ledger,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ) {
        val bluetoothAddress = ledgerDetail.bluetoothAddress
        val currentConnectedDevice = ledgerBleOperationManager.connectedBluetoothDevice
        if (currentConnectedDevice != null && currentConnectedDevice.address == bluetoothAddress) {
            sendCurrentTransaction(
                bluetoothDevice = currentConnectedDevice,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
        } else {
            searchForDevice(
                ledgerAddress = bluetoothAddress,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
        }
    }

    private fun sendCurrentTransaction(
        bluetoothDevice: BluetoothDevice,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ) {
        signHelper.currentItem?.run {
            val walletConnectTransactionOperation = WalletConnectTransactionOperation(bluetoothDevice, this)
            ledgerBleOperationManager.startLedgerOperation(
                newOperation = walletConnectTransactionOperation,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
        }
    }

    private fun searchForDevice(
        ledgerAddress: String,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ) {
        ledgerBleSearchManager.scan(
            newScanCallback = scanCallback,
            currentTransactionIndex = currentTransactionIndex,
            totalTransactionCount = totalTransactionCount,
            filteredAddress = ledgerAddress
        )
    }

    private fun BaseWalletConnectTransaction.processWithCheckingOtherAccounts(
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ) {
        when (
            val authAccountDetail = accountDetailUseCase.getCachedAccountDetail(authAddress.orEmpty())
                ?.data
                ?.account
                ?.detail
        ) {
            is Standard -> signHelper.cacheDequeuedItem(decodedTransaction?.signTx(authAccountDetail.secretKey))
            is Ledger -> {
                sendTransactionWithLedger(
                    ledgerDetail = authAccountDetail,
                    currentTransactionIndex = currentTransactionIndex,
                    totalTransactionCount = totalTransactionCount
                )
            }
            else -> signHelper.cacheDequeuedItem(null)
        }
    }

    private fun getSignerAccountType(signerAccountAddress: String?): Account.Detail? {
        if (signerAccountAddress.isNullOrBlank()) return null
        return accountDetailUseCase.getCachedAccountDetail(signerAccountAddress)?.data?.account?.detail
    }

    private fun postResult(walletConnectSignResult: WalletConnectSignResult) {
        _signResultLiveData.postValue(walletConnectSignResult)
    }

    private fun setupLedgerOperationManager(lifecycle: Lifecycle) {
        ledgerBleOperationManager.setup(lifecycle)
        lifecycle.coroutineScope.launch {
            ledgerBleOperationManager.ledgerBleResultFlow.collect { operationManagerCollectorAction.invoke(it) }
        }
    }

    override fun stopAllResources() {
        ledgerBleSearchManager.stop()
        signHelper.clearCachedData()
        transaction = null
    }

    fun manualStopAllResources() {
        this.stopAllResources()
        currentScope.coroutineContext.cancelChildren()
        ledgerBleOperationManager.manualStopAllProcess()
    }
}
