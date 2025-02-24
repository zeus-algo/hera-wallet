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

package com.algorand.android.modules.transaction.detail.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.algorand.android.databinding.ItemTransactionNoteBinding
import com.algorand.android.models.BaseViewHolder
import com.algorand.android.modules.transaction.detail.ui.model.TransactionDetailItem
import com.algorand.android.utils.enableLongPressToCopyText

class TransactionNoteViewHolder(
    private val binding: ItemTransactionNoteBinding
) : BaseViewHolder<TransactionDetailItem>(binding.root) {

    override fun bind(item: TransactionDetailItem) {
        if (item !is TransactionDetailItem.NoteItem) return
        with(binding) {
            dateLabelTextView.setText(item.labelTextRes)
            dateView.text = item.note
            root.enableLongPressToCopyText(item.note)
        }
    }

    companion object {
        fun create(parent: ViewGroup): TransactionNoteViewHolder {
            val binding = ItemTransactionNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TransactionNoteViewHolder(binding)
        }
    }
}
