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

package network.voi.hera.modules.swap.ledger.signwithledger.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetSignWithLedgerBinding
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.setFragmentNavigationResult
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignWithLedgerBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_sign_with_ledger) {

    private val signWithLedgerViewModel by viewModels<SignWithLedgerViewModel>()

    private val binding by viewBinding(BottomSheetSignWithLedgerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        val transactionCount = signWithLedgerViewModel.transactionCount
        with(binding) {
            transactionDescriptionTextView.text = context?.getXmlStyledString(
                stringResId = R.string.we_ve_detected_that_you,
                replacementList = listOf("transaction_count" to transactionCount.toString())
            )
            signTransactionButton.apply {
                text = resources.getQuantityString(
                    R.plurals.sign_transaction,
                    transactionCount,
                    transactionCount
                )
                setOnClickListener {
                    setFragmentNavigationResult(SIGN_WITH_LEDGER_APPROVED_KEY, true)
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    companion object {
        const val SIGN_WITH_LEDGER_APPROVED_KEY = "sign_with_ledger_approved_key"
    }
}
