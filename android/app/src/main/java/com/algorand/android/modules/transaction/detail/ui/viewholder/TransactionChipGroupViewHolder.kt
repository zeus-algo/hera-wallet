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
import network.voi.hera.R
import network.voi.hera.databinding.ItemTransactionChipGroupBinding
import com.algorand.android.models.BaseViewHolder
import com.algorand.android.modules.transaction.detail.ui.model.TransactionDetailItem
import com.algorand.android.ui.common.walletconnect.WalletConnectExtrasChipGroupView

class TransactionChipGroupViewHolder(
    private val binding: ItemTransactionChipGroupBinding,
    private val listener: WalletConnectExtrasChipGroupView.Listener
) : BaseViewHolder<TransactionDetailItem>(binding.root) {

    private val transactionIdsOfCreatedViews = mutableSetOf<String>()

    override fun bind(item: TransactionDetailItem) {
        if (item !is TransactionDetailItem.ChipGroupItem) return
        if (transactionIdsOfCreatedViews.contains(item.transactionId)) return
        binding.chipGroupView.apply {
            initOpenInExplorerChips(
                algoExplorerUrl = item.algoExplorerUrl,
                goalSeekerUrl = item.goalSeekerUrl,
                padding = R.dimen.spacing_zero
            )
            setChipGroupListener(listener)
        }
        transactionIdsOfCreatedViews.add(item.transactionId)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: WalletConnectExtrasChipGroupView.Listener
        ): TransactionChipGroupViewHolder {
            val binding = ItemTransactionChipGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TransactionChipGroupViewHolder(binding, listener)
        }
    }
}
