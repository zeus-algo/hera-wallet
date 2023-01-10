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

package network.voi.hera.modules.swap.assetselection.fromasset.ui.usecase

import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.OwnedAssetData
import network.voi.hera.modules.swap.assetselection.base.ui.mapper.SwapAssetSelectionItemMapper
import network.voi.hera.modules.swap.assetselection.base.ui.mapper.SwapAssetSelectionPreviewMapper
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapAssetSelectionItem
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapAssetSelectionPreview
import network.voi.hera.usecase.AccountAssetDataUseCase
import network.voi.hera.utils.doesAssetPassSearchFilter
import network.voi.hera.utils.isGreaterThan
import java.math.BigInteger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SwapFromAssetSelectionPreviewUseCase @Inject constructor(
    private val accountAssetDataUseCase: AccountAssetDataUseCase,
    private val swapAssetSelectionItemMapper: SwapAssetSelectionItemMapper,
    private val swapAssetSelectionPreviewMapper: SwapAssetSelectionPreviewMapper
) {

    suspend fun getSwapAssetSelectionPreview(
        accountAddress: String,
        query: String?
    ): Flow<SwapAssetSelectionPreview> = flow {
        val accountAssets = accountAssetDataUseCase.getAccountOwnedAssetData(accountAddress, includeAlgo = true)
        val balanceFilteredAccountAssetList = accountAssets.filter { ownedAssetData ->
            if (ownedAssetData.isAlgo && query == null) return@filter true
            (ownedAssetData.amount isGreaterThan BigInteger.ZERO) && (doesAssetPassSearchFilter(query, ownedAssetData))
        }
        val swapAssetSelectionItemList = createSwapAssetSelectionItemList(balanceFilteredAccountAssetList)

        val preview = swapAssetSelectionPreviewMapper.mapToSwapAssetSelectionPreview(
            swapAssetSelectionItemList = swapAssetSelectionItemList,
            isLoading = false,
            screenState = null,
            navigateToAssetAdditionBottomSheetEvent = null,
            assetSelectedEvent = null
        )
        emit(preview)
    }

    private fun createSwapAssetSelectionItemList(
        filteredAccountAssetList: List<OwnedAssetData>
    ): List<SwapAssetSelectionItem> {
        return filteredAccountAssetList.map { ownedAssetData ->
            swapAssetSelectionItemMapper.mapToSwapAssetSelectionItem(
                ownedAssetData = ownedAssetData,
                formattedPrimaryValue = ownedAssetData.formattedCompactAmount,
                formattedSecondaryValue = ownedAssetData.parityValueInSelectedCurrency.getFormattedCompactValue(),
                arePrimaryAndSecondaryValueVisible = true
            )
        }
    }
}
