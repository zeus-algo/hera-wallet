/*
 *  Copyright 2022 Pera Wallet, LDA
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package network.voi.hera.modules.webexport.accountconfirmation.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import network.voi.hera.R
import network.voi.hera.databinding.ItemAccountBinding
import network.voi.hera.models.AccountIconResource
import network.voi.hera.modules.webexport.accountconfirmation.ui.model.BaseAccountConfirmationListItem
import network.voi.hera.utils.AccountIconDrawable

class AccountConfirmationItemViewHolder(
    private val binding: ItemAccountBinding
) : RecyclerView.ViewHolder(binding.root) {

    private fun setAccountStartIconDrawable(accountIconResource: AccountIconResource?) {
        with(binding.accountItemView) {
            val accountIconSize = resources.getDimension(R.dimen.account_icon_size_large).toInt()
            val accountIconDrawable = AccountIconDrawable.create(
                context = context,
                accountIconResource = accountIconResource
                    ?: AccountIconResource.DEFAULT_ACCOUNT_ICON_RESOURCE,
                size = accountIconSize
            )
            setStartIconDrawable(accountIconDrawable)
        }
    }

    private fun setAccountTitleText(accountTitleText: String?) {
        binding.accountItemView.setTitleText(accountTitleText)
    }

    private fun setAccountDescriptionText(accountDescriptionText: String?) {
        binding.accountItemView.setDescriptionText(accountDescriptionText)
    }

    fun bind(item: BaseAccountConfirmationListItem.AccountItem) {
        with(item.accountAssetIconNameConfiguration) {
            setAccountStartIconDrawable(startAccountIconResource)
            setAccountTitleText(title)
            setAccountDescriptionText(description)
        }
        binding.root.apply {
            item.topMarginResId?.let {
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(0, resources.getDimensionPixelSize(it), 0, 0)
                }
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): AccountConfirmationItemViewHolder {
            val binding = ItemAccountBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return AccountConfirmationItemViewHolder(binding)
        }
    }
}
