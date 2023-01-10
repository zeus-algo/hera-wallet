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

package network.voi.hera.modules.collectibles.detail.base.ui.viewpager

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import network.voi.hera.models.BaseDiffUtil
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.AUDIO
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.GIF
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.IMAGE
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.NO_MEDIA
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.UNSUPPORTED
import network.voi.hera.modules.collectibles.detail.base.ui.model.BaseCollectibleMediaItem.ItemType.VIDEO

class CollectibleMediaAdapter(
    private val listener: MediaClickListener
) : ListAdapter<BaseCollectibleMediaItem, BaseViewHolder<BaseCollectibleMediaItem>>(BaseDiffUtil()) {

    private val mediaListener = object : BaseCollectibleMediaViewHolder.Listener {
        override fun on3DModeClick(imageUrl: String?) {
            listener.on3dModeClick(imageUrl)
        }

        override fun onImageMediaClick(mediaUri: String?, cachedMediaUri: String, collectibleImageView: View) {
            listener.onImageMediaClick(mediaUri, collectibleImageView, cachedMediaUri)
        }

        override fun onVideoMediaClick(imageUrl: String?) {
            listener.onVideoMediaClick(imageUrl)
        }

        override fun onAudioMediaClick(imageUrl: String?) {
            listener.onAudioMediaClick(imageUrl)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseCollectibleMediaItem> {
        return when (viewType) {
            IMAGE.ordinal -> createImageMediaViewHolder(parent)
            VIDEO.ordinal -> createVideoMediaViewHolder(parent)
            UNSUPPORTED.ordinal -> createUnsupportedMediaViewHolder(parent)
            GIF.ordinal -> createGifMediaViewHolder(parent)
            NO_MEDIA.ordinal -> createNoMediaViewHolder(parent)
            AUDIO.ordinal -> createAudioMediaViewHolder(parent)
            else -> throw IllegalArgumentException("$logTag : Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseCollectibleMediaItem>, position: Int) {
        holder.bind(getItem(position))
    }

    private fun createImageMediaViewHolder(parent: ViewGroup): CollectibleImageMediaViewHolder {
        return CollectibleImageMediaViewHolder.create(parent, mediaListener)
    }

    private fun createVideoMediaViewHolder(parent: ViewGroup): CollectibleVideoMediaViewHolder {
        return CollectibleVideoMediaViewHolder.create(parent, mediaListener)
    }

    private fun createUnsupportedMediaViewHolder(parent: ViewGroup): CollectibleUnsupportedMediaViewHolder {
        return CollectibleUnsupportedMediaViewHolder.create(parent, mediaListener)
    }

    private fun createNoMediaViewHolder(parent: ViewGroup): CollectibleNoMediaViewHolder {
        return CollectibleNoMediaViewHolder.create(parent, mediaListener)
    }

    private fun createGifMediaViewHolder(parent: ViewGroup): CollectibleGifMediaViewHolder {
        return CollectibleGifMediaViewHolder.create(parent, mediaListener)
    }

    private fun createAudioMediaViewHolder(parent: ViewGroup): CollectibleAudioMediaViewHolder {
        return CollectibleAudioMediaViewHolder.create(parent, mediaListener)
    }

    interface MediaClickListener {
        fun on3dModeClick(imageUrl: String?)
        fun onVideoMediaClick(videoUrl: String?)
        fun onAudioMediaClick(audioUrl: String?)
        fun onImageMediaClick(
            imageUrl: String?,
            collectibleImageView: View,
            cachedMediaUri: String
        )
    }

    companion object {
        private val logTag = CollectibleMediaAdapter::class.java.simpleName
    }
}
