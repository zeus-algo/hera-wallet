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

package network.voi.hera.modules.swap.transactionsummary.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentSwapTransactionSummaryBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.swap.transactionsummary.ui.adapter.SwapTransactionSummaryAdapter
import network.voi.hera.modules.swap.transactionsummary.ui.model.SwapTransactionSummaryPreview
import network.voi.hera.utils.BaseCustomDividerItemDecoration
import network.voi.hera.utils.addCustomDivider
import network.voi.hera.utils.copyToClipboard
import network.voi.hera.utils.toShortenedAddress
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SwapTransactionSummaryFragment : BaseFragment(R.layout.fragment_swap_transaction_summary) {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_close,
        titleResId = R.string.swap_summary,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val binding by viewBinding(FragmentSwapTransactionSummaryBinding::bind)

    private val swapTransactionSummaryViewModel by viewModels<SwapTransactionSummaryViewModel>()

    private val swapTransactionSummaryCollector: suspend (SwapTransactionSummaryPreview?) -> Unit = { preview ->
        swapTransactionSummaryAdapter.submitList(preview?.baseSwapTransactionSummaryItems.orEmpty())
    }

    private val swapTransactionSummaryAdapterListener =
        SwapTransactionSummaryAdapter.SwapSummaryAdapterListener { accountAddress ->
            // TODO: Use extension function whenever merge this branch with `5.4.0`
            context?.copyToClipboard(
                textToCopy = accountAddress,
                showToast = false
            )
            showTopToast(
                title = getString(R.string.address_copied_to_clipboard),
                description = accountAddress.toShortenedAddress()
            )
        }

    private val swapTransactionSummaryAdapter =
        SwapTransactionSummaryAdapter(listener = swapTransactionSummaryAdapterListener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
        swapTransactionSummaryViewModel.initSwapTransactionSummaryPreview(resources)
    }

    private fun initUi() {
        binding.transactionSummaryRecyclerView.apply {
            adapter = swapTransactionSummaryAdapter
            addCustomDivider(
                drawableResId = R.drawable.horizontal_divider_margin_24dp,
                showLast = false,
                divider = BaseCustomDividerItemDecoration()
            )
        }
    }

    private fun initObservers() {
        lifecycleScope.launchWhenResumed {
            swapTransactionSummaryViewModel.swapTransactionSummaryPreviewFlow
                .collectLatest(swapTransactionSummaryCollector)
        }
    }
}
