/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 */

package network.voi.hera.modules.webexport.accountselection.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import network.voi.hera.databinding.ItemSelectableAccountAssetBinding
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.webexport.accountselection.ui.model.BaseAccountMultipleSelectionListItem

class SelectableAccountItemViewHolder(
    private val binding: ItemSelectableAccountAssetBinding,
    private val listener: Listener
) : BaseViewHolder<BaseAccountMultipleSelectionListItem>(binding.root) {

    override fun bind(item: BaseAccountMultipleSelectionListItem) {
        if (item !is BaseAccountMultipleSelectionListItem.AccountItem) return
        binding.accountAssetIconNameView.initWithConfiguration(item.accountAssetIconNameConfiguration)
        binding.checkBox.isChecked = item.isChecked
        binding.root.apply {
            setOnClickListener { listener.onAccountItemClicked(accountAddress = item.address) }
            item.topMarginResId?.let {
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(0, resources.getDimensionPixelSize(it), 0, 0)
                }
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, listener: Listener): SelectableAccountItemViewHolder {
            val binding = ItemSelectableAccountAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SelectableAccountItemViewHolder(binding, listener)
        }
    }

    fun interface Listener {
        fun onAccountItemClicked(accountAddress: String)
    }
}
