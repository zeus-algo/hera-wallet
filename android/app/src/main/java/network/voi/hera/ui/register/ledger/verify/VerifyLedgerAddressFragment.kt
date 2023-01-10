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

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import network.voi.hera.R
import network.voi.hera.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentVerifyLedgerAddressBinding
import network.voi.hera.ledger.LedgerBleOperationManager
import network.voi.hera.ledger.operations.VerifyAddressOperation
import network.voi.hera.models.Account
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.LedgerBleResult
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.ui.register.ledger.PairLedgerNavigationViewModel
import network.voi.hera.utils.Event
import network.voi.hera.utils.analytics.CreationType
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.sendErrorLog
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyLedgerAddressFragment : DaggerBaseFragment(R.layout.fragment_verify_ledger_address) {

    @Inject
    lateinit var ledgerBleOperationManager: LedgerBleOperationManager

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val binding by viewBinding(FragmentVerifyLedgerAddressBinding::bind)

    private val adapter = VerifiableLedgerAddressesAdapter()

    private val verifyLedgerAddressViewModel: VerifyLedgerAddressViewModel by viewModels()

    private val pairLedgerNavigationViewModel: PairLedgerNavigationViewModel by navGraphViewModels(
        R.id.pairLedgerNavigation
    ) {
        defaultViewModelProviderFactory
    }

    // <editor-fold defaultstate="collapsed" desc="Observers">

    private val listObserver = Observer<List<VerifyLedgerAddressListItem>> { list ->
        adapter.submitList(list)
    }

    private val isAllOperationDoneObserver = Observer<Event<Boolean>?> { isAllOperationDoneEvent ->
        isAllOperationDoneEvent?.consume()?.let { isAllOperationDone ->
            binding.confirmationButton.isVisible = isAllOperationDone
        }
    }

    private val ledgerResultCollector: suspend (Event<LedgerBleResult>?) -> Unit = { ledgerBleResultEvent ->
        ledgerBleResultEvent?.consume()?.let { ledgerBleResult ->
            when (ledgerBleResult) {
                is LedgerBleResult.OnLedgerDisconnected -> {
                    retryCurrentOperation()
                }
                is LedgerBleResult.AppErrorResult -> {
                    showGlobalError(
                        errorMessage = getString(ledgerBleResult.errorMessageId),
                        title = getString(ledgerBleResult.titleResId)
                    )
                    retryCurrentOperation()
                }
                is LedgerBleResult.LedgerErrorResult -> {
                    showGlobalError(errorMessage = ledgerBleResult.errorMessage)
                    retryCurrentOperation()
                }
                is LedgerBleResult.OperationCancelledResult -> {
                    verifyLedgerAddressViewModel.onCurrentOperationDone(isVerified = false)
                }
                is LedgerBleResult.VerifyPublicKeyResult -> {
                    verifyLedgerAddressViewModel.onCurrentOperationDone(isVerified = ledgerBleResult.isVerified)
                }
                else -> {
                    sendErrorLog("Unhandled else case in ledgerResultCollector")
                }
            }
        }
    }

    // </editor-fold>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLedgerBleOperationManager()
        setupViewModel()
        setupLedgerAddressesRecyclerView()
        initObservers()
        binding.confirmationButton.setOnClickListener { onConfirmationClick() }
    }

    private fun setupLedgerBleOperationManager() {
        ledgerBleOperationManager.setup(viewLifecycleOwner.lifecycle)
    }

    private fun setupLedgerAddressesRecyclerView() {
        binding.ledgerAddressesRecyclerView.adapter = adapter
    }

    private fun setupViewModel() {
        verifyLedgerAddressViewModel.createListAuthLedgerAccounts(
            authLedgerAccounts = pairLedgerNavigationViewModel.getSelectedAuthAccounts()
        )
    }

    private fun startVerifyOperation(account: Account?) {
        if (account == null) {
            return
        }
        val currentOperatedLedger = pairLedgerNavigationViewModel.pairedLedger
        if (currentOperatedLedger == null) {
            sendErrorLog("Ledger is not found while operating startVerifyOperation function.")
            return
        }
        if (account.detail is Account.Detail.Ledger) {
            ledgerBleOperationManager.startLedgerOperation(
                VerifyAddressOperation(currentOperatedLedger, account.detail.positionInLedger, account.address)
            )
        } else {
            sendErrorLog("Other than Ledger Account is in the verify operation.")
        }
    }

    private fun retryCurrentOperation() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(RETRY_DELAY)
            startVerifyOperation(verifyLedgerAddressViewModel.awaitingLedgerAccount)
        }
    }

    private fun initObservers() {
        verifyLedgerAddressViewModel.currentLedgerAddressesListLiveData.observe(viewLifecycleOwner, listObserver)

        verifyLedgerAddressViewModel.isVerifyOperationsDoneLiveData.observe(
            viewLifecycleOwner,
            isAllOperationDoneObserver
        )

        viewLifecycleOwner.collectLatestOnLifecycle(
            ledgerBleOperationManager.ledgerBleResultFlow,
            ledgerResultCollector
        )

        verifyLedgerAddressViewModel.awaitingLedgerAccountLiveData.observe(viewLifecycleOwner) {
            startVerifyOperation(it)
        }
    }

    private fun onConfirmationClick() {
        val selectedVerifiedAccounts = verifyLedgerAddressViewModel.getSelectedVerifiedAccounts(
            pairLedgerNavigationViewModel.selectedAccounts
        )
        selectedVerifiedAccounts.forEach { selectedAccount ->
            val creationType = if (selectedAccount.type == Account.Type.REKEYED) {
                CreationType.REKEYED
            } else {
                CreationType.LEDGER
            }
            verifyLedgerAddressViewModel.addNewAccount(selectedAccount, creationType)
        }
        nav(
            VerifyLedgerAddressFragmentDirections.actionVerifyLedgerAddressFragmentToVerifyLedgerInfoFragment(
                selectedVerifiedAccounts.size
            )
        )
    }

    companion object {
        private const val RETRY_DELAY = 1000L
    }
}
