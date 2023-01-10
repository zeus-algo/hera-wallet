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

package network.voi.hera.modules.dapp.moonpay.ui.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentMoonpayIntroBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.StatusBarConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.dapp.moonpay.data.remote.model.SignMoonpayUrlResponse
import network.voi.hera.modules.dapp.moonpay.ui.accountselection.MoonpayAccountSelectionFragment
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.browser.openUrl
import network.voi.hera.utils.startSavedStateListener
import network.voi.hera.utils.useSavedStateValue
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoonpayIntroFragment : BaseFragment(R.layout.fragment_moonpay_intro) {

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.buy_algo_with,
        startIconResId = R.drawable.ic_close,
        backgroundColor = R.color.moonpay,
        titleColor = R.color.white,
        startIconColor = R.color.white,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val args by navArgs<MoonpayIntroFragmentArgs>()
    private val moonpayIntroViewModel by viewModels<MoonpayIntroViewModel>()
    private val binding by viewBinding(FragmentMoonpayIntroBinding::bind)

    private val statusBarConfiguration = StatusBarConfiguration(backgroundColor = R.color.moonpay)

    private val signMoonpayUrlCollector: suspend (SignMoonpayUrlResponse?) -> Unit = {
        openSignedMoonpayUrl(it?.moonpayUrl)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initSavedStateListener()
        initObservers()
    }

    private fun initUi() {
        changeStatusBarConfiguration(statusBarConfiguration)
        binding.buyAlgoButton.setOnClickListener { onBuyAlgoButtonClick() }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            moonpayIntroViewModel.signMoonpayUrlFlow,
            signMoonpayUrlCollector
        )
    }

    private fun initSavedStateListener() {
        startSavedStateListener(R.id.moonpayIntroFragment) {
            useSavedStateValue<String?>(MoonpayAccountSelectionFragment.ACCOUNT_SELECTION_RESULT_KEY) {
                signMoonpayUrl(it)
            }
        }
    }

    private fun onBuyAlgoButtonClick() {
        moonpayIntroViewModel.logBuyAlgoTapEvent()
        if (moonpayIntroViewModel.isMainNet().not()) {
            showGlobalError(getString(R.string.you_can_not_purchase), getString(R.string.not_available))
            return
        }
        if (args.walletAddress == null) {
            navToAccountSelectionFragment()
        } else {
            signMoonpayUrl(args.walletAddress)
        }
    }

    private fun signMoonpayUrl(publicKey: String?) {
        if (publicKey != null) {
            moonpayIntroViewModel.signMoonpayUrl(publicKey)
        }
    }

    private fun navToAccountSelectionFragment() {
        nav(MoonpayIntroFragmentDirections.actionMoonpayIntroFragmentToMoonpayAccountSelectionFragment())
    }

    private fun openSignedMoonpayUrl(url: String?) {
        if (url != null) {
            navBack()
            context?.openUrl(url)
        }
    }
}
