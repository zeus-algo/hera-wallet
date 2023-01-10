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

package network.voi.hera.modules.transaction.detail.ui.applicationcalltransaction

import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.TextButton
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.transaction.detail.ui.model.ApplicationCallAssetInformation
import network.voi.hera.modules.transaction.detail.domain.model.BaseTransactionDetail
import network.voi.hera.modules.transaction.detail.ui.BaseTransactionDetailFragment
import network.voi.hera.modules.transaction.detail.ui.adapter.TransactionDetailAdapter
import network.voi.hera.utils.copyToClipboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApplicationCallTransactionDetailFragment : BaseTransactionDetailFragment() {

    override val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.app_call,
        startIconResId = R.drawable.ic_close,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(
        toolbarConfiguration = toolbarConfiguration,
        firebaseEventScreenId = FIREBASE_EVENT_SCREEN_ID
    )

    override val transactionDetailViewModel by viewModels<ApplicationCallTransactionDetailViewModel>()

    private val transactionDetailLongClickListener = object : TransactionDetailAdapter.LongPressListener {
        override fun onTransactionIdLongClick(transactionId: String) {
            context?.copyToClipboard(transactionId)
        }
    }

    private val applicationCallTransactionListener =
        object : TransactionDetailAdapter.ApplicationCallTransactionListener {
            override fun onInnerTransactionClick(transactions: List<BaseTransactionDetail>) {
                transactionDetailViewModel.putInnerTransactionToStackCache(transactions)
                navToInnerTransactionFragment()
            }

            override fun onShowMoreAssetClick(assetInformationList: List<ApplicationCallAssetInformation>) {
                navToAssetInformationBottomSheet(assetInformationList)
            }
        }

    override val transactionDetailAdapter = TransactionDetailAdapter(
        extrasExtrasClickListener = transactionDetailClickListener,
        longPressListener = transactionDetailLongClickListener,
        tooltipListener = transactionDetailTooltipListener,
        applicationCallTransactionListener = applicationCallTransactionListener
    )

    override fun initUi() {
        configureToolbar()
    }

    private fun configureToolbar() {
        if (transactionDetailViewModel.shouldShowCloseButton) {
            getAppToolbar()?.apply {
                setEndButton(button = TextButton(R.string.close, onClick = ::onTransactionDetailClose))
                configureStartButton(resId = R.drawable.ic_left_arrow, clickAction = ::navBack)
            }
        }
    }

    private fun navToInnerTransactionFragment() {
        nav(
            ApplicationCallTransactionDetailFragmentDirections
                .actionApplicationCallTransactionDetailFragmentToInnerTransactionDetailFragment(
                    accountAddress = transactionDetailViewModel.accountAddress,
                    transactionId = transactionDetailViewModel.transactionId
                )
        )
    }

    private fun navToAssetInformationBottomSheet(assetInformationList: List<ApplicationCallAssetInformation>) {
        nav(
            ApplicationCallTransactionDetailFragmentDirections
                .actionApplicationCallTransactionDetailFragmentToApplicationCallAssetsBottomSheet(
                    assetInformationList.toTypedArray()
                )
        )
    }
}
