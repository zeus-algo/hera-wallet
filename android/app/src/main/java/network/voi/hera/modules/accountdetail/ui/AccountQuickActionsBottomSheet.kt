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

package network.voi.hera.modules.accountdetail.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetAccountQuickActionsBinding
import network.voi.hera.models.AssetTransaction
import network.voi.hera.utils.Event
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class AccountQuickActionsBottomSheet : BaseBottomSheet(
    layoutResId = R.layout.bottom_sheet_account_quick_actions
) {

    private val accountQuickActionsViewModel by viewModels<AccountQuickActionsViewModel>()

    private val binding by viewBinding(BottomSheetAccountQuickActionsBinding::bind)

    private val swapButtonVisibilityCollector: suspend (Boolean) -> Unit = { isSwapButtonVisible ->
        binding.swapButton.isVisible = isSwapButtonVisible
    }

    private val swapNavigationDirectionEventCollector: suspend (Event<NavDirections>?) -> Unit = {
        it?.consume()?.run { nav(this) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    private fun initUi() {
        with(binding) {
            buyAlgoButton.setOnClickListener { navToMoonpayNavigation() }
            sendButton.setOnClickListener { navToGlobalSendAlgoNavigation() }
            receiveButton.setOnClickListener { navToShowQrFragment() }
            addNewAssetButton.setOnClickListener { navToAddAssetFragment() }
            moreButton.setOnClickListener { navToAccountOptionsBottomSheet() }
            swapButton.setOnClickListener { navToSwapNavigation() }
        }
    }

    private fun initObservers() {
        with(accountQuickActionsViewModel.accountQuickActionsPreviewFlow) {
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it.isSwapButtonVisible }.distinctUntilChanged(),
                swapButtonVisibilityCollector
            )
            viewLifecycleOwner.collectLatestOnLifecycle(
                map { it.swapNavigationDirectionEvent }.distinctUntilChanged(),
                swapNavigationDirectionEventCollector
            )
        }
    }

    private fun navToMoonpayNavigation() {
        nav(
            AccountQuickActionsBottomSheetDirections.actionAccountQuickActionsBottomSheetToMoonpayNavigation(
                walletAddress = accountQuickActionsViewModel.accountAddress
            )
        )
    }

    private fun navToGlobalSendAlgoNavigation() {
        nav(
            HomeNavigationDirections.actionGlobalSendAlgoNavigation(
                assetTransaction = AssetTransaction(senderAddress = accountQuickActionsViewModel.accountAddress)
            )
        )
    }

    private fun navToShowQrFragment() {
        nav(
            HomeNavigationDirections.actionGlobalShowQrNavigation(
                title = getString(R.string.qr_code),
                qrText = accountQuickActionsViewModel.accountAddress
            )
        )
    }

    private fun navToAddAssetFragment() {
        nav(
            AccountQuickActionsBottomSheetDirections.actionAccountQuickActionsBottomSheetToAssetAdditionNavigation(
                accountAddress = accountQuickActionsViewModel.accountAddress
            )
        )
    }

    private fun navToAccountOptionsBottomSheet() {
        nav(
            AccountQuickActionsBottomSheetDirections.actionAccountQuickActionsBottomSheetToAccountOptionsNavigation(
                publicKey = accountQuickActionsViewModel.accountAddress
            )
        )
    }

    private fun navToSwapNavigation() {
        accountQuickActionsViewModel.onSwapClick()
    }
}
