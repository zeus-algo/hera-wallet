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

package network.voi.hera.discover.dapp.ui.usecase

import android.content.SharedPreferences
import network.voi.hera.discover.common.ui.model.WebViewError
import network.voi.hera.discover.dapp.ui.model.DiscoverDappPreview
import network.voi.hera.utils.Event
import network.voi.hera.utils.preference.getSavedThemePreference
import javax.inject.Inject

class DiscoverDappPreviewUseCase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getInitialStatePreview(
        dappUrl: String,
        dappTitle: String
    ) = DiscoverDappPreview(
        themePreference = sharedPreferences.getSavedThemePreference(),
        isLoading = true,
        reloadPageEvent = Event(Unit),
        dappUrl = dappUrl,
        dappTitle = dappTitle
    )

    fun requestLoadHomepage(previousState: DiscoverDappPreview) = previousState.copy(
        isLoading = true,
        reloadPageEvent = Event(Unit)
    )

    fun onPreviousNavButtonClicked(previousState: DiscoverDappPreview) = previousState.copy(
        webViewGoBackEvent = Event(Unit)
    )

    fun onNextNavButtonClicked(previousState: DiscoverDappPreview) = previousState.copy(
        webViewGoForwardEvent = Event(Unit)
    )

    fun onPageRequested(previousState: DiscoverDappPreview) = previousState.copy(
        isLoading = true
    )

    fun onPageFinished(previousState: DiscoverDappPreview) = previousState.copy(
        isLoading = false
    )

    fun onError(previousState: DiscoverDappPreview) = previousState.copy(
        isLoading = false,
        loadingErrorEvent = Event(WebViewError.NO_CONNECTION)
    )

    fun onPageUrlChanged(previousState: DiscoverDappPreview) = previousState.copy(
        pageUrlChangedEvent = Event(Unit)
    )

    fun onHttpError(previousState: DiscoverDappPreview) = previousState.copy(
        isLoading = false,
        loadingErrorEvent = Event(WebViewError.HTTP_ERROR)
    )
}
