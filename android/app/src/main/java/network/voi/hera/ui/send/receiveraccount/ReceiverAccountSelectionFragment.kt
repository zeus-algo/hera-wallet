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

package network.voi.hera.ui.send.receiveraccount

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.TransactionBaseFragment
import network.voi.hera.databinding.FragmentReceiverAccountSelectionBinding
import network.voi.hera.models.AccountInformation
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.TargetUser
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.models.TransactionData
import network.voi.hera.ui.accountselection.AccountSelectionAdapter
import network.voi.hera.ui.send.receiveraccount.ReceiverAccountSelectionQrScannerFragment.Companion.ACCOUNT_ADDRESS_SCAN_RESULT_KEY
import network.voi.hera.utils.Event
import network.voi.hera.utils.Resource
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.extensions.hide
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.getTextFromClipboard
import network.voi.hera.utils.isValidAddress
import network.voi.hera.utils.startSavedStateListener
import network.voi.hera.utils.useSavedStateValue
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

// TODO: 18.03.2022 Use BaseAccountSelectionFragment after refactoring TransactionBaseFragment
@AndroidEntryPoint
class ReceiverAccountSelectionFragment : TransactionBaseFragment(R.layout.fragment_receiver_account_selection) {

    private val receiverAccountSelectionViewModel: ReceiverAccountSelectionViewModel by viewModels()

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconClick = ::navBack,
        startIconResId = R.drawable.ic_left_arrow,
        titleResId = R.string.select_the_receiver_account
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val binding by viewBinding(FragmentReceiverAccountSelectionBinding::bind)

    private val accountSelectionListener = object : AccountSelectionAdapter.Listener {
        override fun onAccountItemClick(publicKey: String) {
            receiverAccountSelectionViewModel.fetchToAccountInformation(publicKey)
        }

        override fun onContactItemClick(publicKey: String) {
            receiverAccountSelectionViewModel.fetchToAccountInformation(publicKey)
        }

        override fun onPasteItemClick(publicKey: String) {
            binding.searchView.text = publicKey
        }

        override fun onNftDomainItemClick(accountAddress: String, nftDomain: String, logoUrl: String?) {
            receiverAccountSelectionViewModel.fetchToAccountInformation(accountAddress, nftDomain, logoUrl)
        }
    }

    private val receiverAccountSelectionAdapter = AccountSelectionAdapter(accountSelectionListener)

    private val listCollector: suspend (List<BaseAccountSelectionListItem>?) -> Unit = { accountList ->
        receiverAccountSelectionAdapter.submitList(accountList)
        binding.screenStateView.isVisible = accountList?.isEmpty() == true
    }

    private val toAccountAddressValidationCollector: suspend (Event<Resource<String>>?) -> Unit = {
        it?.consume()?.use(
            onSuccess = { receiverAccountSelectionViewModel.fetchToAccountInformation(it) },
            onFailed = { handleError(it, binding.root) },
            onLoading = ::showProgress,
            onLoadingFinished = ::hideProgress
        )
    }

    private val toAccountInformationCollector: suspend (Event<Resource<AccountInformation>>?) -> Unit = {
        it?.consume()?.use(
            onSuccess = { receiverAccountSelectionViewModel.checkToAccountTransactionRequirements(it) },
            onFailed = { handleError(it, binding.root) },
            onLoading = ::showProgress,
            onLoadingFinished = ::hideProgress
        )
    }

    private val toAccountTransactionRequirementsCollector: suspend (Event<Resource<TargetUser>>?) -> Unit = {
        it?.consume()?.use(
            onSuccess = ::handleNextNavigation,
            onFailed = { handleError(it, binding.root) },
            onLoading = ::showProgress,
            onLoadingFinished = ::hideProgress
        )
    }

    private val windowFocusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
        if (hasFocus) updateLatestCopiedMessage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initUi()
    }

    override fun onStart() {
        super.onStart()
        initSavedStateListener()
    }

    private fun initSavedStateListener() {
        startSavedStateListener(R.id.receiverAccountSelectionFragment) {
            useSavedStateValue<String>(ACCOUNT_ADDRESS_SCAN_RESULT_KEY) { accountAddress ->
                binding.searchView.text = accountAddress
            }
        }
    }

    private fun initUi() {
        with(binding) {
            listRecyclerView.adapter = receiverAccountSelectionAdapter
            searchView.setOnTextChanged(::onTextChangeListener)
            searchView.setOnCustomButtonClick(::onScanQrClick)
            nextButton.setOnClickListener { onNextButtonClick() }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            receiverAccountSelectionViewModel.selectableAccountFlow,
            listCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            receiverAccountSelectionViewModel.toAccountAddressValidationFlow,
            toAccountAddressValidationCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            receiverAccountSelectionViewModel.toAccountInformationFlow,
            toAccountInformationCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            receiverAccountSelectionViewModel.toAccountTransactionRequirementsFlow,
            toAccountTransactionRequirementsCollector
        )
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view?.viewTreeObserver?.addOnWindowFocusChangeListener(windowFocusChangeListener)
        }
        updateLatestCopiedMessage()
    }

    private fun onScanQrClick() {
        nav(
            ReceiverAccountSelectionFragmentDirections
                .actionReceiverAccountSelectionFragmentToReceiverAccountSelectionQrScannerFragment()
        )
    }

    private fun onNextButtonClick() {
        receiverAccountSelectionViewModel.checkIsGivenAddressValid(binding.searchView.text)
    }

    private fun handleNextNavigation(targetUser: TargetUser) {
        val assetTransaction = receiverAccountSelectionViewModel.assetTransaction
        val selectedAccountCacheData = receiverAccountSelectionViewModel.getFromAccountCachedData() ?: return
        val selectedAsset = receiverAccountSelectionViewModel.getSelectedAssetInformation() ?: return
        val minBalanceCalculatedAmount = assetTransaction.amount
        nav(
            ReceiverAccountSelectionFragmentDirections
                .actionReceiverAccountSelectionFragmentToAssetTransferPreviewFragment(
                    TransactionData.Send(
                        selectedAccountCacheData,
                        minBalanceCalculatedAmount,
                        selectedAsset,
                        assetTransaction.note,
                        assetTransaction.xnote,
                        targetUser
                    )
                )
        )
    }

    private fun onTextChangeListener(charSequence: CharSequence?) {
        receiverAccountSelectionViewModel.onSearchQueryUpdate(charSequence.toString())
        binding.nextButton.isVisible = charSequence.toString().isValidAddress()
    }

    private fun showProgress() {
        binding.progressBar.root.show()
    }

    private fun hideProgress() {
        binding.progressBar.root.hide()
    }

    private fun updateLatestCopiedMessage() {
        receiverAccountSelectionViewModel.updateCopiedMessage(context?.getTextFromClipboard()?.toString())
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view?.viewTreeObserver?.removeOnWindowFocusChangeListener(windowFocusChangeListener)
        }
    }
}
