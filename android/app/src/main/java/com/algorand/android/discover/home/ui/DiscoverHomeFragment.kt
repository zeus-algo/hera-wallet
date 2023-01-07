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

package com.algorand.android.discover.home.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.algorand.android.CoreMainActivity
import network.voi.hera.R
import com.algorand.android.core.BackPressedControllerComponent
import com.algorand.android.core.BaseActivity
import com.algorand.android.core.BottomNavigationBackPressedDelegate
import network.voi.hera.databinding.FragmentDiscoverHomeBinding
import com.algorand.android.discover.common.ui.BaseDiscoverFragment
import com.algorand.android.discover.common.ui.model.PeraWebViewClient
import com.algorand.android.discover.common.ui.model.WebViewError
import com.algorand.android.discover.home.domain.PeraMobileWebInterface
import com.algorand.android.discover.home.domain.PeraMobileWebInterface.Companion.WEB_INTERFACE_NAME
import com.algorand.android.discover.home.domain.model.TokenDetailInfo
import com.algorand.android.discover.home.ui.adapter.DiscoverAssetSearchAdapter
import com.algorand.android.discover.home.ui.model.DiscoverAssetItem
import com.algorand.android.discover.home.ui.model.DiscoverHomePreview
import com.algorand.android.discover.utils.getDiscoverAuthHeader
import com.algorand.android.discover.utils.getDiscoverHomeUrl
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.utils.extensions.collectLatestOnLifecycle
import com.algorand.android.utils.extensions.hide
import com.algorand.android.utils.extensions.show
import com.algorand.android.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverHomeFragment :
    BaseDiscoverFragment(R.layout.fragment_discover_home),
    PeraMobileWebInterface.WebInterfaceListener,
    BackPressedControllerComponent by BottomNavigationBackPressedDelegate() {

    private val toolbarConfiguration = ToolbarConfiguration()

    override val discoverViewModel: DiscoverHomeViewModel by viewModels()

    override lateinit var binding: FragmentDiscoverHomeBinding

    override val fragmentConfiguration = FragmentConfiguration(
        toolbarConfiguration = toolbarConfiguration,
        isBottomBarNeeded = true
    )

    private val discoverHomePreviewCollector: suspend (DiscoverHomePreview) -> Unit = { preview ->
        with(preview) {
            updateUi(preview)
            loadingErrorEvent?.consume()?.run {
                handleLoadingError(this)
            }
            tokenDetailScreenRequestEvent?.consume()?.run {
                navigateToTokenDetailScreen(this)
            }
            dappViewerScreenRequestEvent?.consume()?.run {
                this.url?.let { url ->
                    navigateToDappUrl(url, this.name)
                }
            }
            reloadPageEvent?.consume()?.run {
                loadDiscoverHomepage(preview)
            }
        }
    }

    private val loadStateFlowCollector: suspend (CombinedLoadStates) -> Unit = { combinedLoadStates ->
        val isListEmpty = discoverAssetSearchAdapter.itemCount == 0
        val isCurrentStateError = combinedLoadStates.refresh is LoadState.Error
        val isLoading = combinedLoadStates.refresh is LoadState.Loading
        discoverViewModel.updateSearchScreenLoadState(
            isListEmpty = isListEmpty,
            isCurrentStateError = isCurrentStateError,
            isLoading = isLoading
        )
    }

    private val assetSearchAdapterListener = object : DiscoverAssetSearchAdapter.DiscoverAssetSearchAdapterListener {
        override fun onNavigateToAssetDetail(assetId: Long) {
            discoverViewModel.navigateToAssetDetail(assetId)
        }
    }

    private val searchViewQueryTextListener = object : OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            view?.hideKeyboard()
            return false
        }

        override fun onQueryTextChange(query: String?): Boolean {
            query?.let {
                discoverViewModel.onQueryTextChange(it)
                return true
            } ?: return false
        }
    }

    private val discoverAssetSearchAdapter = DiscoverAssetSearchAdapter(assetSearchAdapterListener)

    private val discoverAssetSearchPaginationCollector:
        suspend (PagingData<DiscoverAssetItem>) -> Unit = { pagingData ->
        discoverAssetSearchAdapter.submitData(pagingData)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? CoreMainActivity)?.let { initBackPressedControllerComponent(it, viewLifecycleOwner) }
        initObservers()
        initUi()
    }

    override fun onReportActionFailed() {
        nav(
            DiscoverHomeFragmentDirections.actionDiscoverHomeFragmentToSingleButtonBottomSheetNavigation(
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
        view?.let { binding = FragmentDiscoverHomeBinding.bind(it) }
    }

    override fun pushTokenDetailScreen(data: String) {
        discoverViewModel.pushTokenDetailScreen(data)
    }

    override fun pushDappViewerScreen(data: String) {
        discoverViewModel.pushDappViewerScreen(data)
    }

    private fun initUi() {
        with(binding) {
            searchRecyclerView.adapter = discoverAssetSearchAdapter
            initWebview()
            searchView.setOnQueryTextListener(searchViewQueryTextListener)

            searchIconView.setOnClickListener {
                discoverViewModel.requestSearchVisible(true)
            }

            cancelSearchButton.setOnClickListener {
                discoverViewModel.requestSearchVisible(false)
            }
            tryAgainButton.setOnClickListener { discoverViewModel.requestLoadHomepage() }
        }
    }

    private fun updateUi(preview: DiscoverHomePreview) {
        with(preview) {
            updateSearchView(this)
            updateSearchListState(isListEmpty)
            updateLoadingProgressBar(isLoading)
            updateWebViewVisibility(this)
        }
    }

    private fun updateSearchView(preview: DiscoverHomePreview) {
        with(binding) {
            setSearchEnabled(true)
            if (!preview.isLoading) {
                searchActivatedGroup.isVisible = preview.isSearchActivated
                searchDeactivatedGroup.isVisible = !preview.isSearchActivated
            }
        }
    }

    private fun updateSearchListState(isEmpty: Boolean) {
        with(binding) {
            errorScreenState.isVisible = isEmpty
            if (isEmpty) {
                errorTitleTextView.text = getString(R.string.well_this_is_unexpected)
                errorDescriptionTextView.text = getString(R.string.we_are_not_able_to_find)
            }
        }
    }

    private fun updateWebViewVisibility(preview: DiscoverHomePreview) {
        binding.webView.isVisible = !preview.isSearchActivated
        discoverViewModel.getLastError()?.let {
            handleLoadingError(it)
        }
    }

    private fun updateLoadingProgressBar(isLoading: Boolean) {
        binding.loadingProgressBar.isVisible = isLoading
    }

    private fun handleLoadingError(error: WebViewError) {
        with(binding) {
            webView.hide()
            discoverViewModel.saveLastError(error)
            errorScreenState.show()
            setSearchEnabled(false)
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
    private fun initWebview() {
        with(binding) {
            val peraWebInterface = PeraMobileWebInterface.create(this@DiscoverHomeFragment)
            webView.addJavascriptInterface(peraWebInterface, WEB_INTERFACE_NAME)
            webView.webViewClient = PeraWebViewClient(peraWebViewClientListener)
        }
    }

    private fun loadDiscoverHomepage(preview: DiscoverHomePreview) {
        with(binding) {
            webView.post {
                webView.loadUrl(
                    // TODO Get locale from PeraLocaleProvider after merging TinymanSwapSprint2 branch
                    getDiscoverHomeUrl(
                        themePreference = getWebViewThemeFromThemePreference(preview.themePreference),
                        currency = discoverViewModel.getPrimaryCurrencyId(),
                        locale = (activity as BaseActivity).getCurrentLanguage().language
                    ),
                    getDiscoverAuthHeader()
                )
            }
        }
    }

    private fun setSearchEnabled(enabled: Boolean) {
        with(binding) {
            searchIconView.isEnabled = enabled
            cancelSearchButton.isEnabled = enabled
            searchView.isEnabled = enabled
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            discoverViewModel.discoverHomePreviewFlow,
            discoverHomePreviewCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            discoverViewModel.assetSearchPaginationFlow,
            discoverAssetSearchPaginationCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            discoverAssetSearchAdapter.loadStateFlow,
            loadStateFlowCollector
        )
    }

    private fun navigateToTokenDetailScreen(tokenDetail: TokenDetailInfo) {
        nav(
            DiscoverHomeFragmentDirections.actionDiscoverHomeFragmentToDiscoverDetailNavigation(
                tokenDetail = tokenDetail
            )
        )
    }

    private fun navigateToDappUrl(url: String, title: String?) {
        nav(
            DiscoverHomeFragmentDirections.actionDiscoverHomeFragmentToDiscoverDappFragment(
                dappUrl = url,
                dappTitle = title ?: ""
            )
        )
    }
}
