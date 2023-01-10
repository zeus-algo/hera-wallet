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

package network.voi.hera.modules.accountdetail.ui.usecase

import androidx.navigation.NavDirections
import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.accountdetail.ui.AccountDetailFragmentDirections
import network.voi.hera.modules.accountdetail.ui.mapper.AccountDetailPreviewMapper
import network.voi.hera.modules.accountdetail.ui.model.AccountDetailPreview
import network.voi.hera.modules.swap.utils.SwapNavigationDestinationHelper
import network.voi.hera.utils.Event
import javax.inject.Inject

class AccountDetailPreviewUseCase @Inject constructor(
    private val accountDetailPreviewMapper: AccountDetailPreviewMapper,
    private val swapNavigationDestinationHelper: SwapNavigationDestinationHelper
) {

    fun getInitialPreview(): AccountDetailPreview {
        return accountDetailPreviewMapper.mapToAccountDetail(
            swapNavigationDirectionEvent = null,
            copyAssetIDToClipboardEvent = null
        )
    }

    suspend fun getSwapNavigationUpdatedPreview(
        accountAddress: String,
        previousState: AccountDetailPreview
    ): AccountDetailPreview {
        var swapNavDirection: NavDirections? = null
        swapNavigationDestinationHelper.getSwapNavigationDestination(
            accountAddress = accountAddress,
            onNavToIntroduction = {
                swapNavDirection = AccountDetailFragmentDirections
                    .actionAccountDetailFragmentToSwapIntroductionNavigation(accountAddress)
            },
            onNavToSwap = { address ->
                swapNavDirection = AccountDetailFragmentDirections
                    .actionAccountDetailFragmentToSwapNavigation(address)
            }
        )
        return swapNavDirection?.let { direction ->
            previousState.copy(swapNavigationDirectionEvent = Event(direction))
        } ?: previousState
    }

    fun getAssetLongClickUpdatedPreview(
        previousState: AccountDetailPreview,
        assetId: Long
    ): AccountDetailPreview {
        return with(previousState) {
            if (assetId != AssetInformation.ALGO_ID) {
                copy(copyAssetIDToClipboardEvent = Event(assetId))
            } else {
                previousState
            }
        }
    }
}
