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
 */

package com.algorand.android.nft.ui.nftsend

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.algorand.android.HomeNavigationDirections
import com.algorand.android.R
import com.algorand.android.core.TransactionBaseFragment
import com.algorand.android.databinding.FragmentCollectibleSendBinding
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.CollectibleSendApproveResult
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.models.SignedTransactionDetail
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.nft.ui.model.CollectibleReceiverSelectionResult
import com.algorand.android.nft.ui.model.CollectibleSendPreview
import com.algorand.android.nft.ui.model.RequestOptInConfirmationArgs
import com.algorand.android.nft.ui.nftapprovetransaction.CollectibleTransactionApproveBottomSheet.Companion.COLLECTIBLE_TXN_APPROVE_KEY
import com.algorand.android.nft.ui.nftsend.CollectibleReceiverSelectionFragment.Companion.COLLECTIBLE_RECEIVER_ACCOUNT_SELECTION_RESULT_KEY
import com.algorand.android.nft.ui.nftsend.CollectibleReceiverSelectionFragment.Companion.COLLECTIBLE_RECEIVER_NFT_DOMAIN_SELECTION_RESULT_KEY
import com.algorand.android.nft.ui.nftsend.CollectibleSendQrScannerFragment.Companion.ACCOUNT_ADDRESS_SCAN_RESULT_KEY
import com.algorand.android.utils.SingleButtonBottomSheet
import com.algorand.android.utils.extensions.collectOnLifecycle
import com.algorand.android.utils.extensions.hide
import com.algorand.android.utils.extensions.show
import com.algorand.android.utils.isValidAddress
import com.algorand.android.utils.startSavedStateListener
import com.algorand.android.utils.useFragmentResultListenerValue
import com.algorand.android.utils.useSavedStateValue
import com.algorand.android.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectibleSendFragment : TransactionBaseFragment(R.layout.fragment_collectible_send) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val binding by viewBinding(FragmentCollectibleSendBinding::bind)

    private val collectibleSendViewModel by viewModels<CollectibleSendViewModel>()

    private val onAlgorandAddressInputChangeListener: (String) -> Unit = { address ->
        collectibleSendViewModel.updateSelectedAccountAddress(address)
    }

    private val onScanQrClick: () -> Unit = {
        nav(CollectibleSendFragmentDirections.actionCollectibleSendFragmentToCollectibleSendQrScannerFragment())
    }

    private val collectibleSendPreviewCollector: suspend (CollectibleSendPreview?) -> Unit = {
        if (it != null) updateUi(it)
    }

    private val selectedAddressCollector: suspend (String) -> Unit = { address ->
        with(binding) {
            transferButton.isEnabled = address.isValidAddress()
            algorandWalletAddressTextInputLayout.text = address
        }
    }

    private val onContactClick: () -> Unit = {
        nav(CollectibleSendFragmentDirections.actionCollectibleSendFragmentToCollectibleReceiverSelectionFragment())
    }

    override val transactionFragmentListener = object : TransactionFragmentListener {
        override fun onSignTransactionFinished(signedTransactionDetail: SignedTransactionDetail) {
            if (signedTransactionDetail is SignedTransactionDetail.Send) {
                collectibleSendViewModel.sendSignedTransaction(signedTransactionDetail)
            }
        }

        override fun onSignTransactionLoading() {
            binding.progressBar.loadingProgressBar.show()
        }

        override fun onSignTransactionLoadingFinished() {
            binding.progressBar.loadingProgressBar.hide()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        initSavedStateListeners()
    }

    private fun initObservers() {
        viewLifecycleOwner.collectOnLifecycle(
            collectibleSendViewModel.collectibleSendPreview,
            collectibleSendPreviewCollector
        )
        viewLifecycleOwner.collectOnLifecycle(
            collectibleSendViewModel.selectedAccountAddressFlow,
            selectedAddressCollector
        )
    }

    private fun initSavedStateListeners() {
        startSavedStateListener(R.id.collectibleSendFragment) {
            useSavedStateValue<String>(ACCOUNT_ADDRESS_SCAN_RESULT_KEY) { accountAddress ->
                collectibleSendViewModel.updateSelectedAccountAddress(accountAddress)
            }
            useSavedStateValue<CollectibleSendApproveResult>(COLLECTIBLE_TXN_APPROVE_KEY) { result ->
                if (result.isApproved) {
                    if (result.isOptOutChecked) {
                        collectibleSendViewModel.createSendAndRemoveAssetTransactionData()?.let { transactionData ->
                            sendTransaction(transactionData)
                        }
                    } else {
                        collectibleSendViewModel.createSendTransactionData()?.let { transactionData ->
                            sendTransaction(transactionData)
                        }
                    }
                }
            }
            useSavedStateValue<Boolean>(SingleButtonBottomSheet.CLOSE_KEY) { isRetryClicked ->
                if (isRetryClicked) collectibleSendViewModel.retrySendingTransaction()
            }
            useFragmentResultListenerValue<CollectibleReceiverSelectionResult.AccountSelectionResult>(
                COLLECTIBLE_RECEIVER_ACCOUNT_SELECTION_RESULT_KEY
            ) {
                collectibleSendViewModel.updateSelectedAccountAddress(it.accountAddress)
            }
            useFragmentResultListenerValue<CollectibleReceiverSelectionResult.NftDomainSelectionResult>(
                COLLECTIBLE_RECEIVER_NFT_DOMAIN_SELECTION_RESULT_KEY
            ) {
                with(collectibleSendViewModel) {
                    updateSelectedAccountAddress(it.accountAddress)
                    updateNftDomainInformation(Pair(it.nftDomainName, it.nftDomainLogoUrl))
                }
            }
        }
    }

    private fun updateUi(collectibleSendPreview: CollectibleSendPreview) {
        with(collectibleSendPreview) {
            with(binding) {
                collectibleMediaPager.submitList(collectibleMedias)
                progressBar.loadingProgressBar.isVisible = isLoadingVisible
                collectibleCollectionName.apply {
                    text = collectionName
                    isVisible = isCollectionNameVisible
                }
                collectibleName.apply {
                    text = collectibleSendPreview.collectibleName
                    isVisible = isCollectibleNameVisible
                }
            }
            navigateToOptInEvent?.consume()?.run {
                val publicKey = collectibleSendViewModel.getSelectedAddress()
                navToRequestOptInBottomSheet(collectibleName, collectibleId, publicKey)
            }
            navigateToApprovalBottomSheetEvent?.consume()?.run { navToApproveTransactionBottomSheet() }
            globalErrorTextEvent?.consume()?.run { if (!isNullOrBlank()) showGlobalError(this) }
            navigateToTransactionCompletedEvent?.consume()?.run { navToTransferConfirmedFragment() }
            showNetworkErrorEvent?.consume()?.run { navToNetworkErrorRetryBottomSheet() }
            showCollectibleAlreadyOwnedErrorEvent?.consume()?.run { showCollectibleAlreadyOwnedError() }
            checkIfSelectedAccountReceiveCollectibleEvent?.consume()?.run {
                collectibleSendViewModel.checkIfSelectedAccountReceiveCollectible()
            }
        }
    }

    private fun navToNetworkErrorRetryBottomSheet() {
        nav(
            HomeNavigationDirections.actionGlobalSingleButtonBottomSheet(
                titleAnnotatedString = AnnotatedString(R.string.your_nft_transfer_has_failed),
                descriptionAnnotatedString = AnnotatedString(R.string.please_try_again),
                drawableResId = R.drawable.ic_error,
                drawableTintResId = R.color.negative,
                isResultNeeded = true
            )
        )
    }

    private fun navToTransferConfirmedFragment() {
        nav(CollectibleSendFragmentDirections.actionCollectibleSendFragmentToCollectibleTransferConfirmedFragment())
    }

    private fun navToRequestOptInBottomSheet(collectibleName: String?, collectibleId: Long, receiverPublicKey: String) {
        nav(
            CollectibleSendFragmentDirections.actionCollectibleSendFragmentToRequestOptInConfirmationNavigation(
                RequestOptInConfirmationArgs(
                    senderPublicKey = collectibleSendViewModel.accountAddress,
                    receiverPublicKey = receiverPublicKey,
                    assetId = collectibleId,
                    assetName = collectibleName
                )
            )
        )
    }

    private fun navToApproveTransactionBottomSheet() {
        val transactionData = collectibleSendViewModel.createSendTransactionData() ?: return
        nav(
            CollectibleSendFragmentDirections.actionCollectibleSendFragmentToCollectibleTransactionApproveBottomSheet(
                senderPublicKey = transactionData.accountCacheData.account.address,
                receiverPublicKey = transactionData.targetUser.publicKey,
                fee = (transactionData.calculatedFee ?: transactionData.projectedFee).toFloat(),
                nftId = collectibleSendViewModel.nftId,
                nftDomainLogoUrl = collectibleSendViewModel.nftDomainAddressServiceLogoPair?.second,
                nftDomainName = collectibleSendViewModel.nftDomainAddressServiceLogoPair?.first
            )
        )
    }

    private fun initUi() {
        initSendYourNftToolbar()
        initAddressInputLayout()
        initTransferButton()
    }

    private fun initTransferButton() {
        binding.transferButton.setOnClickListener {
            collectibleSendViewModel.checkIfSenderAndReceiverAccountSame()
        }
    }

    private fun initSendYourNftToolbar() {
        val toolbarConfiguration = ToolbarConfiguration(
            startIconResId = R.drawable.ic_close,
            titleResId = R.string.send_your_nft,
            startIconClick = ::navBack,
            backgroundColor = R.color.background
        )
        binding.bottomSheetCustomToolbar.configure(toolbarConfiguration)
    }

    private fun initAddressInputLayout() {
        with(binding.algorandWalletAddressTextInputLayout) {
            setOnTextChangeListener(onAlgorandAddressInputChangeListener)
            addTrailingIcon(R.drawable.ic_contacts, onContactClick)
            addTrailingIcon(R.drawable.ic_qr_scan, onScanQrClick)
        }
    }

    private fun showCollectibleAlreadyOwnedError() {
        showGlobalError(getString(R.string.you_already_own))
    }
}
