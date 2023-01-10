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

package network.voi.hera.modules.transaction.detail.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import network.voi.hera.R
import network.voi.hera.databinding.ItemTransactionAmountBinding
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.transaction.detail.ui.model.TransactionDetailItem

class TransactionAmountViewHolder(
    private val binding: ItemTransactionAmountBinding
) : BaseViewHolder<TransactionDetailItem>(binding.root) {

    override fun bind(item: TransactionDetailItem) {
        if (item !is TransactionDetailItem.StandardTransactionItem.TransactionAmountItem) return
        with(binding) {
            amountLabelTextView.setText(item.labelTextRes)
            amountView.apply {
                setTextColor(ContextCompat.getColor(context, item.transactionSign.color))
                val transactionSign = item.transactionSign.signTextRes?.run { context.getString(this) }.orEmpty()
                text = context.getString(
                    R.string.pair_value_format_packed,
                    transactionSign,
                    item.formattedTransactionAmount
                )
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): TransactionAmountViewHolder {
            val binding = ItemTransactionAmountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TransactionAmountViewHolder(binding)
        }
    }
}
