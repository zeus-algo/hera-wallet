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

package com.algorand.android.ui.lock

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.R
import com.algorand.android.core.BaseBottomSheet
import com.algorand.android.customviews.DialPadView
import com.algorand.android.customviews.SixDigitPasswordView
import network.voi.hera.databinding.BottomSheetViewPassphraseLockBinding
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.ui.accounts.ViewPassphraseLockViewModel
import com.algorand.android.utils.viewbinding.viewBinding

abstract class BasePasscodeVerificationBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_view_passphrase_lock) {

    protected open val titleResId: Int? = null

    private var lockHandler: Handler? = null

    private val binding by viewBinding(BottomSheetViewPassphraseLockBinding::bind)

    private val viewPassphraseLockViewModel: ViewPassphraseLockViewModel by viewModels()

    private val pinCodeListener = object : SixDigitPasswordView.Listener {
        override fun onPinCodeCompleted(pinCode: String) {
            if (viewPassphraseLockViewModel.getPassword() == pinCode) {
                onPasscodeSuccess()
            } else {
                onPasscodeError()
            }
        }
    }

    private val dialPadListener = object : DialPadView.DialPadListener {
        override fun onNumberClick(number: Int) {
            binding.viewPassphraseLockSixDigitPasswordView.onNewDigit(number)
        }

        override fun onBackspaceClick() {
            binding.viewPassphraseLockSixDigitPasswordView.removeLastDigit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolbar()
        with(binding) {
            viewPassphraseLockDialPadView.setDialPadListener(dialPadListener)
            viewPassphraseLockSixDigitPasswordView.setListener(pinCodeListener)
        }
    }

    private fun configureToolbar() {
        val toolbarConfiguration = ToolbarConfiguration(
            backgroundColor = R.color.primary_background,
            startIconResId = R.drawable.ic_left_arrow,
            titleResId = titleResId,
            startIconClick = ::navBack
        )
        binding.customToolbar.configure(toolbarConfiguration)
    }

    override fun onStart() {
        super.onStart()
        if (viewPassphraseLockViewModel.isNotPasswordChosen()) {
            onPasscodeSuccess()
        }
    }

    override fun onPause() {
        binding.viewPassphraseLockSixDigitPasswordView.cancelAnimations()
        super.onPause()
        lockHandler?.removeCallbacksAndMessages(null)
    }

    protected open fun onPasscodeSuccess() {
        navBack()
    }

    protected open fun onPasscodeError() {
        binding.viewPassphraseLockSixDigitPasswordView.clearWithAnimation()
    }
}
