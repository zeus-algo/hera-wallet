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

package network.voi.hera.ui.send.senderaccount

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.TransactionBaseFragment
import network.voi.hera.databinding.FragmentSenderAccountSelectionBinding
import network.voi.hera.models.AccountInformation
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.SenderAccountSelectionPreview
import network.voi.hera.models.TargetUser
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.models.TransactionData
import network.voi.hera.ui.accountselection.AccountSelectionAdapter
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigInteger

@AndroidEntryPoint
class SenderAccountSelectionFragment : TransactionBaseFragment(R.layout.fragment_sender_account_selection) {

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.select_account,
        startIconResId = R.drawable.ic_close,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val senderAccountSelectionViewModel: SenderAccountSelectionViewModel by viewModels()

    private val binding by viewBinding(FragmentSenderAccountSelectionBinding::bind)

    private val listener = object : AccountSelectionAdapter.Listener {
        override fun onAccountItemClick(publicKey: String) {
            senderAccountSelectionViewModel.fetchFromAccountInformation(publicKey)
        }
    }

    private val senderAccountSelectionAdapter = AccountSelectionAdapter(listener)

    private val senderAccountSelectionPreviewCollector: suspend (SenderAccountSelectionPreview) -> Unit = {
        updateUiWithPreview(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTransactionTipsIfNeed()
        initObservers()
        binding.accountsToSendRecyclerView.adapter = senderAccountSelectionAdapter
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            flow = senderAccountSelectionViewModel.senderAccountSelectionPreviewFlow,
            collection = senderAccountSelectionPreviewCollector
        )
    }

    // If user enter Send Algo flow via deeplink or qr code, then we have to check asset transaction params then
    // we should navigate user to proper screen
    private fun handleNextNavigation(accountInformation: AccountInformation) {
        val assetTransaction = senderAccountSelectionViewModel.assetTransaction.copy(
            senderAddress = accountInformation.address
        )
        // TODO: 26.08.2022 Remove all those checks from Fragment, and handle them in usecase, only call events here
        when {
            assetTransaction.assetId == -1L -> {
                SenderAccountSelectionFragmentDirections
                    .actionSenderAccountSelectionFragmentToAssetSelectionFragment(assetTransaction)
            }
            assetTransaction.amount == BigInteger.ZERO -> {
                SenderAccountSelectionFragmentDirections
                    .actionSenderAccountSelectionFragmentToAssetTransferAmountFragment(assetTransaction)
            }
            assetTransaction.receiverUser == null -> {
                SenderAccountSelectionFragmentDirections
                    .actionSenderAccountSelectionFragmentToReceiverAccountSelectionFragment(assetTransaction)
            }
            else -> {
                val selectedAccountCacheData = senderAccountSelectionViewModel
                    .getAccountCachedData(accountInformation.address) ?: return
                val selectedAsset = senderAccountSelectionViewModel
                    .getAssetInformation(accountInformation.address) ?: return
                val targetUser = TargetUser(
                    assetTransaction.receiverUser,
                    assetTransaction.receiverUser.publicKey,
                    selectedAccountCacheData
                )
                SenderAccountSelectionFragmentDirections
                    .actionSenderAccountSelectionFragmentToAssetTransferPreviewFragment(
                        TransactionData.Send(
                            selectedAccountCacheData,
                            assetTransaction.amount,
                            selectedAsset,
                            assetTransaction.note,
                            assetTransaction.xnote,
                            targetUser
                        )
                    )
            }
        }.apply { nav(this) }
    }

    private fun updateUiWithPreview(preview: SenderAccountSelectionPreview) {
        with(preview) {
            binding.progressBar.root.isVisible = isLoading
            senderAccountSelectionAdapter.submitList(accountList)
            binding.screenStateView.isVisible = isEmptyStateVisible

            fromAccountInformationSuccessEvent?.consume()?.let { handleNextNavigation(it) }
            fromAccountInformationErrorEvent?.consume()?.let { handleError(it.getAsResourceError(), binding.root) }
        }
    }

    private fun showTransactionTipsIfNeed() {
        if (senderAccountSelectionViewModel.shouldShowTransactionTips()) {
            nav(
                SenderAccountSelectionFragmentDirections
                    .actionSenderAccountSelectionFragmentToTransactionTipsBottomSheet()
            )
        }
    }
}
