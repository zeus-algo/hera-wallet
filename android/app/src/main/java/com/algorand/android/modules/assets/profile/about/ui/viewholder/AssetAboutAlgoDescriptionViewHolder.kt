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

package com.algorand.android.modules.assets.profile.about.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import network.voi.hera.databinding.ItemAssetAboutAssetDescriptionBinding
import com.algorand.android.modules.assets.profile.about.ui.model.BaseAssetAboutListItem

class AssetAboutAlgoDescriptionViewHolder(
    private val binding: ItemAssetAboutAssetDescriptionBinding
) : BaseAssetAboutAssetDescriptionViewHolder(binding) {

    override fun bind(item: BaseAssetAboutListItem) {
        if (item !is BaseAssetAboutListItem.BaseAssetDescriptionItem.AlgoDescriptionItem) return
        binding.descriptionTextView.setText(item.descriptionTextResId)
        super.bind(item)
    }

    companion object : BaseAssetAboutAssetDescriptionViewHolderItemViewHolderCreator {
        override fun create(parent: ViewGroup): AssetAboutAlgoDescriptionViewHolder {
            val binding =
                ItemAssetAboutAssetDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AssetAboutAlgoDescriptionViewHolder(binding)
        }
    }
}
