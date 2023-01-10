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

package network.voi.hera.modules.assets.action.unsupportedasset

import android.widget.TextView
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.assets.action.base.BaseAssetActionBottomSheet
import network.voi.hera.utils.extensions.hide
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnsupportedAssetInfoActionBottomSheet : BaseAssetActionBottomSheet() {

    private val toolbarConfiguration = ToolbarConfiguration(titleResId = R.string.account_does_not_accept)

    override val assetActionViewModel by viewModels<UnsupportedAssetInfoActionViewModel>()

    override fun setDescriptionTextView(textView: TextView) {
        textView.setText(R.string.unfortunately_this_account)
    }

    override fun setToolbar(customToolbar: CustomToolbar) {
        customToolbar.configure(toolbarConfiguration)
    }

    override fun setPositiveButton(materialButton: MaterialButton) {
        materialButton.apply {
            setText(R.string.ok)
            setOnClickListener { navBack() }
        }
    }

    override fun setNegativeButton(materialButton: MaterialButton) {
        materialButton.hide()
    }
}
