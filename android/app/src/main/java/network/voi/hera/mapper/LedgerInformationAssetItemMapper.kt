/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package network.voi.hera.mapper

import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.models.LedgerInformationListItem
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class LedgerInformationAssetItemMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapTo(
        accountAssetData: BaseAccountAssetData.BaseOwnedAssetData.OwnedAssetData
    ): LedgerInformationListItem.AssetInformationItem {
        return LedgerInformationListItem.AssetInformationItem(
            id = accountAssetData.id,
            name = AssetName.create(accountAssetData.name),
            shortName = AssetName.createShortName(accountAssetData.shortName),
            isAmountInDisplayedCurrencyVisible = accountAssetData.isAmountInSelectedCurrencyVisible,
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                accountAssetData.verificationTier
            ),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(accountAssetData.id),
            formattedDisplayedCurrencyValue = accountAssetData.getSelectedCurrencyParityValue().getFormattedValue(),
            formattedAmount = accountAssetData.formattedAmount
        )
    }
}
