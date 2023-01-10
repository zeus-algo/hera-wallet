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

package network.voi.hera.modules.sorting.accountsorting.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import network.voi.hera.R
import network.voi.hera.databinding.ItemAccountSortBinding
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AccountIconResource.Companion.DEFAULT_ACCOUNT_ICON_RESOURCE
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.models.ui.AccountAssetItemButtonState
import network.voi.hera.modules.sorting.accountsorting.domain.model.BaseAccountSortingListItem
import network.voi.hera.utils.AccountIconDrawable

class AccountSortItemViewHolder(
    private val binding: ItemAccountSortBinding,
    private val listener: DragButtonPressedListener
) : BaseViewHolder<BaseAccountSortingListItem>(binding.root) {

    override fun bind(item: BaseAccountSortingListItem) {
        if (item !is BaseAccountSortingListItem.AccountSortListItem) return
        with(binding) {
            with(item.accountListItem.itemConfiguration) {
                setAccountStartIconDrawable(accountIconResource)
                setAccountTitleText(accountDisplayName?.getDisplayTextOrAccountShortenedAddress())
                setAccountDescriptionText(accountDisplayName?.getAccountShortenedAddressOrAccountType(root.resources))
                setAccountItemDragButton()
            }
        }
    }

    private fun setAccountStartIconDrawable(accountIconResource: AccountIconResource?) {
        with(binding.accountItemView) {
            val accountIconSize = resources.getDimension(R.dimen.account_icon_size_large).toInt()
            val accountIconDrawable = AccountIconDrawable.create(
                context = context,
                accountIconResource = accountIconResource ?: DEFAULT_ACCOUNT_ICON_RESOURCE,
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

    private fun setAccountItemDragButton() {
        binding.accountItemView.apply {
            setButtonState(AccountAssetItemButtonState.DRAGGABLE)
            setActionButtonOnTouchClickListener { listener.onPressed(this@AccountSortItemViewHolder) }
        }
    }

    fun interface DragButtonPressedListener {
        fun onPressed(viewHolder: AccountSortItemViewHolder)
    }

    companion object {
        fun create(parent: ViewGroup, listener: DragButtonPressedListener): AccountSortItemViewHolder {
            val binding = ItemAccountSortBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AccountSortItemViewHolder(binding, listener)
        }
    }
}
