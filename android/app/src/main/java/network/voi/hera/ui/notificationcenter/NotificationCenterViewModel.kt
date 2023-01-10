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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import network.voi.hera.core.BaseViewModel
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.deviceregistration.domain.usecase.DeviceIdUseCase
import network.voi.hera.models.NotificationCenterPreview
import network.voi.hera.models.NotificationListItem
import network.voi.hera.modules.accounts.domain.usecase.NotificationStatusUseCase
import network.voi.hera.modules.deeplink.DeepLinkParser
import network.voi.hera.notification.PeraNotificationManager
import network.voi.hera.Repository.NotificationRepository
import network.voi.hera.usecase.NotificationCenterUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val peraNotificationManager: PeraNotificationManager,
    private val deviceIdUseCase: DeviceIdUseCase,
    private val notificationRepository: NotificationRepository,
    private val notificationCenterUseCase: NotificationCenterUseCase,
    private val notificationStatusUseCase: NotificationStatusUseCase,
    private val deepLinkParser: DeepLinkParser,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val baseAssetDrawableProviderDecider: AssetDrawableProviderDecider
) : BaseViewModel() {

    private var notificationDataSource: NotificationDataSource? = null

    val notificationPaginationFlow = Pager(
        config = PagingConfig(
            pageSize = DEFAULT_NOTIFICATION_COUNT
        ),
        pagingSourceFactory = {
            NotificationDataSource(
                notificationRepository = notificationRepository,
                deviceIdUseCase = deviceIdUseCase,
                deepLinkParser = deepLinkParser,
                simpleAssetDetailUseCase = simpleAssetDetailUseCase,
                baseAssetDrawableProviderDecider = baseAssetDrawableProviderDecider
            ).also { notificationDataSource = it }
        }
    ).flow
        .cachedIn(viewModelScope)
        .shareIn(viewModelScope, SharingStarted.Lazily)

    private val _notificationCenterPreviewFlow = MutableStateFlow<NotificationCenterPreview?>(null)
    val notificationCenterPreviewFlow: StateFlow<NotificationCenterPreview?> get() = _notificationCenterPreviewFlow

    fun refreshNotificationData(refreshDateTime: ZonedDateTime? = null) {
        if (refreshDateTime != null) {
            setLastRefreshedDateTime(refreshDateTime)
        }
        notificationDataSource?.invalidate()
    }

    fun getLastRefreshedDateTime(): ZonedDateTime {
        return notificationCenterUseCase.getLastRefreshedDateTime()
    }

    fun setLastRefreshedDateTime(zonedDateTime: ZonedDateTime) {
        notificationCenterUseCase.setLastRefreshedDateTime(zonedDateTime)
    }

    fun isAssetAvailableOnAccount(publicKey: String, assetId: Long) {
        viewModelScope.launch {
            notificationCenterUseCase.checkClickedNotificationItemType(assetId, publicKey).collect {
                _notificationCenterPreviewFlow.emit(it)
            }
        }
    }

    fun checkRequestedAssetType(accountAddress: String, assetId: Long) {
        viewModelScope.launch {
            notificationCenterUseCase.checkRequestedAssetType(assetId, accountAddress).collect {
                _notificationCenterPreviewFlow.emit(it)
            }
        }
    }

    fun onNotificationClickEvent(notificationListItem: NotificationListItem) {
        viewModelScope.launch {
            notificationCenterUseCase.onNotificationClickEvent(notificationListItem).collect {
                _notificationCenterPreviewFlow.emit(it)
            }
        }
    }

    fun isRefreshNeededLiveData(): LiveData<Boolean> {
        var newNotificationCount = 0
        return Transformations.map(peraNotificationManager.newNotificationLiveData) {
            newNotificationCount++
            return@map newNotificationCount > 1
        }
    }

    fun updateLastSeenNotification(notificationListItem: NotificationListItem?) {
        viewModelScope.launch {
            notificationStatusUseCase.updateLastSeenNotificationId(
                notificationListItem = notificationListItem ?: return@launch
            )
        }
    }

    companion object {
        private const val DEFAULT_NOTIFICATION_COUNT = 15
    }
}
