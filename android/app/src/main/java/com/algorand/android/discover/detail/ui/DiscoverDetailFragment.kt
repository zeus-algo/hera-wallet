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

package com.algorand.android.discover.detail.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.R
import com.algorand.android.core.BaseActivity
import network.voi.hera.databinding.FragmentDiscoverDetailBinding
import com.algorand.android.discover.common.ui.BaseDiscoverFragment
import com.algorand.android.discover.common.ui.model.PeraWebViewClient
import com.algorand.android.discover.common.ui.model.WebViewError
import com.algorand.android.discover.detail.ui.model.DiscoverDetailPreview
import com.algorand.android.discover.home.domain.PeraMobileWebInterface
import com.algorand.android.discover.home.domain.PeraMobileWebInterface.Companion.WEB_INTERFACE_NAME
import com.algorand.android.discover.utils.getDiscoverAuthHeader
import com.algorand.android.discover.utils.getDiscoverTokenDetailUrl
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.utils.extensions.collectLatestOnLifecycle
import com.algorand.android.utils.extensions.hide
import com.algorand.android.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverDetailFragment :
    BaseDiscoverFragment(R.layout.fragment_discover_detail),
    PeraMobileWebInterface.WebInterfaceListener {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override lateinit var binding: FragmentDiscoverDetailBinding

    override val discoverViewModel: DiscoverDetailViewModel by viewModels()

    override val fragmentConfiguration = FragmentConfiguration(
        toolbarConfiguration = toolbarConfiguration,
        isBottomBarNeeded = false
    )

    private val discoverDetailPreviewCollector: suspend (DiscoverDetailPreview) -> Unit = { preview ->
        with(preview) {
            updateUi(this)
            loadingErrorEvent?.consume()?.run {
                handleLoadingError(this)
            } ?: run { if (!preview.isLoading && discoverViewModel.getLastError() == null) refreshViewFromError() }
            reloadPageEvent?.consume()?.run {
                loadUrl(preview)
            }
            buySellActionEvent?.consume()?.run {
                nav(this)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initUi()
    }

    override fun onReportActionFailed() {
        nav(
            DiscoverDetailFragmentDirections.actionDiscoverDetailFragmentToSingleButtonBottomSheetNavigation(
                titleAnnotatedString = getTitleForFailedReport(),
                descriptionAnnotatedString = getDescriptionForFailedReport(),
                buttonStringResId = R.string.got_it,
                drawableResId = R.drawable.ic_flag,
                drawableTintResId = R.color.negative,
                shouldDescriptionHasLinkMovementMethod = true
            )
        )
    }

    override fun bindWebView(view: View?) {
        view?.let { binding = FragmentDiscoverDetailBinding.bind(it) }
    }

    override fun handleTokenDetailActionButtonClick(data: String) {
        discoverViewModel.handleTokenDetailActionButtonClick(data)
    }

    private fun loadUrl(preview: DiscoverDetailPreview) {
        with(binding) {
            webView.post {
                preview.tokenDetail.tokenId?.let { tokenId ->
                    webView.loadUrl(
                        getDiscoverTokenDetailUrl(
                            themePreference = getWebViewThemeFromThemePreference(preview.themePreference),
                            tokenId = tokenId,
                            poolId = preview.tokenDetail.poolId,
                            currency = discoverViewModel.getPrimaryCurrencyId(),
                            locale = (activity as BaseActivity).getCurrentLanguage().language
                        ),
                        getDiscoverAuthHeader()
                    )
                }
            }
        }
    }

    private fun handleLoadingError(error: WebViewError) {
        with(binding) {
            webView.hide()
            discoverViewModel.saveLastError(error)
            errorScreenState.show()
            when (error) {
                WebViewError.HTTP_ERROR -> {
                    errorTitleTextView.text = getString(R.string.well_this_is_unexpected)
                    errorDescriptionTextView.text = getString(R.string.we_are_not_able_to_find)
                }
                WebViewError.NO_CONNECTION -> {
                    errorTitleTextView.text = getString(R.string.no_internet_connection)
                    errorDescriptionTextView.text = getString(R.string.you_dont_seem_to_be_connected)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUi() {
        with(binding) {
            tryAgainButton.setOnClickListener { discoverViewModel.reloadPage() }
            initWebview()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        with(binding) {
            val peraWebInterface = PeraMobileWebInterface.create(this@DiscoverDetailFragment)
            webView.addJavascriptInterface(peraWebInterface, WEB_INTERFACE_NAME)
            webView.webViewClient = PeraWebViewClient(peraWebViewClientListener)
        }
    }

    private fun updateUi(preview: DiscoverDetailPreview) {
        binding.loadingProgressBar.isVisible = preview.isLoading
        discoverViewModel.getLastError()?.let {
            handleLoadingError(it)
        }
    }

    private fun refreshViewFromError() {
        with(binding) {
            webView.show()
            errorScreenState.hide()
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            discoverViewModel.discoverDetailPreviewFlow,
            discoverDetailPreviewCollector
        )
    }
}
