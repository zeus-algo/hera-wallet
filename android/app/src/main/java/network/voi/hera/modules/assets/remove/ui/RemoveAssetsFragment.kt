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

package network.voi.hera.modules.assets.remove.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentRemoveAssetsBinding
import network.voi.hera.models.AssetAction
import network.voi.hera.models.AssetActionResult
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.AssetTransaction
import network.voi.hera.models.BaseRemoveAssetItem
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.assets.action.transferbalance.TransferBalanceActionBottomSheet.Companion.TRANSFER_ASSET_ACTION_RESULT
import network.voi.hera.modules.assets.remove.ui.adapter.RemoveAssetAdapter
import network.voi.hera.modules.assets.remove.ui.model.RemoveAssetsPreview
import network.voi.hera.utils.ExcludedViewTypesDividerItemDecoration
import network.voi.hera.utils.addCustomDivider
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.isGreaterThan
import network.voi.hera.utils.useFragmentResultListenerValue
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigInteger

@AndroidEntryPoint
class RemoveAssetsFragment : BaseFragment(R.layout.fragment_remove_assets) {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_close,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val removeAssetsViewModel: RemoveAssetsViewModel by viewModels()

    private val binding by viewBinding(FragmentRemoveAssetsBinding::bind)

    private val removeAssetAdapterListener = object : RemoveAssetAdapter.RemoveAssetAdapterListener {
        override fun onSearchQueryUpdate(query: String) {
            removeAssetsViewModel.updateSearchingQuery(query)
        }

        override fun onAssetItemClick(assetId: Long) {
            navToAsaProfile(assetId)
        }

        override fun onCollectibleItemClick(collectibleId: Long) {
            navToCollectibleProfile(collectibleId)
        }

        override fun onCollectibleRemoveClick(
            baseRemoveAssetItem: BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem
        ) {
            onRemoveCollectibleClick(baseRemoveAssetItem)
        }

        override fun onAssetRemoveClick(baseRemoveAssetItem: BaseRemoveAssetItem.BaseRemovableItem.RemoveAssetItem) {
            onRemoveAssetClick(baseRemoveAssetItem)
        }
    }

    private val removeAssetAdapter = RemoveAssetAdapter(removeAssetAdapterListener)

    private val removeAssetsPreviewCollector: suspend (RemoveAssetsPreview?) -> Unit = { preview ->
        if (preview != null) updatePreview(preview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        initObservers()
    }

    private fun setupToolbar() {
        getAppToolbar()?.configure(toolbarConfiguration)
    }

    private fun updatePreview(preview: RemoveAssetsPreview) {
        removeAssetAdapter.submitList(preview.removableAssetList)
    }

    private fun setupRecyclerView() {
        binding.assetsRecyclerView.apply {
            adapter = removeAssetAdapter
            addCustomDivider(
                drawableResId = R.drawable.horizontal_divider_80_24dp,
                showLast = false,
                divider = ExcludedViewTypesDividerItemDecoration(BaseRemoveAssetItem.excludedItemFromDivider)
            )
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            removeAssetsViewModel.removeAssetsPreviewFlow,
            removeAssetsPreviewCollector
        )
    }

    private fun onRemoveAssetClick(removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem) {
        val hasBalanceInAccount = removeAssetItem.amount isGreaterThan BigInteger.ZERO
        if (hasBalanceInAccount) {
            navToTransferBalanceActionBottomSheet(removeAssetItem)
        } else {
            navToRemoveAssetActionBottomSheet(removeAssetItem)
        }
    }

    private fun onRemoveCollectibleClick(
        removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem
    ) {
        val hasBalanceInAccount = removeAssetItem.amount isGreaterThan BigInteger.ZERO
        if (hasBalanceInAccount) {
            navToTransferBalanceActionBottomSheet(removeAssetItem)
        } else {
            navToOptOutCollectibleActionBottomSheet(removeAssetItem)
        }
    }

    private fun navToCollectibleProfile(collectibleId: Long) {
        nav(
            RemoveAssetsFragmentDirections.actionRemoveAssetsFragmentToCollectibleProfileNavigation(
                collectibleId = collectibleId,
                accountAddress = removeAssetsViewModel.accountAddress
            )
        )
    }

    private fun navToAsaProfile(assetId: Long) {
        nav(
            RemoveAssetsFragmentDirections.actionRemoveAssetsFragmentToAsaProfileNavigation(
                assetId = assetId,
                accountAddress = removeAssetsViewModel.accountAddress
            )
        )
    }

    private fun navToTransferBalanceActionBottomSheet(removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem) {
        nav(
            RemoveAssetsFragmentDirections.actionRemoveAssetsFragmentToAssetTransferBalanceActionNavigation(
                AssetAction(
                    assetId = removeAssetItem.id,
                    asset = AssetInformation.createAssetInformation(
                        removeAssetItem = removeAssetItem,
                        resources = binding.root.resources
                    ),
                    publicKey = removeAssetsViewModel.accountAddress
                )
            )
        )
    }

    private fun navToRemoveAssetActionBottomSheet(removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem) {
        nav(
            RemoveAssetsFragmentDirections.actionRemoveAssetsFragmentToAssetRemovalActionNavigation(
                AssetAction(
                    assetId = removeAssetItem.id,
                    publicKey = removeAssetsViewModel.accountAddress,
                    asset = AssetInformation.createAssetInformation(
                        removeAssetItem = removeAssetItem,
                        resources = binding.root.resources
                    )
                )
            )
        )
    }

    private fun navToOptOutCollectibleActionBottomSheet(
        removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem
    ) {
        val assetAction = with(removeAssetItem) {
            AssetAction(
                assetId = id,
                publicKey = removeAssetsViewModel.accountAddress,
                asset = AssetInformation(
                    assetId = id,
                    creatorPublicKey = creatorPublicKey,
                    fullName = name.getName(resources),
                    verificationTier = null
                )
            )
        }
        nav(
            RemoveAssetsFragmentDirections.actionRemoveAssetsFragmentToNftOptOutConfirmationNavigation(
                assetAction = assetAction
            )
        )
    }

    override fun onResume() {
        super.onResume()
        initSavedStateListener()
    }

    private fun navToSendAlgoNavigation(assetTransaction: AssetTransaction, shouldPopulateAmountWithMax: Boolean) {
        nav(HomeNavigationDirections.actionGlobalSendAlgoNavigation(assetTransaction, shouldPopulateAmountWithMax))
    }

    private fun initSavedStateListener() {
        useFragmentResultListenerValue<AssetActionResult>(
            key = TRANSFER_ASSET_ACTION_RESULT,
            result = { assetActionResult ->
                val assetId = assetActionResult.asset.assetId
                val assetTransaction = AssetTransaction(
                    assetId = assetId,
                    senderAddress = removeAssetsViewModel.accountAddress,
                    amount = BigInteger.ZERO,
                )
                navToSendAlgoNavigation(
                    assetTransaction = assetTransaction,
                    shouldPopulateAmountWithMax = true
                )
            }
        )
    }
}
