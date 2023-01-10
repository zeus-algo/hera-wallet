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

package network.voi.hera.modules.dapp.moonpay.ui.email

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import network.voi.hera.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentMoonpayEmailBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.hideKeyboard
import network.voi.hera.utils.requestFocusAndShowKeyboard
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoonpayEmailFragment : DaggerBaseFragment(R.layout.fragment_moonpay_email) {

    private val binding by viewBinding(FragmentMoonpayEmailBinding::bind)

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.buy_algo_with,
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        with(binding) {
            continueWithMoonpayButton.setOnClickListener { }
            emailInputLayout.requestFocusAndShowKeyboard()
        }
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboard()
    }
}
