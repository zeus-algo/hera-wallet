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

package network.voi.hera.modules.collectibles.action.optout

import android.widget.TextView
import androidx.fragment.app.viewModels
import network.voi.hera.MainActivity
import network.voi.hera.R
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AssetActionResult
import network.voi.hera.modules.assets.action.base.BaseAssetActionBottomSheet
import network.voi.hera.utils.AccountIconDrawable
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.setDrawable
import network.voi.hera.utils.setFragmentNavigationResult
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectibleOptOutConfirmationBottomSheet : BaseAssetActionBottomSheet() {

    override val assetActionViewModel by viewModels<CollectibleOptOutConfirmationViewModel>()

    override fun initUi() {
        with(binding) {
            transactionFeeGroup.show()
            accountGroup.show()
        }
    }

    override fun setDescriptionTextView(textView: TextView) {
        textView.text = getString(R.string.you_are_about_to_opt, assetActionViewModel.assetName.getName(resources))
    }

    override fun setToolbar(customToolbar: CustomToolbar) {
        customToolbar.changeTitle(getString(R.string.opt_out_nft))
    }

    override fun setPositiveButton(materialButton: MaterialButton) {
        materialButton.apply {
            setText(R.string.remove)
            setOnClickListener {
                asset?.let { assetDescription ->
                    val assetActionResult = AssetActionResult(
                        asset = assetDescription,
                        publicKey = assetActionViewModel.accountAddress
                    )
                    (activity as? MainActivity)?.signRemoveAssetTransaction(assetActionResult)
                    setFragmentNavigationResult(COLLECTIBLE_OPT_OUT_KEY, true)
                }
                navBack()
            }
        }
    }

    override fun setNegativeButton(materialButton: MaterialButton) {
        materialButton.apply {
            setText(R.string.keep_it)
            setOnClickListener { navBack() }
        }
    }

    override fun setTransactionFeeTextView(textView: TextView) {
        textView.text = assetActionViewModel.getTransactionFee()
    }

    override fun setAccountNameTextView(textView: TextView) {
        textView.apply {
            with(assetActionViewModel.getAccountName()) {
                text = getDisplayAddress()
                setDrawable(
                    start = AccountIconDrawable.create(
                        context = context,
                        accountIconResource = accountIconResource ?: AccountIconResource.DEFAULT_ACCOUNT_ICON_RESOURCE,
                        size = resources.getDimension(R.dimen.account_icon_size_normal).toInt()
                    )
                )
                setOnLongClickListener { onAccountAddressCopied(publicKey); true }
            }
        }
    }

    companion object {
        const val COLLECTIBLE_OPT_OUT_KEY = "collectible_opt_out_key"
    }
}
