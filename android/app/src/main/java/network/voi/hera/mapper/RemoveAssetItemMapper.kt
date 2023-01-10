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

import androidx.annotation.StringRes
import network.voi.hera.decider.AssetDrawableProviderDecider
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleAudioData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleImageData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleMixedData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleVideoData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedUnsupportedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.OwnedAssetData
import network.voi.hera.models.BaseRemoveAssetItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem.RemoveCollectibleAudioItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem.RemoveCollectibleImageItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem.RemoveCollectibleMixedItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem.RemoveCollectibleVideoItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem.RemoveNotSupportedCollectibleItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.RemoveAssetItem
import network.voi.hera.models.BaseRemoveAssetItem.DescriptionViewItem
import network.voi.hera.models.BaseRemoveAssetItem.SearchViewItem
import network.voi.hera.models.BaseRemoveAssetItem.TitleViewItem
import network.voi.hera.models.ScreenState
import network.voi.hera.models.ui.AccountAssetItemButtonState
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.utils.AssetName
import javax.inject.Inject

class RemoveAssetItemMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToRemoveAssetItem(
        ownedAssetData: OwnedAssetData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveAssetItem {
        return with(ownedAssetData) {
            RemoveAssetItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = if (isAmountInSelectedCurrencyVisible) {
                    parityValueInSelectedCurrency.getFormattedCompactValue()
                } else {
                    null
                },
                verificationTierConfiguration =
                verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier),
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToRemoveCollectibleImageItem(
        ownedCollectibleImageData: OwnedCollectibleImageData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveCollectibleImageItem {
        return with(ownedCollectibleImageData) {
            RemoveCollectibleImageItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = if (isAmountInSelectedCurrencyVisible) {
                    parityValueInSelectedCurrency.getFormattedCompactValue()
                } else {
                    null
                },
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                optedInAtRound = optedInAtRound,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToRemoveCollectibleVideoItem(
        ownedCollectibleImageData: OwnedCollectibleVideoData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveCollectibleVideoItem {
        return with(ownedCollectibleImageData) {
            RemoveCollectibleVideoItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = if (isAmountInSelectedCurrencyVisible) {
                    parityValueInSelectedCurrency.getFormattedCompactValue()
                } else {
                    null
                },
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                optedInAtRound = optedInAtRound,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapTo(
        ownedCollectibleAudioData: OwnedCollectibleAudioData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveCollectibleAudioItem {
        return with(ownedCollectibleAudioData) {
            RemoveCollectibleAudioItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = parityValueInSelectedCurrency.getFormattedCompactValue(),
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                optedInAtRound = optedInAtRound,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToRemoveCollectibleMixedItem(
        ownedCollectibleMixedData: OwnedCollectibleMixedData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveCollectibleMixedItem {
        return with(ownedCollectibleMixedData) {
            RemoveCollectibleMixedItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = if (isAmountInSelectedCurrencyVisible) {
                    parityValueInSelectedCurrency.getFormattedCompactValue()
                } else {
                    null
                },
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                optedInAtRound = optedInAtRound,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToRemoveNotSupportedCollectibleItem(
        ownedUnsupportedCollectibleData: OwnedUnsupportedCollectibleData,
        actionItemButtonState: AccountAssetItemButtonState
    ): RemoveNotSupportedCollectibleItem {
        return with(ownedUnsupportedCollectibleData) {
            RemoveNotSupportedCollectibleItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.create(shortName),
                amount = amount,
                creatorPublicKey = creatorPublicKey,
                decimals = decimals,
                formattedAmount = formattedAmount,
                formattedCompactAmount = formattedCompactAmount,
                formattedSelectedCurrencyValue = parityValueInSelectedCurrency.getFormattedValue(),
                formattedSelectedCurrencyCompactValue = if (isAmountInSelectedCurrencyVisible) {
                    parityValueInSelectedCurrency.getFormattedCompactValue()
                } else {
                    null
                },
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                actionItemButtonState = actionItemButtonState,
                optedInAtRound = optedInAtRound,
                amountInPrimaryCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToTitleItem(@StringRes titleTextRes: Int): TitleViewItem {
        return TitleViewItem(titleTextRes)
    }

    fun mapToDescriptionItem(@StringRes descriptionTextRes: Int): DescriptionViewItem {
        return DescriptionViewItem(descriptionTextRes)
    }

    fun mapToSearchItem(@StringRes searchViewHintResId: Int): SearchViewItem {
        return SearchViewItem(searchViewHintResId)
    }

    fun mapToScreenStateItem(screenState: ScreenState): BaseRemoveAssetItem.ScreenStateItem {
        return BaseRemoveAssetItem.ScreenStateItem(screenState)
    }
}
