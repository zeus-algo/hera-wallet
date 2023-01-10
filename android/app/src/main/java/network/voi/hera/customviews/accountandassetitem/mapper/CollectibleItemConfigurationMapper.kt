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

package network.voi.hera.customviews.accountandassetitem.mapper

import network.voi.hera.customviews.accountandassetitem.model.BaseItemConfiguration
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class CollectibleItemConfigurationMapper @Inject constructor(
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider,
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider
) {

    fun mapTo(
        accountAssetData: BaseAccountAssetData
    ): BaseItemConfiguration.BaseAssetItemConfiguration.CollectibleItemConfiguration {
        return with(accountAssetData) {
            val ownedAssetData = this as? BaseAccountAssetData.BaseOwnedAssetData
            BaseItemConfiguration.BaseAssetItemConfiguration.CollectibleItemConfiguration(
                assetId = id,
                assetIconDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                primaryAssetName = AssetName.create(name),
                secondaryAssetName = AssetName.createShortName(shortName),
                primaryValueText = ownedAssetData?.getSelectedCurrencyParityValue()?.getFormattedCompactValue(),
                secondaryValueText = ownedAssetData?.parityValueInSelectedCurrency?.getFormattedValue(),
                verificationTierConfiguration =
                verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier)
            )
        }
    }
}
