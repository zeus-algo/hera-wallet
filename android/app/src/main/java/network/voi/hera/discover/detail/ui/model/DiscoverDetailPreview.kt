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

package network.voi.hera.discover.detail.ui.model

import androidx.navigation.NavDirections
import network.voi.hera.discover.common.ui.model.WebViewError
import network.voi.hera.discover.home.domain.model.TokenDetailInfo
import network.voi.hera.utils.Event
import network.voi.hera.utils.preference.ThemePreference

data class DiscoverDetailPreview(
    val themePreference: ThemePreference,
    val isLoading: Boolean = false,
    val loadingErrorEvent: Event<WebViewError>? = null,
    val reloadPageEvent: Event<Unit>? = null,
    val buySellActionEvent: Event<NavDirections>? = null,
    val tokenDetail: TokenDetailInfo
)
