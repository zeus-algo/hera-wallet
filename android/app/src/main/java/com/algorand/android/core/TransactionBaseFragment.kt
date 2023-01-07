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

package com.algorand.android.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import com.algorand.android.customviews.LedgerLoadingDialog
import com.algorand.android.models.Account
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.SignedTransactionDetail
import com.algorand.android.models.TransactionData
import com.algorand.android.models.TransactionManagerResult
import com.algorand.android.utils.AccountCacheManager
import com.algorand.android.utils.BLE_OPEN_REQUEST_CODE
import com.algorand.android.utils.Event
import com.algorand.android.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.algorand.android.utils.Resource
import com.algorand.android.utils.getXmlStyledString
import com.algorand.android.utils.isBluetoothEnabled
import com.algorand.android.utils.sendErrorLog
import com.algorand.android.utils.showAlertDialog
import com.algorand.android.utils.showSnackbar
import com.algorand.android.utils.showWithStateCheck
import javax.inject.Inject

abstract class TransactionBaseFragment(
    @LayoutRes layoutResId: Int
) : DaggerBaseFragment(layoutResId), LedgerLoadingDialog.Listener {

    @Inject
    lateinit var transactionManager: TransactionManager

    @Inject
    lateinit var accountCacheManager: AccountCacheManager

    // TODO: 13.06.2022 Remove bleWaitingTransactionData and use a list instance for both txn type
    private var bleWaitingTransactionData: TransactionData? = null
    private var bleWaitingGroupTransactionData: List<TransactionData>? = null
    private var ledgerLoadingDialog: LedgerLoadingDialog? = null

    open val transactionFragmentListener: TransactionFragmentListener? = null

    private val transactionManagerObserver = Observer<Event<TransactionManagerResult>?> { event ->
        event?.consume()?.run {
            when (this) {
                is TransactionManagerResult.Success -> {
                    hideLoading()
                    transactionFragmentListener?.onSignTransactionFinished(this.signedTransactionDetail)
                }
                is TransactionManagerResult.Error.GlobalWarningError -> {
                    showTransactionError(this)
                }
                is TransactionManagerResult.Error.SnackbarError.Retry -> {
//                    Currently, we are showing this kind of error in case of ASA  adding failure. Since we are are
//                    handling this operation in [MainActivity], no need to check it here. But as a fallback behaviour,
//                    we will display [CustomSnackbar] here as well.
                    onCustomBottomSheetOpened(this)
                }
                TransactionManagerResult.Loading -> {
                    transactionFragmentListener?.onSignTransactionLoading()
                }
                is TransactionManagerResult.LedgerWaitingForApproval -> {
                    showLedgerLoading(bluetoothName)
                }
                TransactionManagerResult.LedgerScanFailed -> {
                    hideLoading()
                    navigateToConnectionIssueBottomSheet()
                }
                TransactionManagerResult.LedgerOperationCanceled -> {
                    onSignTransactionCancelledByLedger()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        transactionManager.setup(lifecycle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionManager.transactionManagerResultLiveData.observe(viewLifecycleOwner, transactionManagerObserver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                sendWaitingTransactionData()
            } else {
                permissionDeniedOnTransactionData(R.string.error_location_message, R.string.error_permission_title)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BLE_OPEN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                sendWaitingTransactionData()
            } else {
                permissionDeniedOnTransactionData(R.string.error_bluetooth_message, R.string.error_bluetooth_title)
            }
        }
    }

    internal fun permissionDeniedOnTransactionData(@StringRes errorResId: Int, @StringRes titleResId: Int) {
        bleWaitingTransactionData = null
        bleWaitingGroupTransactionData = null
        showTransactionError(
            TransactionManagerResult.Error.GlobalWarningError.Defined(
                description = AnnotatedString(errorResId),
                titleResId = titleResId
            )
        )
    }

    private fun sendWaitingTransactionData() {
        bleWaitingTransactionData?.run {
            sendTransaction(this)
        }
        bleWaitingGroupTransactionData?.run {
            sendGroupTransaction(this)
        }
    }

    private fun hideLoading() {
        transactionFragmentListener?.onSignTransactionLoadingFinished()
        ledgerLoadingDialog?.dismissAllowingStateLoss()
        ledgerLoadingDialog = null
    }

    private fun showLedgerLoading(ledgerName: String?) {
        if (ledgerLoadingDialog == null) {
            ledgerLoadingDialog = LedgerLoadingDialog.createLedgerLoadingDialog(ledgerName)
            ledgerLoadingDialog?.showWithStateCheck(childFragmentManager)
        }
    }

    private fun navigateToConnectionIssueBottomSheet() {
        nav(HomeNavigationDirections.actionGlobalLedgerConnectionIssueBottomSheet())
    }

    fun sendTransaction(transactionData: TransactionData) {
        val accountCacheData = transactionData.accountCacheData

        when (accountCacheData.account.type) {
            Account.Type.LEDGER, Account.Type.REKEYED, Account.Type.REKEYED_AUTH -> {
                if (isBluetoothEnabled().not()) {
                    bleWaitingTransactionData = transactionData
                    return
                }
            }
            else -> {
                sendErrorLog("Unhandled else case in TransactionBaseFragment.sendTransaction")
            }
        }
        transactionManager.initSigningTransactions(false, transactionData)
    }

    fun sendGroupTransaction(transactionDataList: List<TransactionData>) {
        if (transactionDataList.all {
                with(it.accountCacheData.account.type) {
                    this == Account.Type.LEDGER || this == Account.Type.REKEYED || this == Account.Type.REKEYED_AUTH
                }
            }
        ) {
            if (isBluetoothEnabled().not()) {
                bleWaitingGroupTransactionData = transactionDataList
                return
            }
        }
        transactionManager.initSigningTransactions(
            isGroupTransaction = true,
            transactionData = transactionDataList.toTypedArray()
        )
    }

    protected fun showTransactionError(error: TransactionManagerResult.Error.GlobalWarningError) {
        hideLoading()
        val (title, errorMessage) = error.getMessage(requireContext())
        showGlobalError(errorMessage, title)
        transactionManager.manualStopAllResources()
    }

    protected open fun onCustomBottomSheetOpened(transactionResult: TransactionManagerResult.Error.SnackbarError) {}

    protected fun handleError(error: Resource.Error, viewGroup: ViewGroup) {
        when (error) {
            is Resource.Error.Annotated -> {
                showSnackbar(context?.getXmlStyledString(error.annotatedString).toString(), viewGroup)
            }
            is Resource.Error.Warning -> {
                context?.showAlertDialog(
                    getString(error.titleRes),
                    context?.getXmlStyledString(error.annotatedString).toString()
                )
            }
            is Resource.Error.Navigation -> {
                nav(error.navDirections)
            }
            is Resource.Error.GlobalWarning -> {
                val titleString = error.titleRes?.let { getString(it) }
                context?.run { showGlobalError(error.parse(this), titleString) }
            }
            else -> {
                context?.run { showGlobalError(error.parse(this)) }
            }
        }
    }

    protected open fun onSignTransactionCancelledByLedger() {
        val transactionManagerResult = TransactionManagerResult.Error.GlobalWarningError.Defined(
            description = AnnotatedString(R.string.error_cancelled_message),
            titleResId = R.string.error_cancelled_title
        )
        showTransactionError(transactionManagerResult)
    }

    override fun onLedgerLoadingCancelled(shouldStopResources: Boolean) {
        hideLoading()
        transactionManager.manualStopAllResources()
    }

    interface TransactionFragmentListener {
        fun onSignTransactionLoadingFinished() {
            // This blank line is here to disable mandatory overriding operation
        }

        fun onSignTransactionLoading() {
            // This blank line is here to disable mandatory overriding operation
        }

        fun onSignTransactionFinished(signedTransactionDetail: SignedTransactionDetail)
    }
}
