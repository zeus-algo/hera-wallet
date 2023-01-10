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

package network.voi.hera.modules.transaction.signmanager

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import network.voi.hera.ledger.CustomScanCallback
import network.voi.hera.ledger.LedgerBleOperationManager
import network.voi.hera.ledger.LedgerBleSearchManager
import network.voi.hera.ledger.operations.ExternalTransaction
import network.voi.hera.ledger.operations.ExternalTransactionOperation
import network.voi.hera.models.Account
import network.voi.hera.models.Account.Detail.Ledger
import network.voi.hera.models.Account.Detail.RekeyedAuth
import network.voi.hera.models.Account.Detail.Standard
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.LedgerBleResult
import network.voi.hera.models.LedgerBleResult.AppErrorResult
import network.voi.hera.models.LedgerBleResult.LedgerErrorResult
import network.voi.hera.models.LedgerBleResult.OperationCancelledResult
import network.voi.hera.models.LedgerBleResult.SignedTransactionResult
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.NotInitialized
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.Event
import network.voi.hera.utils.LifecycleScopedCoroutineOwner
import network.voi.hera.utils.ListQueuingHelper
import network.voi.hera.utils.sendErrorLog
import network.voi.hera.utils.signTx
import javax.inject.Inject
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class ExternalTransactionSignManager<TRANSACTION : ExternalTransaction> @Inject constructor(
    private val ledgerBleSearchManager: LedgerBleSearchManager,
    private val ledgerBleOperationManager: LedgerBleOperationManager,
    private val externalTransactionQueuingHelper: ExternalTransactionQueuingHelper,
    private val accountDetailUseCase: AccountDetailUseCase
) : LifecycleScopedCoroutineOwner() {

    protected val signResultFlow: StateFlow<ExternalTransactionSignResult>
        get() = _signResultFlow
    private val _signResultFlow = MutableStateFlow<ExternalTransactionSignResult>(NotInitialized)

    protected var transaction: List<TRANSACTION>? = null

    private val signHelperListener = object : ListQueuingHelper.Listener<ExternalTransaction, ByteArray> {
        override fun onAllItemsDequeued(signedTransactions: List<ByteArray?>) {
            transaction?.run {
                _signResultFlow.value = ExternalTransactionSignResult.Success(this)
            }
        }

        override fun onNextItemToBeDequeued(
            transaction: ExternalTransaction,
            currentItemIndex: Int,
            totalItemCount: Int
        ) {
            val accountType = getSignerAccountType(transaction.accountAddress)
            if (accountType == null) {
                externalTransactionQueuingHelper.cacheDequeuedItem(null)
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
                externalTransactionQueuingHelper.currentItem?.run {
                    val swapTransactionOperation = ExternalTransactionOperation(device, this)
                    ledgerBleOperationManager.startLedgerOperation(
                        swapTransactionOperation,
                        currentTransactionIndex,
                        totalTransactionCount
                    )
                }
            }
        }

        override fun onScanError(errorMessageResId: Int, titleResId: Int) {
            postResult(ExternalTransactionSignResult.LedgerScanFailed)
        }
    }

    private val operationManagerCollectorAction: (suspend (Event<LedgerBleResult>?) -> Unit) = { ledgerBleResultEvent ->
        ledgerBleResultEvent?.consume()?.let { ledgerBleResult ->
            if (transaction == null) return@let
            when (ledgerBleResult) {
                is LedgerBleResult.LedgerWaitingForApproval -> {
                    ExternalTransactionSignResult.LedgerWaitingForApproval(
                        ledgerName = ledgerBleResult.bluetoothName,
                        currentTransactionIndex = ledgerBleResult.currentTransactionIndex,
                        totalTransactionCount = ledgerBleResult.totalTransactionCount,
                        isTransactionIndicatorVisible = ledgerBleResult.totalTransactionCount != null &&
                            ledgerBleResult.currentTransactionIndex != null &&
                            ledgerBleResult.totalTransactionCount > 1
                    ).apply(::postResult)
                }
                is SignedTransactionResult -> {
                    externalTransactionQueuingHelper.currentItem?.run {
                        onTransactionSigned(this, ledgerBleResult.transactionByteArray)
                    }
                }
                is LedgerErrorResult -> {
                    postResult(ExternalTransactionSignResult.Error.Api(ledgerBleResult.errorMessage))
                }
                is AppErrorResult -> postResult(
                    ExternalTransactionSignResult.Error.Defined(
                        AnnotatedString(ledgerBleResult.errorMessageId),
                        ledgerBleResult.titleResId
                    )
                )
                is OperationCancelledResult -> postResult(ExternalTransactionSignResult.TransactionCancelled())
                else -> {
                    sendErrorLog("Unhandled else case in WalletConnectSignManager.operationManagerCollectorAction")
                }
            }
        }
    }

    fun setup(lifecycle: Lifecycle) {
        assignToLifecycle(lifecycle)
        setupLedgerOperationManager(lifecycle)
        externalTransactionQueuingHelper.initListener(signHelperListener)
    }

    open fun signTransaction(transaction: List<TRANSACTION>) {
        postResult(ExternalTransactionSignResult.Loading)
        this.transaction = transaction
        externalTransactionQueuingHelper.initItemsToBeEnqueued(transaction)
    }

    private fun ExternalTransaction.signTransaction(
        accountDetail: Account.Detail,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?,
        checkIfRekeyed: Boolean = true
    ) {
        if (checkIfRekeyed && isRekeyedToAnotherAccount) {
            when (accountDetail) {
                is RekeyedAuth -> {
                    accountDetail.rekeyedAuthDetail[accountAuthAddress].let { rekeyedAuthDetail ->
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
                        externalTransactionQueuingHelper.cacheDequeuedItem(null)
                    }
                }
                is Standard -> {
                    signTransactionWithSecretKey(this, accountDetail.secretKey)
                }
                else -> externalTransactionQueuingHelper.cacheDequeuedItem(null)
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
        externalTransactionQueuingHelper.currentItem?.run {
            val externalTransactionOperation = ExternalTransactionOperation(bluetoothDevice, this)
            ledgerBleOperationManager.startLedgerOperation(
                newOperation = externalTransactionOperation,
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

    private fun ExternalTransaction.processWithCheckingOtherAccounts(
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ) {
        when (
            val authAccountDetail = accountDetailUseCase.getCachedAccountDetail(accountAuthAddress.orEmpty())
                ?.data
                ?.account
                ?.detail
        ) {
            is Standard -> {
                signTransactionWithSecretKey(this, authAccountDetail.secretKey)
            }
            is Ledger -> {
                sendTransactionWithLedger(
                    ledgerDetail = authAccountDetail,
                    currentTransactionIndex = currentTransactionIndex,
                    totalTransactionCount = totalTransactionCount
                )
            }
            else -> externalTransactionQueuingHelper.cacheDequeuedItem(null)
        }
    }

    private fun signTransactionWithSecretKey(transaction: ExternalTransaction, secretKey: ByteArray) {
        val signedTransaction = transaction.transactionByteArray?.signTx(secretKey)
        onTransactionSigned(transaction, signedTransaction)
    }

    protected open fun onTransactionSigned(transaction: ExternalTransaction, signedTransaction: ByteArray?) {
        externalTransactionQueuingHelper.cacheDequeuedItem(signedTransaction)
    }

    private fun getSignerAccountType(signerAccountAddress: String?): Account.Detail? {
        if (signerAccountAddress.isNullOrBlank()) return null
        return accountDetailUseCase.getCachedAccountDetail(signerAccountAddress)?.data?.account?.detail
    }

    private fun postResult(transactionSignResult: ExternalTransactionSignResult) {
        _signResultFlow.value = transactionSignResult
    }

    private fun setupLedgerOperationManager(lifecycle: Lifecycle) {
        ledgerBleOperationManager.setup(lifecycle)
        lifecycle.coroutineScope.launch {
            ledgerBleOperationManager.ledgerBleResultFlow.collect { operationManagerCollectorAction.invoke(it) }
        }
    }

    override fun stopAllResources() {
        ledgerBleSearchManager.stop()
        externalTransactionQueuingHelper.clearCachedData()
        transaction = null
    }

    fun manualStopAllResources() {
        this.stopAllResources()
        currentScope.coroutineContext.cancelChildren()
        ledgerBleOperationManager.manualStopAllProcess()
    }
}
