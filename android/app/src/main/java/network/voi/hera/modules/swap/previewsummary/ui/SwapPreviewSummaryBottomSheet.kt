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

package network.voi.hera.modules.swap.previewsummary.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetSwapPreviewSummaryBinding
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.swap.previewsummary.ui.model.SwapPreviewSummaryPreview
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AccountIconDrawable
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.setDrawable
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SwapPreviewSummaryBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_swap_preview_summary) {

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.swap_summary,
        startIconClick = ::navBack,
        startIconResId = R.drawable.ic_close
    )

    private val binding by viewBinding(BottomSheetSwapPreviewSummaryBinding::bind)

    private val swapPreviewSummaryViewModel by viewModels<SwapPreviewSummaryViewModel>()

    private val swapPreviewSummaryPreviewCollector: suspend (SwapPreviewSummaryPreview) -> Unit = { preview ->
        initPreview(preview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    private fun initUi() {
        with(binding) {
            toolbar.configure(toolbarConfiguration)
            priceRatioTextView.setOnClickListener { onSwitchPriceRatioClick() }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            swapPreviewSummaryViewModel.swapPreviewSummaryPreviewFlow,
            swapPreviewSummaryPreviewCollector
        )
    }

    private fun initPreview(preview: SwapPreviewSummaryPreview) {
        with(preview) {
            initAccountDetails(accountDisplayName, accountIconResource)
            with(binding) {
                priceRatioTextView.text = root.context.getXmlStyledString(getPriceRatio(resources))
                slippageToleranceTextView.text = slippageTolerance
                priceImpactTextView.text = getString(R.string.formatted_percentage, priceImpact)
                minimumReceivedTextView.text = root.context.getXmlStyledString(minimumReceived)
                exchangeFeeTextView.text = formattedExchangeFee
                peraFeeTextView.text = formattedPeraFee
                totalSwapFeeTextView.text = formattedTotalFee
            }
        }
    }

    private fun initAccountDetails(accountDisplayName: AccountDisplayName, accountIconResource: AccountIconResource) {
        with(binding) {
            val iconSize = resources.getDimensionPixelSize(R.dimen.account_icon_size_normal)
            val accountIconDrawable = AccountIconDrawable.create(root.context, accountIconResource, iconSize)
            val accountName = accountDisplayName.getDisplayTextOrAccountShortenedAddress()
            accountTextView.apply {
                setDrawable(start = accountIconDrawable)
                text = accountName
            }
        }
    }

    private fun onSwitchPriceRatioClick() {
        val newPriceRatio = swapPreviewSummaryViewModel.getUpdatedPriceRatio(resources)
        binding.priceRatioTextView.text = context?.getXmlStyledString(newPriceRatio)
    }
}
