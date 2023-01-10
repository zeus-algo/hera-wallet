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

package network.voi.hera.modules.assets.profile.detail.ui.mapper

import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.modules.assets.profile.detail.ui.model.AssetDetailPreview
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import java.math.BigDecimal
import javax.inject.Inject

class AssetDetailPreviewMapper @Inject constructor(
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider,
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDetailMarketInformationDecider: AssetDetailMarketInformationDecider
) {

    @SuppressWarnings("LongParameterList")
    fun mapToAssetDetailPreview(
        baseOwnedAssetDetail: BaseAccountAssetData.BaseOwnedAssetData,
        accountAddress: String,
        accountName: String,
        accountType: Account.Type?,
        canAccountSignTransaction: Boolean,
        isQuickActionButtonsVisible: Boolean,
        isSwapButtonSelected: Boolean,
        isMarketInformationVisible: Boolean,
        last24HoursChange: BigDecimal?,
        formattedAssetPrice: String?
    ): AssetDetailPreview {
        return with(baseOwnedAssetDetail) {
            AssetDetailPreview(
                assetId = id,
                assetFullName = AssetName.create(name),
                isAlgo = isAlgo,
                formattedPrimaryValue = formattedAmount,
                formattedSecondaryValue = getSelectedCurrencyParityValue().getFormattedValue(),
                accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(accountType),
                accountDisplayName = AccountDisplayName.create(accountAddress, accountName, accountType),
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                assetPrismUrl = prismUrl,
                verificationTierConfiguration =
                verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier),
                isQuickActionButtonsVisible = canAccountSignTransaction && isQuickActionButtonsVisible,
                isSwapButtonSelected = isSwapButtonSelected,
                isMarketInformationVisible = isMarketInformationVisible,
                isChangePercentageVisible = assetDetailMarketInformationDecider.decideIsChangePercentageVisible(
                    last24HoursChange
                ),
                changePercentage = last24HoursChange,
                formattedAssetPrice = formattedAssetPrice.orEmpty(),
                changePercentageIcon = assetDetailMarketInformationDecider.decideIconResOfChangePercentage(
                    last24HoursChange
                ),
                changePercentageTextColor = assetDetailMarketInformationDecider.decideTextColorResOfChangePercentage(
                    last24HoursChange
                )
            )
        }
    }
}
