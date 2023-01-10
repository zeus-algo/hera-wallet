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

package network.voi.hera.nft.domain.usecase

import network.voi.hera.R
import network.voi.hera.mapper.ScreenStateMapper
import network.voi.hera.models.BaseAccountSelectionListItem
import network.voi.hera.models.ScreenState
import network.voi.hera.nft.mapper.CollectibleReceiverAccountSelectionPreviewMapper
import network.voi.hera.nft.ui.model.CollectibleReceiverAccountSelectionPreview
import network.voi.hera.usecase.AccountSelectionListUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class CollectibleReceiverAccountSelectionPreviewUseCase @Inject constructor(
    private val accountSelectionUseCase: AccountSelectionListUseCase,
    private val previewMapper: CollectibleReceiverAccountSelectionPreviewMapper,
    private val screenStateMapper: ScreenStateMapper
) {

    fun getAccountListItems() = flow<CollectibleReceiverAccountSelectionPreview> {
        emit(previewMapper.mapToLoadingPreview())
        val accountListItems = accountSelectionUseCase.createAccountSelectionListAccountItems(
            showHoldings = false,
            shouldIncludeWatchAccounts = false,
            showFailedAccounts = true
        )
        val screenState = createEmptyStateIfNeed(accountListItems)
        val isScreenStateViewVisible = screenState != null
        emit(
            previewMapper.mapToCollectibleReceiverAccountSelectionPreview(
                accountItems = accountListItems,
                screenState = screenState,
                isScreenStateViewVisible = isScreenStateViewVisible
            )
        )
    }

    private fun createEmptyStateIfNeed(accountItems: List<BaseAccountSelectionListItem>): ScreenState.CustomState? {
        return if (accountItems.isEmpty()) {
            screenStateMapper.mapToCustomState(
                title = R.string.no_account_found,
                description = R.string.you_need_to_create,
            )
        } else {
            null
        }
    }
}
