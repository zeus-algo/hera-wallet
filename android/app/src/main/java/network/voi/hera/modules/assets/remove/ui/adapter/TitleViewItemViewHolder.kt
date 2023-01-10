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

import android.view.LayoutInflater
import android.view.ViewGroup
import network.voi.hera.databinding.ItemRemoveAssetTitleBinding
import network.voi.hera.models.BaseRemoveAssetItem
import network.voi.hera.models.BaseViewHolder

class TitleViewItemViewHolder(
    private val binding: ItemRemoveAssetTitleBinding
) : BaseViewHolder<BaseRemoveAssetItem>(binding.root) {
    override fun bind(item: BaseRemoveAssetItem) {
        if (item !is BaseRemoveAssetItem.TitleViewItem) return
        binding.titleTextView.setText(item.titleTextRes)
    }

    companion object {
        fun create(parent: ViewGroup): TitleViewItemViewHolder {
            val binding = ItemRemoveAssetTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TitleViewItemViewHolder(binding)
        }
    }
}
