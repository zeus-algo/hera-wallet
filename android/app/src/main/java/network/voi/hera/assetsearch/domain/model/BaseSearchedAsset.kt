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

package network.voi.hera.assetsearch.domain.model

sealed class BaseSearchedAsset {
    abstract val assetId: Long
    abstract val fullName: String?
    abstract val shortName: String?
    abstract val logo: String?
    abstract val verificationTier: VerificationTier

    data class SearchedAsset(
        override val assetId: Long,
        override val fullName: String?,
        override val shortName: String?,
        override val logo: String?,
        override val verificationTier: VerificationTier
    ) : BaseSearchedAsset()

    data class SearchedCollectible(
        override val assetId: Long,
        override val fullName: String?,
        override val shortName: String?,
        override val logo: String?,
        override val verificationTier: VerificationTier,
        val collectible: CollectibleSearch?
    ) : BaseSearchedAsset()

    data class DiscoverSearchedAsset(
        override val assetId: Long,
        override val fullName: String?,
        override val shortName: String?,
        override val logo: String?,
        override val verificationTier: VerificationTier,
        val formattedUsdValue: String?
    ) : BaseSearchedAsset()

    data class DiscoverSearchedCollectible(
        override val assetId: Long,
        override val fullName: String?,
        override val shortName: String?,
        override val logo: String?,
        override val verificationTier: VerificationTier,
        val usdValue: String?,
        val collectible: CollectibleSearch?
    ) : BaseSearchedAsset()
}
