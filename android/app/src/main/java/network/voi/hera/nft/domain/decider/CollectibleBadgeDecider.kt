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

package network.voi.hera.nft.domain.decider

import network.voi.hera.R
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleAudioData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleImageData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleMixedData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedCollectibleVideoData
import network.voi.hera.models.BaseAccountAssetData.BaseOwnedAssetData.BaseOwnedCollectibleData.OwnedUnsupportedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingAdditionCollectibleData.AdditionAudioCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingAdditionCollectibleData.AdditionImageCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingAdditionCollectibleData.AdditionMixedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingAdditionCollectibleData.AdditionUnsupportedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingAdditionCollectibleData.AdditionVideoCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingDeletionCollectibleData.DeletionAudioCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingDeletionCollectibleData.DeletionImageCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingDeletionCollectibleData.DeletionMixedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingDeletionCollectibleData.DeletionUnsupportedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingDeletionCollectibleData.DeletionVideoCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingSendingCollectibleData.SendingAudioCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingSendingCollectibleData.SendingImageCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingSendingCollectibleData.SendingMixedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingSendingCollectibleData.SendingUnsupportedCollectibleData
import network.voi.hera.models.BaseAccountAssetData.PendingAssetData.BasePendingCollectibleData.PendingSendingCollectibleData.SendingVideoCollectibleData
import javax.inject.Inject

class CollectibleBadgeDecider @Inject constructor() {

    fun decideCollectibleBadgeResId(ownedCollectibleData: BaseOwnedCollectibleData): Int? {
        return when (ownedCollectibleData) {
            is OwnedCollectibleImageData -> null
            is OwnedCollectibleVideoData -> getVideoBadgeResId()
            is OwnedUnsupportedCollectibleData -> getUnsupportedBadgeResId()
            is OwnedCollectibleMixedData -> getMixedBadgeResId()
            is OwnedCollectibleAudioData -> getAudioBadgeResId()
        }
    }

    fun decidePendingCollectibleBadgeResId(pendingCollectibleData: BasePendingCollectibleData): Int? {
        return when (pendingCollectibleData) {
            is AdditionImageCollectibleData, is DeletionImageCollectibleData, is SendingImageCollectibleData -> {
                null
            }
            is AdditionVideoCollectibleData, is DeletionVideoCollectibleData, is SendingVideoCollectibleData -> {
                getVideoBadgeResId()
            }
            is AdditionMixedCollectibleData, is DeletionMixedCollectibleData, is SendingMixedCollectibleData -> {
                getMixedBadgeResId()
            }
            is AdditionUnsupportedCollectibleData, is DeletionUnsupportedCollectibleData,
            is SendingUnsupportedCollectibleData -> {
                getUnsupportedBadgeResId()
            }
            is AdditionAudioCollectibleData, is DeletionAudioCollectibleData,
            is SendingAudioCollectibleData -> getAudioBadgeResId()
        }
    }

    private fun getVideoBadgeResId(): Int = R.drawable.ic_badge_video

    private fun getAudioBadgeResId(): Int = R.drawable.ic_badge_audio

    private fun getMixedBadgeResId(): Int = R.drawable.ic_badge_mixed

    private fun getUnsupportedBadgeResId(): Int = R.drawable.ic_badge_unsupported
}
