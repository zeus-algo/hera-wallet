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

package network.voi.hera.assetsearch.ui.mapper

import androidx.annotation.StringRes
import network.voi.hera.assetsearch.domain.model.BaseSearchedAsset
import network.voi.hera.assetsearch.ui.model.BaseAssetSearchListItem
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.ui.AccountAssetItemButtonState
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class BaseAssetSearchItemMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToAssetSearchItem(
        searchedAsset: BaseSearchedAsset.SearchedAsset,
        accountAssetItemButtonState: AccountAssetItemButtonState
    ): BaseAssetSearchListItem.AssetListItem.AssetSearchItem {
        return BaseAssetSearchListItem.AssetListItem.AssetSearchItem(
            assetId = searchedAsset.assetId,
            fullName = AssetName.create(searchedAsset.fullName),
            shortName = AssetName.createShortName(searchedAsset.shortName),
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                searchedAsset.verificationTier
            ),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(searchedAsset),
            accountAssetItemButtonState = accountAssetItemButtonState
        )
    }

    fun mapToCollectibleSearchItem(
        searchedCollectible: BaseSearchedAsset.SearchedCollectible,
        accountAssetItemButtonState: AccountAssetItemButtonState
    ): BaseAssetSearchListItem.AssetListItem.BaseCollectibleSearchListItem.ImageCollectibleSearchItem {
        return BaseAssetSearchListItem.AssetListItem.BaseCollectibleSearchListItem.ImageCollectibleSearchItem(
            assetId = searchedCollectible.assetId,
            fullName = AssetName.create(searchedCollectible.fullName),
            shortName = AssetName.createShortName(searchedCollectible.shortName),
            accountAssetItemButtonState = accountAssetItemButtonState,
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(searchedCollectible)
        )
    }

    fun mapToInfoViewItem(): BaseAssetSearchListItem.InfoViewItem {
        return BaseAssetSearchListItem.InfoViewItem
    }

    fun mapToSearchViewItem(@StringRes searchViewHintResId: Int): BaseAssetSearchListItem.SearchViewItem {
        return BaseAssetSearchListItem.SearchViewItem(searchViewHintResId = searchViewHintResId)
    }
}
