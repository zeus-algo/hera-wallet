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

package network.voi.hera.ui.send

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.utils.isEqualTo
import java.math.BigInteger

// TODO: 13.01.2022 send_algo_navigation graph will be separated into multiple graphs
class SendAlgoFragment : BaseFragment(R.layout.fragment_send_algo) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val args by navArgs<SendAlgoFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val assetTransaction = args.assetTransaction
        when {
            assetTransaction == null || assetTransaction.senderAddress.isEmpty() -> {
                SendAlgoFragmentDirections.actionSendAlgoFragmentToSenderAccountSelectionFragment(assetTransaction)
            }
            assetTransaction.assetId == -1L -> {
                SendAlgoFragmentDirections.actionSendAlgoFragmentToAssetSelectionFragment(assetTransaction)
            }
            assetTransaction.amount isEqualTo BigInteger.ZERO -> {
                SendAlgoFragmentDirections.actionSendAlgoFragmentToAssetTransferAmountFragment(
                    assetTransaction = assetTransaction,
                    shouldPopulateAmountWithMax = args.shouldPopulateAmountWithMax
                )
            }
            assetTransaction.receiverUser == null -> {
                SendAlgoFragmentDirections.actionSendAlgoFragmentToReceiverAccountSelectionFragment(assetTransaction)
            }
            else -> throw Exception("$logTag Unknown direction")
        }.apply { nav(this) }
    }

    companion object {
        private val logTag = SendAlgoFragment::class.java.simpleName
    }
}
