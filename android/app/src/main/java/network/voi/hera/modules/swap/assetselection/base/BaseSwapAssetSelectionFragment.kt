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

package network.voi.hera.modules.swap.assetselection.base

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.databinding.FragmentSwapAssetSelectionBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ScreenState
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.swap.assetselection.base.SwapAssetSelectionAdapter.SwapAssetSelectionAdapterListener
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapAssetSelectionItem
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

abstract class BaseSwapAssetSelectionFragment : BaseFragment(R.layout.fragment_swap_asset_selection) {

    abstract fun onAssetSelected(assetItem: SwapAssetSelectionItem)
    abstract fun setToolbarTitle(toolbar: CustomToolbar?)

    abstract val baseAssetSelectionViewModel: BaseSwapAssetSelectionViewModel

    private val binding by viewBinding(FragmentSwapAssetSelectionBinding::bind)

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val swapAssetSelectionAdapterListener = object : SwapAssetSelectionAdapterListener {
        override fun onAssetSelected(item: SwapAssetSelectionItem) {
            this@BaseSwapAssetSelectionFragment.onAssetSelected(item)
        }
    }

    private val swapAssetSelectionAdapter = SwapAssetSelectionAdapter(swapAssetSelectionAdapterListener)

    private val assetSelectionItemListCollector: suspend (List<SwapAssetSelectionItem>?) -> Unit = {
        swapAssetSelectionAdapter.submitList(it.orEmpty())
    }

    private val isLoadingStateCollector: suspend (Boolean?) -> Unit = { isLoading ->
        binding.progressBar.root.isVisible = isLoading == true
    }

    private val screenStateCollector: suspend (ScreenState?) -> Unit = { screenState ->
        binding.screenStateView.apply {
            isVisible = screenState != null
            setupUi(screenState ?: return@apply)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(getAppToolbar())
        initObservers()
        initUi()
    }

    private fun initUi() {
        with(binding) {
            swapAssetSelectionRecyclerView.adapter = swapAssetSelectionAdapter
            searchView.setOnTextChanged { query ->
                baseAssetSelectionViewModel.updateSearchQuery(query)
            }
        }
    }

    protected open fun initObservers() {
        with(baseAssetSelectionViewModel.swapAssetSelectionPreviewFlow) {
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it?.swapAssetSelectionItemList }.distinctUntilChanged(),
                assetSelectionItemListCollector
            )
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it?.isLoading }.distinctUntilChanged(),
                isLoadingStateCollector
            )
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it?.screenState }.distinctUntilChanged(),
                screenStateCollector
            )
        }
    }
}
