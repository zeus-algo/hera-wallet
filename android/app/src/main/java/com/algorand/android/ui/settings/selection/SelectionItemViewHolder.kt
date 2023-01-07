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

package com.algorand.android.ui.settings.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.voi.hera.databinding.ItemSelectionBinding

class SelectionItemViewHolder(
    private val binding: ItemSelectionBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(selectionListItem: SelectionListItem) {
        with(binding.selectionItemView) {
            text = selectionListItem.getVisibleName(itemView.context)
            isSelected = selectionListItem.isSelected
        }
    }

    companion object {
        fun create(parent: ViewGroup): SelectionItemViewHolder {
            val binding = ItemSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SelectionItemViewHolder(binding)
        }
    }
}
