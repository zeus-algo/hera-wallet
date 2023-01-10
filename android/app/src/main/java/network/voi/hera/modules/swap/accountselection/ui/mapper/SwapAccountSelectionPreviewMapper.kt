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

package network.voi.hera.modules.swap.accountselection.ui.mapper

import androidx.navigation.NavDirections
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetAction
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.modules.swap.accountselection.ui.model.SwapAccountSelectionPreview
import network.voi.hera.utils.Event
import javax.inject.Inject

class SwapAccountSelectionPreviewMapper @Inject constructor() {

    fun mapToSwapAccountSelectionPreview(
        accountListItems: List<BaseAccountSelectionListItem.BaseAccountItem>,
        isLoading: Boolean,
        navToSwapNavigationEvent: Event<NavDirections>?,
        errorEvent: Event<AnnotatedString>?,
        isEmptyStateVisible: Boolean,
        optInToAssetEvent: Event<AssetAction>?
    ): SwapAccountSelectionPreview {
        return SwapAccountSelectionPreview(
            accountListItems = accountListItems,
            isLoading = isLoading,
            navToSwapNavigationEvent = navToSwapNavigationEvent,
            errorEvent = errorEvent,
            isEmptyStateVisible = isEmptyStateVisible,
            optInToAssetEvent = optInToAssetEvent
        )
    }
}
