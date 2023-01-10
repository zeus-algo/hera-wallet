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

package network.voi.hera.modules.swap.transactionstatus.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentSwapTransactionStatusBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.modules.swap.transactionstatus.ui.model.SwapTransactionStatusPreview
import network.voi.hera.utils.browser.openGroupTransactionInAlgoExplorer
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SwapTransactionStatusFragment : BaseFragment(R.layout.fragment_swap_transaction_status) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val swapTransactionStatusViewModel by viewModels<SwapTransactionStatusViewModel>()

    private val binding by viewBinding(FragmentSwapTransactionStatusBinding::bind)

    private val swapTransactionStatusPreviewCollector: suspend (SwapTransactionStatusPreview?) -> Unit = { preview ->
        if (preview != null) updateUiWithPreview(preview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        swapTransactionStatusViewModel.initSwapTransactionStatusPreviewFlow(resources)
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            swapTransactionStatusViewModel.swapTransactionStatusPreviewFlow
                .collectLatest(swapTransactionStatusPreviewCollector)
        }
    }

    private fun updateUiWithPreview(preview: SwapTransactionStatusPreview) {
        preview.navigateBackEvent?.consume()?.run {
            exitSwapNavigation()
            return
        }
        preview.navigateToAssetSwapFragmentEvent?.consume()?.run {
            navBack()
            return
        }
        setTransactionStatusGroup(preview)
        setTransactionDetailGroup(preview)
        setTransactionActionGroup(preview)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Back button needs to be overridden to force user to use action buttons
            return
        }
    }

    private fun setTransactionStatusGroup(preview: SwapTransactionStatusPreview) {
        with(binding) {
            with(preview) {
                transactionStatusLottieView.apply {
                    transactionStatusAnimationResId?.let { setAnimation(it) } ?: run { clearAnimation() }
                    setBackgroundResource(transactionStatusAnimationBackgroundResId)
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, transactionStatusAnimationBackgroundTintResId)
                    )

                    transactionStatusAnimationDrawableResId?.let { safeImageResId -> setImageResource(safeImageResId) }
                    transactionStatusAnimationDrawableTintResId?.let { safeImageTintResId ->
                        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, safeImageTintResId))
                    }
                }
                transactionStatusTitleTextView.text = context?.getXmlStyledString(
                    annotatedString = transactionStatusTitleAnnotatedString
                )
                transactionStatusDescriptionTextView.text = context?.getXmlStyledString(
                    annotatedString = transactionStatusDescriptionAnnotatedString
                )
            }
        }
    }

    private fun setTransactionDetailGroup(preview: SwapTransactionStatusPreview) {
        with(binding) {
            with(preview) {
                transactionDetailGroup.isVisible = isTransactionDetailGroupVisible
                viewTransactionDetailButton.setOnClickListener {
                    openTransactionInAlgoExplorer(urlEncodedTransactionGroupId)
                }
                viewSwapSummaryButton.setOnClickListener { navToSwapSummary() }
            }
        }
    }

    private fun setTransactionActionGroup(preview: SwapTransactionStatusPreview) {
        with(binding) {
            with(preview) {
                primaryActionButton.isVisible = isPrimaryActionButtonVisible
                goToHomePageButton.isVisible = isGoToHomepageButtonVisible
                primaryActionButton.apply {
                    primaryActionButtonTextResId?.let { resId -> setText(resId) }
                    setOnClickListener {
                        swapTransactionStatusViewModel.onPrimaryButtonClicked(swapTransactionStatusType)
                    }
                }
                goToHomePageButton.apply {
                    secondaryActionButtonTextResId?.let { resId -> setText(resId) }
                    setOnClickListener { exitSwapNavigation() }
                }
            }
        }
    }

    private fun exitSwapNavigation() {
        findNavController().popBackStack(R.id.assetSwapFragment, false)
        navBack()
    }

    private fun navToSwapSummary() {
        with(swapTransactionStatusViewModel) {
            nav(
                SwapTransactionStatusFragmentDirections
                    .actionSwapTransactionStatusFragmentToSwapTransactionSummaryFragment(
                        swapQuote = swapQuote,
                        optInTransactionFees = getOptInTransactionsFees(),
                        algorandTransactionFees = getAlgorandTransactionFees()
                    )
            )
        }
    }

    private fun openTransactionInAlgoExplorer(transactionGroupId: String?) {
        val networkSlug = swapTransactionStatusViewModel.getNetworkSlug()
        context?.openGroupTransactionInAlgoExplorer(groupId = transactionGroupId, networkSlug = networkSlug)
    }
}
