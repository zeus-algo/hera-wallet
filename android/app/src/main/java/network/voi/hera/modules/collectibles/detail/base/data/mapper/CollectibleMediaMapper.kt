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

import network.voi.hera.modules.collectibles.detail.base.data.model.CollectibleMediaResponse
import network.voi.hera.modules.collectibles.detail.base.data.model.CollectibleMediaTypeExtensionResponse
import network.voi.hera.nft.data.model.CollectibleMediaTypeResponse
import network.voi.hera.nft.domain.model.BaseCollectibleMedia
import javax.inject.Inject

class CollectibleMediaMapper @Inject constructor() {

    fun mapToCollectibleMedia(mediaResponse: CollectibleMediaResponse): BaseCollectibleMedia {
        return when (mediaResponse.mediaType) {
            CollectibleMediaTypeResponse.IMAGE -> getCollectibleMediaForImage(mediaResponse)
            CollectibleMediaTypeResponse.VIDEO -> mapToVideoCollectibleMedia(mediaResponse)
            CollectibleMediaTypeResponse.AUDIO -> mapToAudioCollectibleMedia(mediaResponse)
            CollectibleMediaTypeResponse.UNKNOWN -> mapToUnsupportedCollectibleMedia(mediaResponse)
            else -> mapToUnsupportedCollectibleMedia(mediaResponse)
        }
    }

    private fun getCollectibleMediaForImage(mediaResponse: CollectibleMediaResponse): BaseCollectibleMedia {
        return when (mediaResponse.mediaTypeExtension) {
            CollectibleMediaTypeExtensionResponse.GIF -> mapToGifCollectibleMedia(mediaResponse)
            CollectibleMediaTypeExtensionResponse.WEBP -> mapToImageCollectibleMedia(mediaResponse)
            else -> mapToImageCollectibleMedia(mediaResponse)
        }
    }

    private fun mapToImageCollectibleMedia(
        mediaResponse: CollectibleMediaResponse
    ): BaseCollectibleMedia.ImageCollectibleMedia {
        return BaseCollectibleMedia.ImageCollectibleMedia(
            downloadUrl = mediaResponse.downloadUrl,
            previewUrl = mediaResponse.previewUrl
        )
    }

    private fun mapToGifCollectibleMedia(
        mediaResponse: CollectibleMediaResponse
    ): BaseCollectibleMedia.GifCollectibleMedia {
        return BaseCollectibleMedia.GifCollectibleMedia(
            downloadUrl = mediaResponse.downloadUrl,
            previewUrl = mediaResponse.previewUrl
        )
    }

    private fun mapToVideoCollectibleMedia(
        mediaResponse: CollectibleMediaResponse
    ): BaseCollectibleMedia.VideoCollectibleMedia {
        return BaseCollectibleMedia.VideoCollectibleMedia(
            downloadUrl = mediaResponse.downloadUrl,
            previewUrl = mediaResponse.previewUrl
        )
    }

    private fun mapToAudioCollectibleMedia(
        mediaResponse: CollectibleMediaResponse
    ): BaseCollectibleMedia.AudioCollectibleMedia {
        return BaseCollectibleMedia.AudioCollectibleMedia(
            downloadUrl = mediaResponse.downloadUrl,
            previewUrl = mediaResponse.previewUrl
        )
    }

    private fun mapToUnsupportedCollectibleMedia(
        mediaResponse: CollectibleMediaResponse
    ): BaseCollectibleMedia.UnsupportedCollectibleMedia {
        return BaseCollectibleMedia.UnsupportedCollectibleMedia(
            downloadUrl = mediaResponse.downloadUrl,
            previewUrl = mediaResponse.previewUrl
        )
    }
}
