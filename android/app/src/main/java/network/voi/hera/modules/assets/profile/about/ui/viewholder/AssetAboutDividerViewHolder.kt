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

package network.voi.hera.modules.assets.profile.about.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import network.voi.hera.databinding.ItemAssetAboutDividerBinding
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.assets.profile.about.ui.model.BaseAssetAboutListItem

class AssetAboutDividerViewHolder(
    binding: ItemAssetAboutDividerBinding
) : BaseViewHolder<BaseAssetAboutListItem>(binding.root) {

    override fun bind(item: BaseAssetAboutListItem) {
        if (item !is BaseAssetAboutListItem.DividerItem) return
    }

    companion object {
        fun create(parent: ViewGroup): AssetAboutDividerViewHolder {
            val binding = ItemAssetAboutDividerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AssetAboutDividerViewHolder(binding)
        }
    }
}
