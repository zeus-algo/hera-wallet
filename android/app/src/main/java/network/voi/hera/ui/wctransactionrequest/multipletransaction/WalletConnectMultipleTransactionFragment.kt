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

package network.voi.hera.ui.wctransactionrequest.multipletransaction

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentWalletConnectMultipleTransactionBinding
import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.models.TransactionRequestAction
import network.voi.hera.ui.wctransactionrequest.WalletConnectTransactionAdapter
import network.voi.hera.utils.viewbinding.viewBinding

class WalletConnectMultipleTransactionFragment : BaseFragment(
    R.layout.fragment_wallet_connect_multiple_transaction
) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val toolbarConfiguration = ToolbarConfiguration(titleResId = R.string.unsigned_transactions)

    private val binding by viewBinding(FragmentWalletConnectMultipleTransactionBinding::bind)

    private val args: WalletConnectMultipleTransactionFragmentArgs by navArgs()

    private val transactionAdapterListener = object : WalletConnectTransactionAdapter.Listener {
        override fun onMultipleTransactionClick(transactionList: List<BaseWalletConnectTransaction>) {
            val txnArray = transactionList.toTypedArray()
            listener?.onNavigate(
                WalletConnectMultipleTransactionFragmentDirections
                    .actionWalletConnectMultipleTransactionFragmentToWalletConnectAtomicTransactionsFragment(txnArray)
            )
        }

        override fun onSingleTransactionClick(transaction: BaseWalletConnectTransaction) {
            listener?.onNavigate(
                WalletConnectMultipleTransactionFragmentDirections
                    .actionWalletConnectMultipleTransactionFragmentToTransactionRequestDetailFragment(transaction)
            )
        }
    }

    private val transactionAdapter = WalletConnectTransactionAdapter(transactionAdapterListener)

    private var listener: TransactionRequestAction? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment?.parentFragment as? TransactionRequestAction
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        transactionAdapter.submitList(args.transactions.toList())
    }

    private fun initUi() {
        with(binding) {
            listener?.showButtons()
            customToolbar.configure(toolbarConfiguration)
            transactionListRecyclerView.adapter = transactionAdapter
        }
    }
}
