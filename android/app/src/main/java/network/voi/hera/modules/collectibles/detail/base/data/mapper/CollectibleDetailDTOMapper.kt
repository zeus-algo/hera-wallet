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

package network.voi.hera.modules.collectibles.detail.base.data.mapper

import network.voi.hera.assetsearch.data.mapper.VerificationTierDTODecider
import network.voi.hera.assetsearch.domain.mapper.VerificationTierDecider
import network.voi.hera.models.AssetDetailResponse
import network.voi.hera.nft.domain.mapper.CollectibleMediaTypeMapper
import network.voi.hera.modules.collectibles.detail.base.data.model.CollectibleDetailDTO
import network.voi.hera.nft.data.mapper.CollectibleTraitMapper
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import javax.inject.Inject

class CollectibleDetailDTOMapper @Inject constructor(
    private val collectibleMediaTypeMapper: CollectibleMediaTypeMapper,
    private val collectibleTraitMapper: CollectibleTraitMapper,
    private val collectibleMediaMapper: CollectibleMediaMapper,
    private val verificationTierDTODecider: VerificationTierDTODecider,
    private val verificationTierDecider: VerificationTierDecider
) {

    fun mapToCollectibleDetail(response: AssetDetailResponse): CollectibleDetailDTO {
        // TODO Remove this after updating AssetFetchAndCacheUseCase TODOs
        val verificationTier = with(response) {
            val verificationTierDto = verificationTierDTODecider.decideVerificationTierDTO(verificationTier)
            verificationTierDecider.decideVerificationTier(verificationTierDto)
        }
        return CollectibleDetailDTO(
            collectibleAssetId = response.assetId,
            fullName = response.fullName,
            shortName = response.shortName,
            fractionDecimals = response.fractionDecimals ?: DEFAULT_ASSET_DECIMAL,
            usdValue = response.usdValue,
            assetCreator = response.assetCreator,
            mediaType = collectibleMediaTypeMapper.mapToCollectibleMediaType(response.collectible?.mediaType),
            primaryImageUrl = response.collectible?.primaryImageUrl,
            title = response.collectible?.title,
            collectionName = response.collectible?.collection?.collectionName,
            description = response.collectible?.description,
            traits = response.collectible?.traits?.map { collectibleTraitMapper.mapToTraits(it) }.orEmpty(),
            explorerUrl = response.explorerUrl,
            medias = response.collectible?.collectibleMedias?.map {
                collectibleMediaMapper.mapToCollectibleMedia(it)
            }.orEmpty(),
            totalSupply = response.totalSupply,
            verificationTier = verificationTier,
            logoUri = response.logoUri,
            logoSvgUri = response.logoSvgUri,
            projectName = response.projectName,
            projectUrl = response.projectUrl,
            discordUrl = response.discordUrl,
            telegramUrl = response.telegramUrl,
            twitterUsername = response.twitterUsername,
            assetDescription = response.description,
            url = response.url,
            maxSupply = response.maxSupply,
            last24HoursAlgoPriceChangePercentage = response.last24HoursAlgoPriceChangePercentage,
            isAvailableOnDiscoverMobile = response.isAvailableOnDiscoverMobile
        )
    }
}
