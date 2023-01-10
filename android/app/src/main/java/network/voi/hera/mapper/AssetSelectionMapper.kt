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

package network.voi.hera.mapper

import network.voi.hera.customviews.accountandassetitem.model.BaseItemConfiguration
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleAudioData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleImageData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleMixedData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleVideoData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedUnsupportedCollectibleData
import network.voi.hera.models.BaseSelectAssetItem.BaseSelectCollectibleItem.SelectAudioCollectibleItem
import network.voi.hera.models.BaseSelectAssetItem.BaseSelectCollectibleItem.SelectCollectibleImageItem
import network.voi.hera.models.BaseSelectAssetItem.BaseSelectCollectibleItem.SelectMixedCollectibleItem
import network.voi.hera.models.BaseSelectAssetItem.BaseSelectCollectibleItem.SelectNotSupportedCollectibleItem
import network.voi.hera.models.BaseSelectAssetItem.BaseSelectCollectibleItem.SelectVideoCollectibleItem
import network.voi.hera.models.BaseSelectAssetItem.SelectAssetItem
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class AssetSelectionMapper @Inject constructor(
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToAssetItem(
        assetItemConfiguration: BaseItemConfiguration.BaseAssetItemConfiguration.AssetItemConfiguration
    ): SelectAssetItem {
        return SelectAssetItem(assetItemConfiguration)
    }

    fun mapToCollectibleImageItem(
        ownedCollectibleImageData: OwnedCollectibleImageData
    ): SelectCollectibleImageItem {
        return SelectCollectibleImageItem(
            id = ownedCollectibleImageData.id,
            isAlgo = ownedCollectibleImageData.isAlgo,
            shortName = ownedCollectibleImageData.shortName,
            name = ownedCollectibleImageData.name,
            amount = ownedCollectibleImageData.amount,
            formattedAmount = ownedCollectibleImageData.formattedAmount,
            formattedCompactAmount = ownedCollectibleImageData.formattedCompactAmount,
            formattedSelectedCurrencyValue = ownedCollectibleImageData.parityValueInSelectedCurrency
                .getFormattedValue(),
            formattedSelectedCurrencyCompactValue = ownedCollectibleImageData.parityValueInSelectedCurrency
                .getFormattedCompactValue(),
            isAmountInSelectedCurrencyVisible = ownedCollectibleImageData.isAmountInSelectedCurrencyVisible,
            avatarDisplayText = AssetName.create(ownedCollectibleImageData.name),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(
                assetId = ownedCollectibleImageData.id
            ),
            optedInAtRound = ownedCollectibleImageData.optedInAtRound,
            amountInSelectedCurrency = ownedCollectibleImageData.parityValueInSelectedCurrency.amountAsCurrency
        )
    }

    fun mapToCollectibleVideoItem(
        ownedCollectibleVideoData: OwnedCollectibleVideoData
    ): SelectVideoCollectibleItem {
        return SelectVideoCollectibleItem(
            id = ownedCollectibleVideoData.id,
            isAlgo = ownedCollectibleVideoData.isAlgo,
            shortName = ownedCollectibleVideoData.shortName,
            name = ownedCollectibleVideoData.name,
            amount = ownedCollectibleVideoData.amount,
            formattedAmount = ownedCollectibleVideoData.formattedAmount,
            formattedCompactAmount = ownedCollectibleVideoData.formattedCompactAmount,
            formattedSelectedCurrencyValue = ownedCollectibleVideoData.parityValueInSelectedCurrency
                .getFormattedValue(),
            formattedSelectedCurrencyCompactValue = ownedCollectibleVideoData.parityValueInSelectedCurrency
                .getFormattedCompactValue(),
            isAmountInSelectedCurrencyVisible = ownedCollectibleVideoData.isAmountInSelectedCurrencyVisible,
            avatarDisplayText = AssetName.create(ownedCollectibleVideoData.name),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(
                assetId = ownedCollectibleVideoData.id
            ),
            optedInAtRound = ownedCollectibleVideoData.optedInAtRound,
            amountInSelectedCurrency = ownedCollectibleVideoData.parityValueInSelectedCurrency.amountAsCurrency
        )
    }

    fun mapToCollectibleAudioItem(
        ownedCollectibleAudioData: OwnedCollectibleAudioData
    ): SelectAudioCollectibleItem {
        return SelectAudioCollectibleItem(
            id = ownedCollectibleAudioData.id,
            isAlgo = ownedCollectibleAudioData.isAlgo,
            shortName = ownedCollectibleAudioData.shortName,
            name = ownedCollectibleAudioData.name,
            amount = ownedCollectibleAudioData.amount,
            formattedAmount = ownedCollectibleAudioData.formattedAmount,
            formattedCompactAmount = ownedCollectibleAudioData.formattedCompactAmount,
            formattedSelectedCurrencyValue = ownedCollectibleAudioData.parityValueInSelectedCurrency
                .getFormattedValue(),
            formattedSelectedCurrencyCompactValue = ownedCollectibleAudioData.parityValueInSelectedCurrency
                .getFormattedCompactValue(),
            isAmountInSelectedCurrencyVisible = ownedCollectibleAudioData.isAmountInSelectedCurrencyVisible,
            avatarDisplayText = AssetName.create(ownedCollectibleAudioData.name),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(
                assetId = ownedCollectibleAudioData.id
            ),
            optedInAtRound = ownedCollectibleAudioData.optedInAtRound,
            amountInSelectedCurrency = ownedCollectibleAudioData.parityValueInSelectedCurrency.amountAsCurrency
        )
    }

    fun mapToCollectibleMixedItem(
        ownedCollectibleMixedData: OwnedCollectibleMixedData
    ): SelectMixedCollectibleItem {
        return SelectMixedCollectibleItem(
            id = ownedCollectibleMixedData.id,
            isAlgo = ownedCollectibleMixedData.isAlgo,
            shortName = ownedCollectibleMixedData.shortName,
            name = ownedCollectibleMixedData.name,
            amount = ownedCollectibleMixedData.amount,
            formattedAmount = ownedCollectibleMixedData.formattedAmount,
            formattedCompactAmount = ownedCollectibleMixedData.formattedCompactAmount,
            formattedSelectedCurrencyValue = ownedCollectibleMixedData.parityValueInSelectedCurrency
                .getFormattedValue(),
            formattedSelectedCurrencyCompactValue = ownedCollectibleMixedData.parityValueInSelectedCurrency
                .getFormattedCompactValue(),
            isAmountInSelectedCurrencyVisible = ownedCollectibleMixedData.isAmountInSelectedCurrencyVisible,
            avatarDisplayText = AssetName.create(ownedCollectibleMixedData.name),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(
                assetId = ownedCollectibleMixedData.id
            ),
            optedInAtRound = ownedCollectibleMixedData.optedInAtRound,
            amountInSelectedCurrency = ownedCollectibleMixedData.parityValueInSelectedCurrency.amountAsCurrency
        )
    }

    fun mapToCollectibleNotSupportedItem(
        ownedUnsupportedCollectibleData: OwnedUnsupportedCollectibleData
    ): SelectNotSupportedCollectibleItem {
        return SelectNotSupportedCollectibleItem(
            id = ownedUnsupportedCollectibleData.id,
            isAlgo = ownedUnsupportedCollectibleData.isAlgo,
            shortName = ownedUnsupportedCollectibleData.shortName,
            name = ownedUnsupportedCollectibleData.name,
            amount = ownedUnsupportedCollectibleData.amount,
            formattedAmount = ownedUnsupportedCollectibleData.formattedAmount,
            formattedCompactAmount = ownedUnsupportedCollectibleData.formattedCompactAmount,
            formattedSelectedCurrencyValue = ownedUnsupportedCollectibleData.parityValueInSelectedCurrency
                .getFormattedValue(),
            formattedSelectedCurrencyCompactValue = ownedUnsupportedCollectibleData.parityValueInSelectedCurrency
                .getFormattedCompactValue(),
            isAmountInSelectedCurrencyVisible = ownedUnsupportedCollectibleData.isAmountInSelectedCurrencyVisible,
            avatarDisplayText = AssetName.create(ownedUnsupportedCollectibleData.name),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(
                assetId = ownedUnsupportedCollectibleData.id
            ),
            optedInAtRound = ownedUnsupportedCollectibleData.optedInAtRound,
            amountInSelectedCurrency = ownedUnsupportedCollectibleData.parityValueInSelectedCurrency.amountAsCurrency
        )
    }
}
