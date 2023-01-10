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

package network.voi.hera.modules.swap.assetselection.toasset.ui

import androidx.fragment.app.viewModels
import network.voi.hera.MainActivity
import network.voi.hera.R
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.models.AssetAction
import network.voi.hera.models.AssetActionResult
import network.voi.hera.modules.assets.action.addition.AddAssetActionBottomSheet.Companion.ADD_ASSET_ACTION_RESULT_KEY
import network.voi.hera.modules.swap.assetselection.base.BaseSwapAssetSelectionFragment
import network.voi.hera.modules.swap.assetselection.base.BaseSwapAssetSelectionViewModel
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapAssetSelectionItem
import network.voi.hera.utils.Event
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.setFragmentNavigationResult
import network.voi.hera.utils.useFragmentResultListenerValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class SwapToAssetSelectionFragment : BaseSwapAssetSelectionFragment() {

    private val swapToAssetSelectionViewModel by viewModels<SwapToAssetSelectionViewModel>()

    override val baseAssetSelectionViewModel: BaseSwapAssetSelectionViewModel
        get() = swapToAssetSelectionViewModel

    private val navigateToAssetAdditionBottomSheetEventCollector: suspend (Event<AssetAction>?) -> Unit = { event ->
        event?.consume()?.let { handleAssetAddition(it) }
    }

    private val assetSelectionSuccessEventCollector: suspend (Event<SwapAssetSelectionItem>?) -> Unit = {
        it?.consume()?.let { assetItem -> setResultAndNavigateBack(assetItem.assetId) }
    }

    override fun initObservers() {
        super.initObservers()
        with(baseAssetSelectionViewModel.swapAssetSelectionPreviewFlow) {
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it?.assetSelectedEvent }.distinctUntilChanged(),
                assetSelectionSuccessEventCollector
            )
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it?.navigateToAssetAdditionBottomSheetEvent }.distinctUntilChanged(),
                navigateToAssetAdditionBottomSheetEventCollector
            )
        }
    }

    override fun onResume() {
        super.onResume()
        useFragmentResultListenerValue<AssetActionResult>(ADD_ASSET_ACTION_RESULT_KEY) { assetActionResult ->
            (activity as? MainActivity)?.signAddAssetTransaction(assetActionResult)
        }
    }

    override fun onAssetSelected(assetItem: SwapAssetSelectionItem) {
        swapToAssetSelectionViewModel.onAssetSelected(assetItem)
    }

    override fun setToolbarTitle(toolbar: CustomToolbar?) {
        toolbar?.changeTitle(R.string.swap_to)
    }

    private fun handleAssetAddition(assetAction: AssetAction) {
        nav(
            SwapToAssetSelectionFragmentDirections
                .actionSwapToAssetSelectionFragmentToAssetAdditionActionNavigation(assetAction)
        )
        (activity as? MainActivity)?.mainViewModel?.assetOperationResultLiveData?.observe(viewLifecycleOwner) {
            it.peek().use(
                onSuccess = {
                    if (it.assetId == assetAction.assetId) {
                        setResultAndNavigateBack(it.assetId)
                    }
                }
            )
        }
    }

    private fun setResultAndNavigateBack(assetId: Long) {
        setFragmentNavigationResult(SWAP_TO_ASSET_ID_KEY, assetId)
        navBack()
    }

    companion object {
        const val SWAP_TO_ASSET_ID_KEY = "swapToAssetIdKey"
    }
}
