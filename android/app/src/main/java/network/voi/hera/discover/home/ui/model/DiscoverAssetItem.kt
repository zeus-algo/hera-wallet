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

package network.voi.hera.discover.home.ui.model

import network.voi.hera.assetsearch.ui.model.VerificationTierConfiguration
import network.voi.hera.models.RecyclerListItem
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.assetdrawable.BaseAssetDrawableProvider

data class DiscoverAssetItem(
    val assetId: Long,
    val fullName: AssetName,
    val shortName: AssetName,
    val prismUrl: String?,
    val baseAssetDrawableProvider: BaseAssetDrawableProvider,
    val verificationTierConfiguration: VerificationTierConfiguration?,
    val formattedUsdValue: String?
) : RecyclerListItem {
    override fun areItemsTheSame(other: RecyclerListItem): Boolean {
        return other is DiscoverAssetItem && other.assetId == assetId
    }

    override fun areContentsTheSame(other: RecyclerListItem): Boolean {
        return other is DiscoverAssetItem && other == this
    }
}
