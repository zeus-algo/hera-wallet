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

package network.voi.hera.ui.notificationcenter

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import network.voi.hera.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentNotificationCenterBinding
import network.voi.hera.models.AccountDetailTab
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.IconButton
import network.voi.hera.models.NotificationCenterPreview
import network.voi.hera.models.NotificationListItem
import network.voi.hera.models.ScreenState
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.time.ZonedDateTime

@AndroidEntryPoint
class NotificationCenterFragment : DaggerBaseFragment(R.layout.fragment_notification_center) {

    private val notificationCenterViewModel: NotificationCenterViewModel by viewModels()

    private val binding by viewBinding(FragmentNotificationCenterBinding::bind)

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.notifications,
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(
        toolbarConfiguration = toolbarConfiguration,
        isBottomBarNeeded = false
    )

    private val emptyState by lazy {
        ScreenState.CustomState(
            icon = R.drawable.ic_notification,
            title = R.string.no_current_notifications,
            description = R.string.your_recent_transactions
        )
    }

    private val notificationAdapter = NotificationAdapter(::onNewItemAddedToTop, ::onNotificationClick)

    private val notificationCenterPreviewCollector: suspend (NotificationCenterPreview?) -> Unit = {
        if (it != null) initPreview(it)
    }

    private val notificationPaginationCollector: suspend (PagingData<NotificationListItem>) -> Unit = { pagingData ->
        notificationAdapter.submitData(pagingData)
    }

    private val loadStateFlowCollector: suspend (CombinedLoadStates) -> Unit = { combinedLoadStates ->
        val isNotificationListEmpty = notificationAdapter.itemCount == 0
        val isCurrentStateError = combinedLoadStates.refresh is LoadState.Error
        val isLoading = combinedLoadStates.refresh is LoadState.Loading
        binding.swipeRefreshLayout.isRefreshing = isLoading
        when {
            isCurrentStateError -> {
                enableNotificationsErrorState((combinedLoadStates.refresh as LoadState.Error).error)
            }
            isLoading.not() && isNotificationListEmpty -> {
                binding.screenStateView.setupUi(emptyState)
            }
        }
        binding.notificationsRecyclerView.isInvisible =
            isCurrentStateError || isNotificationListEmpty
        binding.screenStateView.isVisible =
            (isCurrentStateError || isNotificationListEmpty) && isLoading.not()
    }

