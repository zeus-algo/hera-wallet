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

package network.voi.hera.modules.swap.confirmswap.ui.mapper

import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.modules.swap.confirmswap.ui.model.ConfirmSwapPreview
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class ConfirmSwapAssetDetailMapper @Inject constructor(
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider,
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider
) {

    fun mapToAssetDetail(
        assetId: Long,
        formattedAmount: String,
        formattedApproximateValue: String,
        shortName: AssetName,
        verificationTier: VerificationTier
    ): ConfirmSwapPreview.SwapAssetDetail {
        return ConfirmSwapPreview.SwapAssetDetail(
            formattedAmount = formattedAmount,
            formattedApproximateValue = formattedApproximateValue,
            shortName = shortName,
            assetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(assetId),
            verificationTierConfig = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                verificationTier
            )
        )
    }
}
