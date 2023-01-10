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

package network.voi.hera.modules.swap.confirmswap.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.customviews.LedgerLoadingDialog
import network.voi.hera.customviews.SwapAssetInputView
import network.voi.hera.databinding.FragmentConfirmSwapBinding
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.swap.confirmswap.domain.model.SwapQuoteTransaction
import network.voi.hera.modules.swap.confirmswap.ui.model.ConfirmSwapPreview
import network.voi.hera.modules.swap.ledger.signwithledger.ui.model.LedgerDialogPayload
import network.voi.hera.modules.swap.slippagetolerance.ui.SlippageToleranceBottomSheet.Companion.CHECKED_SLIPPAGE_TOLERANCE_KEY
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AccountIconDrawable
import network.voi.hera.utils.ErrorResource
import network.voi.hera.utils.Event
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.showWithStateCheck
import network.voi.hera.utils.useFragmentResultListenerValue
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ConfirmSwapFragment : BaseFragment(R.layout.fragment_confirm_swap), LedgerLoadingDialog.Listener {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack,
        titleResId = R.string.confirm_swap
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val binding by viewBinding(FragmentConfirmSwapBinding::bind)

    private val confirmSwapViewModel by viewModels<ConfirmSwapViewModel>()

    private var ledgerLoadingDialog: LedgerLoadingDialog? = null

    private val confirmSwapPreviewCollector: suspend (ConfirmSwapPreview) -> Unit = { preview ->
        initConfirmSwapPreview(preview)
    }

    private val isLoadingVisibleCollector: suspend (Boolean) -> Unit = { isLoading ->
        binding.progressBar.root.isVisible = isLoading
    }

    private val slippageToleranceCollector: suspend (String) -> Unit = { slippageTolerance ->
        binding.slippageToleranceTextView.text = slippageTolerance
    }

    private val minimumReceivedAmountCollector: suspend (AnnotatedString) -> Unit = { minimumReceivedAmount ->
        binding.minimumReceivedTextView.text = context?.getXmlStyledString(minimumReceivedAmount)
    }

    private val errorEventCollector: suspend (Event<ErrorResource>?) -> Unit = { errorEvent ->
        errorEvent?.consume()?.run {
            showGlobalError(parseError(context ?: return@run), parseTitle(context ?: return@run))
        }
    }

    private val updateSlippageToleranceSuccessEventCollector: suspend (Event<Unit>?) -> Unit = { updateSuccessEvent ->
        updateSuccessEvent?.consume()?.run { showAlertSuccess(getString(R.string.slippage_tolerance_value_updated)) }
    }

    private val navigateToTransactionStatusFragmentEventCollector: suspend (
        Event<List<SwapQuoteTransaction>>?
    ) -> Unit = {
        it?.consume()?.run {
            nav(
                ConfirmSwapFragmentDirections.actionConfirmSwapFragmentToSwapTransactionStatusFragment(
                    confirmSwapViewModel.swapQuote,
                    this.toTypedArray()
                )
            )
        }
    }

    private val navigateToLedgerWaitingForApprovalDialogEventCollector: suspend (
        Event<LedgerDialogPayload>?
    ) -> Unit = {
        it?.consume()?.let { payload -> showLedgerWaitingForApprovalBottomSheet(payload) }
    }

    private val navigateToLedgerNotFoundDialogEventCollector: suspend (Event<Unit>?) -> Unit = {
        it?.consume()?.run { nav(HomeNavigationDirections.actionGlobalLedgerConnectionIssueBottomSheet()) }
    }

    private val dismissLedgerWaitingForApprovalDialogEventCollector: suspend (Event<Unit>?) -> Unit = {
        it?.consume()?.run {
            ledgerLoadingDialog?.dismissAllowingStateLoss()
            ledgerLoadingDialog = null
        }
    }

    private fun showLedgerWaitingForApprovalBottomSheet(
        ledgerDialogPayload: LedgerDialogPayload
    ) {
        if (ledgerLoadingDialog == null) {
            ledgerLoadingDialog = LedgerLoadingDialog.createLedgerLoadingDialog(
                ledgerName = ledgerDialogPayload.ledgerName,
                currentTransactionIndex = ledgerDialogPayload.currentTransactionIndex,
                totalTransactionCount = ledgerDialogPayload.totalTransactionCount,
                isTransactionIndicatorVisible = ledgerDialogPayload.isTransactionIndicatorVisible
            )
            ledgerLoadingDialog?.showWithStateCheck(childFragmentManager, ledgerDialogPayload.ledgerName.orEmpty())
        } else {
            ledgerLoadingDialog?.updateTransactionIndicator(ledgerDialogPayload.currentTransactionIndex)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
        confirmSwapViewModel.setupSwapTransactionSignManager(viewLifecycleOwner.lifecycle)
    }

    private fun initUi() {
        with(binding) {
            confirmSwapButton.setOnClickListener { confirmSwapViewModel.onConfirmSwapClick() }
            priceRatioTextView.setOnClickListener { onSwitchPriceRatioClick() }
            slippageToleranceLabelTextView.setOnClickListener { navToSlippageToleranceInfoBottomSheet() }
            slippageToleranceTextView.setOnClickListener { onUpdateSlippageToleranceClick() }
            priceImpactLabelTextView.setOnClickListener { navToPriceImpactInfoBottomSheet() }
            exchangeFeeLabelTextView.setOnClickListener { navToExchangeFeeInfoBottomSheet() }
        }
    }

    override fun onResume() {
        super.onResume()
        useFragmentResultListenerValue<Float>(CHECKED_SLIPPAGE_TOLERANCE_KEY) { slippageTolerance ->
            confirmSwapViewModel.onSlippageToleranceUpdated(slippageTolerance)
        }
    }

    private fun navToPriceImpactInfoBottomSheet() {
        nav(ConfirmSwapFragmentDirections.actionConfirmSwapFragmentToPriceImpactInfoBottomSheet())
    }

    private fun navToExchangeFeeInfoBottomSheet() {
        nav(ConfirmSwapFragmentDirections.actionConfirmSwapFragmentToExchangeFeeInfoBottomSheet())
    }

    private fun navToSlippageToleranceInfoBottomSheet() {
        nav(ConfirmSwapFragmentDirections.actionConfirmSwapFragmentToSlippageToleranceInfoBottomSheet())
    }

    private fun initObservers() {
        with(confirmSwapViewModel.confirmSwapPreviewFlow) {
            with(viewLifecycleOwner) {
                collectLatestOnLifecycle(
                    confirmSwapViewModel.confirmSwapPreviewFlow,
                    confirmSwapPreviewCollector
                )
                collectLatestOnLifecycle(
                    map { it.minimumReceived }.distinctUntilChanged(),
                    minimumReceivedAmountCollector
                )
                collectLatestOnLifecycle(
                    map { it.isLoading }.distinctUntilChanged(),
                    isLoadingVisibleCollector
                )
                collectLatestOnLifecycle(
                    map { it.slippageTolerance }.distinctUntilChanged(),
                    slippageToleranceCollector
                )
                collectLatestOnLifecycle(
                    map { it.errorEvent }.distinctUntilChanged(),
                    errorEventCollector
                )
                collectLatestOnLifecycle(
                    map { it.slippageToleranceUpdateSuccessEvent }.distinctUntilChanged(),
                    updateSlippageToleranceSuccessEventCollector
                )
                collectLatestOnLifecycle(
                    map { it.navigateToLedgerNotFoundDialogEvent }.distinctUntilChanged(),
                    navigateToLedgerNotFoundDialogEventCollector
                )
                collectLatestOnLifecycle(
                    map { it.navigateToLedgerWaitingForApprovalDialogEvent }.distinctUntilChanged(),
                    navigateToLedgerWaitingForApprovalDialogEventCollector
                )
                collectLatestOnLifecycle(
                    map { it.navigateToTransactionStatusFragmentEvent }.distinctUntilChanged(),
                    navigateToTransactionStatusFragmentEventCollector
                )
                collectLatestOnLifecycle(
                    map { it.dismissLedgerWaitingForApprovalDialogEvent }.distinctUntilChanged(),
                    dismissLedgerWaitingForApprovalDialogEventCollector
                )
            }
        }
    }

    private fun initConfirmSwapPreview(preview: ConfirmSwapPreview) {
        with(binding) {
            with(preview) {
                initAssetDetail(fromAssetInputView, fromAssetDetail)
                initAssetDetail(toAssetInputView, toAssetDetail)
                priceImpactTextView.text = formattedPriceImpact
                peraFeeTextView.text = formattedPeraFee
                exchangeFeeTextView.text = formattedExchangeFee
                priceRatioTextView.text = context?.getXmlStyledString(getPriceRatio(resources))
                priceImpactErrorGroup.isVisible = isPriceImpactErrorVisible
                initToolbarAccountDetail(accountDisplayName, accountIconResource)
            }
        }
    }

    private fun initToolbarAccountDetail(
        accountDisplayName: AccountDisplayName,
        accountIconResource: AccountIconResource
    ) {
        getAppToolbar()?.run {
            val iconSize = resources.getDimensionPixelSize(R.dimen.account_icon_size_xsmall)
            AccountIconDrawable.create(context, accountIconResource, iconSize)?.run {
                setSubtitleStartDrawable(this)
            }
            changeSubtitle(accountDisplayName.getDisplayTextOrAccountShortenedAddress())
            setOnTitleLongClickListener { onAccountAddressCopied(accountDisplayName.getAccountAddress()) }
        }
    }

    private fun onSwitchPriceRatioClick() {
        val priceRatioAnnotatedString = confirmSwapViewModel.getSwitchedPriceRatio(resources)
        binding.priceRatioTextView.text = context?.getXmlStyledString(priceRatioAnnotatedString)
    }

    private fun onUpdateSlippageToleranceClick() {
        nav(
            ConfirmSwapFragmentDirections
                .actionConfirmSwapFragmentToSlippageToleranceBottomSheet(confirmSwapViewModel.getSlippageTolerance())
        )
    }

    private fun initAssetDetail(assetInputView: SwapAssetInputView, assetDetail: ConfirmSwapPreview.SwapAssetDetail) {
        with(assetDetail) {
            assetInputView.apply {
                assetDrawableProvider.provideAssetDrawable(
                    imageView = getImageView(),
                    onResourceFailed = ::setImageDrawable
                )
                setAssetDetails(
                    amount = formattedAmount,
                    assetShortName = shortName,
                    verificationTierConfiguration = verificationTierConfig,
                    approximateValue = getString(R.string.approximate_currency_value, formattedApproximateValue)
                )
            }
        }
    }

    override fun onLedgerLoadingCancelled(shouldStopResources: Boolean) {
        ledgerLoadingDialog = null
        confirmSwapViewModel.onLedgerDialogCancelled()
    }
}
