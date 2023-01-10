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

package network.voi.hera.discover.home.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import network.voi.hera.discover.home.ui.model.DiscoverAssetItem
import network.voi.hera.models.BaseDiffUtil
import network.voi.hera.models.BaseViewHolder

class DiscoverAssetSearchAdapter(
    private val listener: DiscoverAssetSearchAdapterListener
) : PagingDataAdapter<DiscoverAssetItem, BaseViewHolder<DiscoverAssetItem>>(BaseDiffUtil()) {

    private val discoverAssetItemListener = object : DiscoverAssetItemViewHolder.DiscoverAssetSearchItemListener {
        override fun onAssetItemClick(assetId: Long) {
            listener.onNavigateToAssetDetail(assetId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DiscoverAssetItem> {
        return DiscoverAssetItemViewHolder.create(parent, listener = discoverAssetItemListener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DiscoverAssetItem>, position: Int) {
        holder.bind(getItem(position) ?: return)
    }

    interface DiscoverAssetSearchAdapterListener {
        fun onNavigateToAssetDetail(assetId: Long)
    }
}
