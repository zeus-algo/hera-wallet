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

package network.voi.hera.modules.swap.assetselection.base.ui.mapper

import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapAssetSelectionItem
import network.voi.hera.modules.swap.assetselection.toasset.domain.model.AvailableSwapAsset
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class SwapAssetSelectionItemMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val drawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToSwapAssetSelectionItem(
        availableSwapAsset: AvailableSwapAsset,
        formattedPrimaryValue: String,
        formattedSecondaryValue: String,
        arePrimaryAndSecondaryValueVisible: Boolean
    ): SwapAssetSelectionItem {
        return with(availableSwapAsset) {
            SwapAssetSelectionItem(
                assetId = assetId,
                assetFullName = assetName,
                assetShortName = assetShortName,
                formattedPrimaryValue = formattedPrimaryValue,
                formattedSecondaryValue = formattedSecondaryValue,
                arePrimaryAndSecondaryValueVisible = arePrimaryAndSecondaryValueVisible,
                assetDrawableProvider = drawableProviderDecider.getAssetDrawableProvider(assetId),
                verificationTier = verificationTierConfigurationDecider
                    .decideVerificationTierConfiguration(verificationTier)
            )
        }
    }

    fun mapToSwapAssetSelectionItem(
        ownedAssetData: BaseAccountAssetData.BaseOwnedAssetData,
        formattedPrimaryValue: String,
        formattedSecondaryValue: String,
        arePrimaryAndSecondaryValueVisible: Boolean
    ): SwapAssetSelectionItem {
        return SwapAssetSelectionItem(
            assetId = ownedAssetData.id,
            assetFullName = AssetName.create(ownedAssetData.name),
            assetShortName = AssetName.createShortName(ownedAssetData.shortName),
            formattedPrimaryValue = formattedPrimaryValue,
            formattedSecondaryValue = formattedSecondaryValue,
            arePrimaryAndSecondaryValueVisible = arePrimaryAndSecondaryValueVisible,
            assetDrawableProvider = drawableProviderDecider.getAssetDrawableProvider(ownedAssetData.id),
            verificationTier = verificationTierConfigurationDecider
                .decideVerificationTierConfiguration(ownedAssetData.verificationTier)
        )
    }
}
