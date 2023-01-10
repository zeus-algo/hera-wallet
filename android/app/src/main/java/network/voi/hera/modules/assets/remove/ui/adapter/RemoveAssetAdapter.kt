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

package network.voi.hera.modules.assets.remove.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import network.voi.hera.models.BaseDiffUtil
import network.voi.hera.models.BaseRemoveAssetItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.BaseRemoveCollectibleItem
import network.voi.hera.models.BaseRemoveAssetItem.BaseRemovableItem.RemoveAssetItem
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.DESCRIPTION_VIEW_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.SCREEN_STATE_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_ASSET_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_COLLECTIBLE_AUDIO_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_COLLECTIBLE_IMAGE_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_COLLECTIBLE_MIXED_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_COLLECTIBLE_NOT_SUPPORTED_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.REMOVE_COLLECTIBLE_VIDEO_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.SEARCH_VIEW_ITEM
import network.voi.hera.models.BaseRemoveAssetItem.ItemType.TITLE_VIEW_ITEM
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.accountdetail.assets.ui.adapter.AccountAssetsAdapter
import network.voi.hera.modules.assets.remove.ui.ScreenStateViewHolder
import network.voi.hera.utils.hideKeyboard

class RemoveAssetAdapter(
    private val listener: RemoveAssetAdapterListener
) : ListAdapter<BaseRemoveAssetItem, BaseViewHolder<BaseRemoveAssetItem>>(BaseDiffUtil()) {

    private val assetRemovalItemListener = object : RemoveAssetItemViewHolder.AssetRemovalItemListener {
        override fun onActionButtonClick(removeAssetListItem: RemoveAssetItem) {
            listener.onAssetRemoveClick(removeAssetListItem)
        }

        override fun onItemClick(assetId: Long) {
            listener.onAssetItemClick(assetId)
        }
    }

    private val collectibleRemovalItemListener =
        object : BaseRemoveCollectibleItemViewHolder.CollectibleRemovalItemListener {
            override fun onActionButtonClick(removeAssetListItem: BaseRemoveCollectibleItem) {
                listener.onCollectibleRemoveClick(removeAssetListItem)
            }

            override fun onItemClick(collectibleId: Long) {
                listener.onCollectibleItemClick(collectibleId)
            }
        }

    private val searchViewItemListener = SearchViewItemViewHolder.SearchViewItemListener {
        listener.onSearchQueryUpdate(it)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.itemType?.ordinal ?: RecyclerView.NO_POSITION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseRemoveAssetItem> {
        return when (viewType) {
            REMOVE_ASSET_ITEM.ordinal -> createRemoveAssetItemViewHolder(parent)
            REMOVE_COLLECTIBLE_IMAGE_ITEM.ordinal -> createRemoveCollectibleImageItemViewHolder(parent)
            REMOVE_COLLECTIBLE_NOT_SUPPORTED_ITEM.ordinal -> createRemoveNotSupportedCollectibleItemViewHolder(parent)
            REMOVE_COLLECTIBLE_VIDEO_ITEM.ordinal -> createRemoveCollectibleVideoItemViewHolder(parent)
            REMOVE_COLLECTIBLE_AUDIO_ITEM.ordinal -> createRemoveCollectibleAudioItemViewHolder(parent)
            REMOVE_COLLECTIBLE_MIXED_ITEM.ordinal -> createRemoveCollectibleMixedItemViewHolder(parent)
            SEARCH_VIEW_ITEM.ordinal -> crateSearchItemViewHolder(parent)
            TITLE_VIEW_ITEM.ordinal -> createTitleItemViewHolder(parent)
            DESCRIPTION_VIEW_ITEM.ordinal -> createDescriptionItemViewHolder(parent)
            SCREEN_STATE_ITEM.ordinal -> createScreenStateViewHolder(parent)
            else -> throw IllegalArgumentException("$logTag: Unknown viewType = $viewType")
        }
    }

    private fun createRemoveAssetItemViewHolder(parent: ViewGroup): RemoveAssetItemViewHolder {
        return RemoveAssetItemViewHolder.create(parent, assetRemovalItemListener)
    }

    private fun createRemoveNotSupportedCollectibleItemViewHolder(
        parent: ViewGroup
    ): RemoveNotSupportedCollectibleItemViewHolder {
        return RemoveNotSupportedCollectibleItemViewHolder.create(parent, collectibleRemovalItemListener)
    }

    private fun createRemoveCollectibleImageItemViewHolder(parent: ViewGroup): RemoveCollectibleImageItemViewHolder {
        return RemoveCollectibleImageItemViewHolder.create(parent, collectibleRemovalItemListener)
    }

    private fun createRemoveCollectibleMixedItemViewHolder(parent: ViewGroup): RemoveCollectibleMixedItemViewHolder {
        return RemoveCollectibleMixedItemViewHolder.create(parent, collectibleRemovalItemListener)
    }

    private fun createRemoveCollectibleVideoItemViewHolder(parent: ViewGroup): RemoveCollectibleVideoItemViewHolder {
        return RemoveCollectibleVideoItemViewHolder.create(parent, collectibleRemovalItemListener)
    }

    private fun createRemoveCollectibleAudioItemViewHolder(parent: ViewGroup): BaseViewHolder<BaseRemoveAssetItem> {
        return RemoveCollectibleAudioItemViewHolder.create(parent, collectibleRemovalItemListener)
    }

    private fun createTitleItemViewHolder(parent: ViewGroup): TitleViewItemViewHolder {
        return TitleViewItemViewHolder.create(parent)
    }

    private fun createDescriptionItemViewHolder(parent: ViewGroup): DescriptionViewItemViewHolder {
        return DescriptionViewItemViewHolder.create(parent)
    }

    private fun crateSearchItemViewHolder(parent: ViewGroup): SearchViewItemViewHolder {
        return SearchViewItemViewHolder.create(parent, searchViewItemListener)
    }

    private fun createScreenStateViewHolder(parent: ViewGroup): ScreenStateViewHolder {
        return ScreenStateViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseRemoveAssetItem>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<BaseRemoveAssetItem>) {
        super.onViewDetachedFromWindow(holder)
        if (holder is SearchViewItemViewHolder) {
            holder.itemView.hideKeyboard()
        }
    }

    interface RemoveAssetAdapterListener {
        fun onSearchQueryUpdate(query: String)
        fun onAssetItemClick(assetId: Long)
        fun onCollectibleItemClick(collectibleId: Long)
        fun onCollectibleRemoveClick(baseRemoveAssetItem: BaseRemoveCollectibleItem)
        fun onAssetRemoveClick(baseRemoveAssetItem: RemoveAssetItem)
    }

    companion object {
        private val logTag = AccountAssetsAdapter::class.java.simpleName
    }
}
