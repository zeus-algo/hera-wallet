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

package com.algorand.android.ui.common.walletconnect

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import network.voi.hera.R
import com.algorand.android.core.DaggerBaseBottomSheet
import network.voi.hera.databinding.BottomSheetWalletConnectDappMessageBinding
import com.algorand.android.utils.loadPeerMetaIcon
import com.algorand.android.utils.viewbinding.viewBinding

class WalletConnectDappMessageBottomSheet : DaggerBaseBottomSheet(
    layoutResId = R.layout.bottom_sheet_wallet_connect_dapp_message,
    fullPageNeeded = false,
    firebaseEventScreenId = null
) {

    private val binding by viewBinding(BottomSheetWalletConnectDappMessageBinding::bind)
    private val args by navArgs<WalletConnectDappMessageBottomSheetArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        with(binding) {
            closeButton.setOnClickListener { navBack() }
            descriptionTextView.text = args.message
            appIconImageView.loadPeerMetaIcon(args.peerMeta.icons.firstOrNull())
            appNameTextView.text = args.peerMeta.name
        }
    }
}
