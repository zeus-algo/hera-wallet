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

package network.voi.hera.modules.swap.assetswap.ui.mapper

import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.modules.swap.assetswap.ui.model.AssetSwapPreview
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class SelectedAssetDetailMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToSelectedAssetDetail(
        assetId: Long,
        formattedBalance: String,
        assetShortName: String?,
        verificationTier: VerificationTier?,
        assetDecimal: Int
    ): AssetSwapPreview.SelectedAssetDetail {
        return AssetSwapPreview.SelectedAssetDetail(
            assetId = assetId,
            formattedBalance = formattedBalance,
            assetShortName = AssetName.createShortName(assetShortName),
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                verificationTier
            ),
            assetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(assetId),
            assetDecimal = assetDecimal
        )
    }

    fun mapToSelectedAssetDetail(
        assetId: Long,
        formattedBalance: String,
        assetShortName: AssetName,
        verificationTier: VerificationTier?,
        assetDecimal: Int
    ): AssetSwapPreview.SelectedAssetDetail {
        return AssetSwapPreview.SelectedAssetDetail(
            assetId = assetId,
            formattedBalance = formattedBalance,
            assetShortName = assetShortName,
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                verificationTier
            ),
            assetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(assetId),
            assetDecimal = assetDecimal
        )
    }
}
