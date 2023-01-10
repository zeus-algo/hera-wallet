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

package network.voi.hera.ui.ledgersearch.ledgerinformation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetLedgerInformationBinding
import network.voi.hera.models.Account
import network.voi.hera.models.LedgerInformationListItem
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.sendErrorLog
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LedgerInformationBottomSheet(
    override val fullPageNeeded: Boolean = true
) : BaseBottomSheet(R.layout.bottom_sheet_ledger_information) {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_close,
        startIconClick = ::navBack
    )

    private val args: LedgerInformationBottomSheetArgs by navArgs()

    private val ledgerInformationViewModel: LedgerInformationViewModel by viewModels()

    private val binding by viewBinding(BottomSheetLedgerInformationBinding::bind)

    private val ledgerInformationAdapter = LedgerInformationAdapter()

    private val ledgerInformationCollector: suspend (List<LedgerInformationListItem>?) -> Unit = {
        ledgerInformationAdapter.submitList(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    private fun initUi() {
        binding.toolbar.configure(toolbarConfiguration)
        setupToolbarTitle()
        setupRecyclerView()
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            ledgerInformationViewModel.ledgerInformationFlow,
            ledgerInformationCollector
        )
    }

    private fun setupRecyclerView() {
        binding.ledgerInformationList.adapter = ledgerInformationAdapter
    }

    private fun setupToolbarTitle() {
        val account = args.selectedLedgerAccountSelectionListItem.account as? Account
        when (val detail = account?.detail) {
            is Account.Detail.Ledger -> {
                binding.toolbar.changeTitle(
                    context?.getXmlStyledString(
                        stringResId = R.string.ledger_position,
                        replacementList = listOf("account_index" to (detail.positionInLedger + 1).toString())
                    ).toString()
                )
            }
            is Account.Detail.RekeyedAuth -> {
                binding.toolbar.changeTitle(account.address)
            }
            else -> {
                sendErrorLog("Unhandled else case in LedgerInformationBottomSheet.setupToolbarTitle")
            }
        }
    }
}