    private val isRefreshNeededObserver = Observer<Boolean> { isRefreshNeeded ->
        if (isRefreshNeeded) {
            refreshList(changeRefreshTime = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        initObservers()
        initUi()
    }

    private fun initUi() {
        binding.screenStateView.setOnNeutralButtonClickListener(::handleErrorButtonClick)
    }

    private fun initPreview(notificationCenterPreview: NotificationCenterPreview) {
        with(notificationCenterPreview) {
            errorMessageResId?.consume()?.run { showGlobalError(errorMessage = getString(this)) }
            onGoingAssetDetailEvent?.consume()?.run {
                navToAssetDetail(publicKey = first, assetId = second)
            }
            onGoingCollectibleDetailEvent?.consume()?.run {
                navToCollectibleDetail(publicKey = first, assetId = second)
            }
            onGoingAssetProfileEvent?.consume()?.run {
                navToAsaProfile(accountAddress = first, assetId = second)
            }
            onGoingCollectibleProfileEvent?.consume()?.run {
                navToCollectibleProfile(accountAddress = first, collectibleId = second)
            }
            onAssetSupportRequestEvent?.consume()?.run {
                notificationCenterViewModel.checkRequestedAssetType(accountAddress = first, assetId = second)
            }
            onHistoryNotAvailableEvent?.consume()?.run {
                onHistoryNotAvailable(publicKey = this)
            }
            onTransactionEvent?.consume()?.run {
                notificationCenterViewModel.isAssetAvailableOnAccount(publicKey = first, assetId = second)
            }
        }
    }

    private fun setupToolbar() {
        getAppToolbar()?.setEndButton(button = IconButton(R.drawable.ic_filter, onClick = ::onFilterClick))
    }

    private fun setupRecyclerView() {
        binding.notificationsRecyclerView.apply {
            notificationAdapter.lastRefreshedDateTime = notificationCenterViewModel.getLastRefreshedDateTime()
            notificationCenterViewModel.setLastRefreshedDateTime(ZonedDateTime.now())
            adapter = notificationAdapter
        }

        notificationAdapter.registerDataObserver()

        handleLoadState()

        binding.swipeRefreshLayout.setOnRefreshListener { refreshList(changeRefreshTime = true) }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            notificationCenterViewModel.notificationPaginationFlow,
            notificationPaginationCollector
        )
        viewLifecycleOwner.collectLatestOnLifecycle(
            notificationCenterViewModel.notificationCenterPreviewFlow,
            notificationCenterPreviewCollector
        )
        notificationCenterViewModel.isRefreshNeededLiveData().observe(viewLifecycleOwner, isRefreshNeededObserver)
    }

    override fun onResume() {
        super.onResume()
        refreshList(changeRefreshTime = false)
    }

    private fun handleLoadState() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            notificationAdapter.loadStateFlow,
            loadStateFlowCollector
        )
    }

    private fun enableNotificationsErrorState(throwable: Throwable) {
        if (throwable is IOException) {
            binding.screenStateView.setupUi(ScreenState.ConnectionError())
        } else {
            binding.screenStateView.setupUi(ScreenState.DefaultError())
        }
    }

    private fun handleErrorButtonClick() {
        refreshList()
    }

    private fun refreshList(changeRefreshTime: Boolean = false) {
        var refreshDateTime: ZonedDateTime? = null
        if (changeRefreshTime) {
            refreshDateTime = ZonedDateTime.now()
            notificationAdapter.lastRefreshedDateTime = refreshDateTime
        }
        notificationCenterViewModel.refreshNotificationData(refreshDateTime)
    }

    private fun onNotificationClick(notificationListItem: NotificationListItem) {
        notificationCenterViewModel.onNotificationClickEvent(notificationListItem)
    }

    private fun onNewItemAddedToTop() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            binding.notificationsRecyclerView.scrollToPosition(0)
            notificationCenterViewModel.updateLastSeenNotification(
                notificationListItem = notificationAdapter.snapshot().firstOrNull()
            )
        }
    }

    override fun onDestroyView() {
        notificationAdapter.unregisterDataObserver()
        super.onDestroyView()
    }

    private fun onFilterClick() {
        nav(
            NotificationCenterFragmentDirections.actionNotificationCenterFragmentToNotificationFilterFragment(
                showDoneButton = false
            )
        )
    }

    private fun navToCollectibleDetail(publicKey: String, assetId: Long) {
        nav(
            NotificationCenterFragmentDirections.actionNotificationCenterFragmentToCollectibleDetailFragment(
                publicKey = publicKey,
                collectibleAssetId = assetId
            )
        )
    }

    private fun navToAssetDetail(publicKey: String, assetId: Long) {
        nav(HomeNavigationDirections.actionGlobalAssetProfileNavigation(accountAddress = publicKey, assetId = assetId))
    }

    private fun navToAsaProfile(accountAddress: String, assetId: Long) {
        nav(
            NotificationCenterFragmentDirections.actionNotificationCenterFragmentToAsaProfileNavigation(
                accountAddress = accountAddress,
                assetId = assetId
            )
        )
    }

    private fun navToCollectibleProfile(accountAddress: String, collectibleId: Long) {
        nav(
            NotificationCenterFragmentDirections.actionNotificationCenterFragmentToCollectibleProfileNavigation(
                accountAddress = accountAddress,
                collectibleId = collectibleId
            )
        )
    }

    private fun onHistoryNotAvailable(publicKey: String) {
        navToAccountHistory(publicKey)
        showUnavailableTransactionHistoryError()
    }

    private fun navToAccountHistory(publicKey: String) {
        nav(
            NotificationCenterFragmentDirections.actionNotificationCenterFragmentToAccountDetailFragment(
                publicKey = publicKey,
                accountDetailTab = AccountDetailTab.HISTORY
            )
        )
    }

    private fun showUnavailableTransactionHistoryError() {
        showGlobalError(
            errorMessage = getString(R.string.the_history_for_this_spesific),
            title = getString(R.string.asset_history_not_available)
        )
    }
}
