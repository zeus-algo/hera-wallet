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

package network.voi.hera.modules.walletconnectfallbackbrowser.ui.usecase

import android.content.pm.PackageManager
import network.voi.hera.core.BaseUseCase
import network.voi.hera.modules.walletconnectfallbackbrowser.domain.usecase.FallbackBrowserSelectionUseCase
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.mapper.FallbackBrowserSelectionPreviewMapper
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.mapper.WalletConnectFallbackBrowserItemMapper
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.model.FallbackBrowserListItem
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.model.FallbackBrowserSelectionPreview
import javax.inject.Inject

class FallbackBrowserSelectionPreviewUseCase @Inject constructor(
    private val fallbackBrowserSelectionUseCase: FallbackBrowserSelectionUseCase,
    private val fallbackBrowserSelectionPreviewMapper: FallbackBrowserSelectionPreviewMapper,
    private val walletConnectFallbackBrowserItemMapper: WalletConnectFallbackBrowserItemMapper,
    private val getInstalledAppPackageNameListUseCase: GetInstalledAppPackageNameListUseCase
) : BaseUseCase() {

    suspend fun getFallbackBrowserPreview(
        browserGroupResponse: String,
        packageManager: PackageManager?
    ): FallbackBrowserSelectionPreview {
        val installedApplicationPackageNameList =
            getInstalledAppPackageNameListUseCase.getInstalledAppsPackageNameListOrEmpty(packageManager)
        val browserList = getFallBrowserListItemList(
            browserGroupResponse = browserGroupResponse,
            installedApplicationPackageNameList = installedApplicationPackageNameList
        )
        return when {
            browserList.isEmpty() -> fallbackBrowserSelectionPreviewMapper.mapToNoBrowserFoundErrorState()
            browserList.size == 1 ->
                fallbackBrowserSelectionPreviewMapper.mapToSingleBrowserFoundState(browserList.first())
            else -> fallbackBrowserSelectionPreviewMapper.mapToSuccessState(browserList)
        }
    }

    fun getInitialLoadingPreview(): FallbackBrowserSelectionPreview {
        return fallbackBrowserSelectionPreviewMapper.mapToInitialLoadingState()
    }

    private suspend fun getFallBrowserListItemList(
        browserGroupResponse: String,
        installedApplicationPackageNameList: List<String>
    ): List<FallbackBrowserListItem> {
        return fallbackBrowserSelectionUseCase.getFilteredFallbackBrowserListByGroup(
            browserGroupResponse = browserGroupResponse,
            installedApplicationPackageNameList = installedApplicationPackageNameList
        ).map { walletConnectFallbackBrowserItemMapper.mapTo(it) }
    }
}
