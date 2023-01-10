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

package network.voi.hera.ui.accounts

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.viewModels
import com.algorand.algosdk.mobile.Mobile
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.DialogViewPassphraseBinding
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.disableScreenCapture
import network.voi.hera.utils.enableScreenCapture
import network.voi.hera.utils.setupMnemonic
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPassphraseBottomSheet : BaseBottomSheet(R.layout.dialog_view_passphrase) {

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.passphrase,
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    private val binding by viewBinding(DialogViewPassphraseBinding::bind)

    private val viewPassphraseViewModel: ViewPassphraseViewModel by viewModels()

    private var isScreenCaptureEnablingAllowed = true

    private val onWindowFocusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
        isScreenCaptureEnablingAllowed = hasFocus
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPassphraseToolbar.configure(toolbarConfiguration)
        setupPassphraseLayout()
    }

    private fun setupPassphraseLayout() {
        viewPassphraseViewModel.getAccountSecretKey()?.let {
            try {
                val mnemonic = Mobile.mnemonicFromPrivateKey(it) ?: throw Exception("Mnemonic cannot be null.")
                setupMnemonic(mnemonic, binding.passphraseLeftColumnTextView, binding.passphraseRightColumnTextView)
            } catch (exception: Exception) {
                navBack()
            }
        } ?: run { navBack() }
    }

    override fun onResume() {
        super.onResume()
        view?.viewTreeObserver?.addOnWindowFocusChangeListener(onWindowFocusChangeListener)
        activity?.disableScreenCapture()
    }

    override fun onStop() {
        if (isScreenCaptureEnablingAllowed) {
            activity?.enableScreenCapture()
        }
        view?.viewTreeObserver?.removeOnWindowFocusChangeListener(onWindowFocusChangeListener)
        super.onStop()
    }
}
