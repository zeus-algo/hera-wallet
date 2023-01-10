@file:SuppressWarnings("TooManyFunctions")
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

package network.voi.hera.modules.accountdetail.assets.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentAccountAssetsBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.modules.accountdetail.assets.ui.adapter.AccountAssetsAdapter
import network.voi.hera.modules.accountdetail.assets.ui.domain.AccountAssetsPreviewUseCase.Companion.QUICK_ACTIONS_INDEX
import network.voi.hera.modules.accountdetail.assets.ui.model.AccountDetailAssetsItem
import network.voi.hera.utils.ExcludedViewTypesDividerItemDecoration
import network.voi.hera.utils.addCustomDivider
import network.voi.hera.utils.addItemVisibilityChangeListener
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountAssetsFragment : BaseFragment(R.layout.fragment_account_assets) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val binding by viewBinding(FragmentAccountAssetsBinding::bind)

    private val accountAssetsViewModel: AccountAssetsViewModel by viewModels()

    private var listener: Listener? = null

    private val accountAssetListener = object : AccountAssetsAdapter.Listener {
        override fun onAssetClick(assetId: Long) {
            listener?.onAssetClick(assetId)
        }

        override fun onAssetLongClick(assetId: Long) {
            listener?.onAssetLongClick(assetId)
        }

        override fun onNFTClick(nftId: Long) {
            listener?.onNFTClick(nftId)
        }

        override fun onNFTLongClick(nftId: Long) {
            listener?.onNFTLongClick(nftId)
        }

        override fun onAddNewAssetClick() {
            accountAssetsViewModel.logAccountAssetsAddAssetEvent()
            listener?.onAddAssetClick()
        }

        override fun onSearchQueryUpdated(query: String) {
            accountAssetsViewModel.updateSearchQuery(query = query)
        }

        override fun onManageAssetsClick() {
            accountAssetsViewModel.logAccountAssetsManageAssetsEvent()
            listener?.onManageAssetsClick()
        }

        override fun onBuyAlgoClick() {
            accountAssetsViewModel.logAccountAssetsBuyAlgoTapEventTracker()
            listener?.onBuyAlgoClick()
        }

        override fun onSendClick() {
            listener?.onSendClick()
        }

        override fun onSwapClick() {
            listener?.onSwapClick()
        }

        override fun onMoreClick() {
            listener?.onMoreClick()
        }

        override fun onRequiredMinimumBalanceClick() {
            listener?.onMinimumBalanceInfoClick()
        }
    }

    private val accountAssetsAdapter = AccountAssetsAdapter(accountAssetListener)

    private val accountAssetsCollector: suspend (List<AccountDetailAssetsItem>?) -> Unit = { accountDetailItemList ->
        accountAssetsAdapter.submitList(accountDetailItemList.orEmpty())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        // TODO: find a way to update the preview flow only in case of filter option changes
        accountAssetsViewModel.initAccountAssetsFlow()
    }

    private fun initUi() {
        binding.accountAssetsRecyclerView.adapter = accountAssetsAdapter
        binding.accountAssetsRecyclerView.addCustomDivider(
            drawableResId = R.drawable.horizontal_divider_80_24dp,
            showLast = false,
            divider = ExcludedViewTypesDividerItemDecoration(AccountDetailAssetsItem.excludedItemFromDivider)
        )
        if (accountAssetsViewModel.canAccountSignTransactions()) {
            binding.accountQuickActionsFloatingActionButton.setOnClickListener {
                listener?.onAccountQuickActionsFloatingActionButtonClicked()
            }
            binding.accountAssetsRecyclerView.addItemVisibilityChangeListener(QUICK_ACTIONS_INDEX) { isVisible ->
                with(binding.accountAssetsMotionLayout) {
                    if (isVisible) transitionToStart() else transitionToEnd()
                }
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            accountAssetsViewModel.accountAssetsFlow,
            accountAssetsCollector
        )
    }

    interface Listener {
        fun onAddAssetClick()
        fun onAssetClick(assetId: Long)
        fun onAssetLongClick(assetId: Long)
        fun onNFTClick(nftId: Long)
        fun onNFTLongClick(nftId: Long)
        fun onBuyAlgoClick()
        fun onSendClick()
        fun onSwapClick()
        fun onMoreClick()
        fun onManageAssetsClick()
        fun onAccountQuickActionsFloatingActionButtonClicked()
        fun onMinimumBalanceInfoClick()
    }

    companion object {
        const val ADDRESS_KEY = "address_key"
        fun newInstance(address: String): AccountAssetsFragment {
            return AccountAssetsFragment().apply { arguments = Bundle().apply { putString(ADDRESS_KEY, address) } }
        }
    }
}
